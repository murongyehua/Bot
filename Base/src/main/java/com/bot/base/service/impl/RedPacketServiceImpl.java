package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.SendMsgUtil;
import com.bot.game.dao.entity.BotGameUserScore;
import com.bot.game.dao.entity.BotGameUserScoreExample;
import com.bot.game.dao.entity.BotRedPacket;
import com.bot.game.dao.entity.BotRedPacketExample;
import com.bot.game.dao.mapper.BotGameUserScoreMapper;
import com.bot.game.dao.mapper.BotRedPacketMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service("redPacketServiceImpl")
public class RedPacketServiceImpl implements BaseService {

    @Resource
    private BotRedPacketMapper redPacketMapper;

    @Resource
    private BotGameUserScoreMapper userScoreMapper;

    private static final int NEED_SCORE = 50;

    private static final String ACTIVE_CODE = "spring";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (ObjectUtil.notEqual(reqContent, "小林红包")) {
            return null;
        }
        log.info("[红包兑换] 开始处理, userId={}, activeCode={}", token, ACTIVE_CODE);
        
        // 1. 检查用户积分
        BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
        scoreExample.createCriteria().andUserIdEqualTo(token);
        List<BotGameUserScore> userScores = userScoreMapper.selectByExample(scoreExample);
        if (CollectionUtil.isEmpty(userScores)) {
            log.warn("[红包兑换] 用户不存在, userId={}", token);
            if (groupId != null) {
                SendMsgUtil.sendGroupMsgForGame(groupId, String.format("积分不足，本期红包兑换需要%s积分。", NEED_SCORE), token);
                return new CommonResp(null, ENRespType.TEXT.getType());
            }else {
                return new CommonResp(String.format("积分不足，本期红包兑换需要%s积分。", NEED_SCORE), ENRespType.TEXT.getType());
            }

        }
        
        BotGameUserScore userScore = userScores.get(0);
        if (userScore.getScore() < NEED_SCORE) {
            log.warn("[红包兑换] 积分不足, userId={}, currentScore={}, needScore={}", token, userScore.getScore(), NEED_SCORE);
            if (groupId != null) {
                SendMsgUtil.sendGroupMsgForGame(groupId, String.format("积分不足，本期红包兑换需要%s积分。", NEED_SCORE), token);
                return new CommonResp(null, ENRespType.TEXT.getType());
            }else {
                return new CommonResp(String.format("积分不足，本期红包兑换需要%s积分。", NEED_SCORE), ENRespType.TEXT.getType());
            }

        }
        
        // 2. 检查用户是否已经领过本期红包（防止重复领取）
        BotRedPacketExample userRedPacketCheck = new BotRedPacketExample();
        userRedPacketCheck.createCriteria()
            .andActiveCodeEqualTo(ACTIVE_CODE)
            .andFetchUserIdEqualTo(token);
        long userFetchCount = redPacketMapper.countByExample(userRedPacketCheck);
        if (userFetchCount > 0) {
            log.warn("[红包兑换] 重复领取, userId={}, activeCode={}", token, ACTIVE_CODE);
            if (groupId != null) {
                SendMsgUtil.sendGroupMsgForGame(groupId, "您已经兑换过本期红包，每人限领一次！", token);
                return new CommonResp(null, ENRespType.TEXT.getType());
            }else {
                return new CommonResp("您已经兑换过本期红包，每人限领一次！", ENRespType.TEXT.getType());
            }
        }
        
        // 3. 查询可用红包
        BotRedPacketExample redPacketExample = new BotRedPacketExample();
        redPacketExample.createCriteria().andStatusEqualTo("0").andActiveCodeEqualTo(ACTIVE_CODE);
        List<BotRedPacket> redPackets = redPacketMapper.selectByExample(redPacketExample);
        if (CollectionUtil.isEmpty(redPackets)) {
            log.warn("[红包兑换] 红包已兑完, userId={}, activeCode={}", token, ACTIVE_CODE);
            if (groupId != null) {
                SendMsgUtil.sendGroupMsgForGame(groupId, "本期红包已兑完，请下次再来！", token);
                return new CommonResp(null, ENRespType.TEXT.getType());
            }else {
                return new CommonResp("本期红包已兑完，请下次再来！", ENRespType.TEXT.getType());
            }
        }
        
        // 4. 使用乐观锁更新红包状态
        BotRedPacket redPacket = redPackets.get(0);
        Long redPacketId = redPacket.getId();
        Integer oldVersion = redPacket.getVersion();
        
        log.info("[红包兑换] 尝试锁定红包, userId={}, redPacketId={}, version={}", token, redPacketId, oldVersion);
        
        // 构造更新对象
        BotRedPacket updateRecord = new BotRedPacket();
        updateRecord.setStatus("1");
        updateRecord.setFetchUserId(token);
        updateRecord.setVersion(oldVersion + 1); // 版本号+1
        
        // 构造更新条件：WHERE id=? AND status='0' AND version=?
        BotRedPacketExample updateCondition = new BotRedPacketExample();
        updateCondition.createCriteria()
            .andIdEqualTo(redPacketId)
            .andStatusEqualTo("0")
            .andVersionEqualTo(oldVersion); // 乐观锁：只有版本号匹配才能更新
        
        int updateCount = redPacketMapper.updateByExampleSelective(updateRecord, updateCondition);
        
        if (updateCount == 0) {
            // 更新失败，说明红包已被其他用户抢走或版本号不匹配
            log.warn("[红包兑换] 红包已被抢走, userId={}, redPacketId={}, version={}", token, redPacketId, oldVersion);
            if (groupId != null) {
                SendMsgUtil.sendGroupMsgForGame(groupId, "哎呀，兑换的人太多了，请重试！", token);
            }else {
                SendMsgUtil.sendMsg(token, "哎呀，兑换的人太多了，请重试！");
            }
            // 抛出异常触发事务回滚
            throw new RuntimeException("红包更新失败，已被其他用户领取");
        }
        
        log.info("[红包兑换] 红包锁定成功, userId={}, redPacketId={}", token, redPacketId);
        
        // 5. 扣除积分
        try {
            int originalScore = userScore.getScore();
            userScore.setScore(originalScore - NEED_SCORE);
            userScoreMapper.updateByPrimaryKey(userScore);
            log.info("[红包兑换] 积分扣除成功, userId={}, 原积分={}, 扣除={}, 剩余={}", 
                token, originalScore, NEED_SCORE, userScore.getScore());
        } catch (Exception e) {
            log.error("[红包兑换] 积分扣除失败, userId={}, redPacketId={}", token, redPacketId, e);
            // 抛出异常触发事务回滚
            throw new RuntimeException("积分扣除失败", e);
        }
        
        // 6. 发放红包
        String message = String.format(
            "🎉 兑换成功！\r\n" +
            "💰 积分消耗：-%s\r\n" +
            "\r\n" +
            "═══════════════════\r\n" +
            "🔑 红包口令\r\n" +
            "═══════════════════\r\n" +
            "%s\r\n" +
            "═══════════════════\r\n" +
            "\r\n" +
            "📱 使用步骤：\r\n" +
            "1️⃣ 打开支付宝\r\n" +
            "2️⃣ 搜索【红包】\r\n" +
            "3️⃣ 输入上方口令领取\r\n" +
            "\r\n" +
            "⚠️ 温馨提示：\r\n" +
            "• 每个口令仅限使用一次\r\n" +
            "• 请勿分享给他人，先到先得哦~", 
            NEED_SCORE, redPacket.getContent());
        log.info("[红包兑换] 兑换成功, userId={}, redPacketId={}, content={}", token, redPacketId, redPacket.getContent());
        if (groupId != null) {
            SendMsgUtil.sendGroupMsgForGame(groupId, message, token);
            return new CommonResp(null, ENRespType.TEXT.getType());
        }else {
            return new CommonResp(message, ENRespType.TEXT.getType());
        }
    }
}
