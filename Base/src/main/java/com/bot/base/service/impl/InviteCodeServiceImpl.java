package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.DeepChatReq;
import com.bot.base.service.BaseService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.enums.ENRegStatus;
import com.bot.common.enums.ENRegType;
import com.bot.common.enums.ENRespType;
import com.bot.common.enums.ENSystemWord;
import com.bot.common.util.HttpSenderUtil;
import com.bot.common.util.SendMsgUtil;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.*;
import com.bot.game.service.SystemConfigHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 邀请码服务
 */
@Slf4j
@Service("inviteCodeServiceImpl")
public class InviteCodeServiceImpl implements BaseService {

    @Resource
    private BotGameUserScoreMapper gameUserScoreMapper;

    @Resource
    private BotUserWordMapper userWordMapper;

    @Resource
    private BotUserMapper userMapper;

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @Value("${chat.url}")
    private String chatUrl;

    @Value("${invite.code.key}")
    private String inviteCodeKey;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        // 1. 生成邀请码
        if (ObjectUtil.equals("邀请码", reqContent)) {
            return handleGenerateInviteCode(token, groupId);
        }

        // 2. 使用邀请码激活（需要检查缓存中是否存在该邀请码）
        if (SystemConfigCache.userInviteCodeMap.containsKey(reqContent)) {
            return handleUseInviteCode(reqContent, groupId == null ? token : groupId, token);
        }

        return null;
    }

    /**
     * 生成邀请码
     */
    private CommonResp handleGenerateInviteCode(String userId, String groupId) {
        try {
            // 查询用户积分信息
            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
            scoreExample.createCriteria().andUserIdEqualTo(userId);
            List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(scoreExample);

            BotGameUserScore userScore;
            if (CollectionUtil.isEmpty(scores)) {
                // 首次使用，初始化用户积分数据
                userScore = new BotGameUserScore();
                userScore.setUserId(userId);
                userScore.setNickname(groupId != null ? SendMsgUtil.getGroupNickName(groupId, userId) : userId);
                userScore.setScore(0);
                userScore.setInviteCount(0);
                
                // 生成新邀请码
                String newInviteCode;
                try {
                    newInviteCode = generateUniqueInviteCode();
                } catch (Exception e) {
                    log.error("生成邀请码失败", e);
                    return new CommonResp("生成邀请码失败，请稍后再试~", ENRespType.TEXT.getType());
                }
                
                userScore.setInviteCode(newInviteCode);
                gameUserScoreMapper.insert(userScore);
                
                // 刷新缓存
                systemConfigHolder.loadInviteCodes();
                
                return buildInviteCodeResponse(newInviteCode, 0);
            } else {
                userScore = scores.get(0);
                
                // 检查是否已有邀请码
                if (StrUtil.isNotEmpty(userScore.getInviteCode())) {
                    // 已有邀请码，直接返回
                    int inviteCount = userScore.getInviteCount() != null ? userScore.getInviteCount() : 0;
                    return buildInviteCodeResponse(userScore.getInviteCode(), inviteCount);
                } else {
                    // 生成新邀请码
                    String newInviteCode;
                    try {
                        newInviteCode = generateUniqueInviteCode();
                    } catch (Exception e) {
                        log.error("生成邀请码失败", e);
                        return new CommonResp("生成邀请码失败，请稍后再试~", ENRespType.TEXT.getType());
                    }
                    
                    userScore.setInviteCode(newInviteCode);
                    userScore.setInviteCount(0);
                    gameUserScoreMapper.updateByPrimaryKey(userScore);
                    
                    // 刷新缓存
                    systemConfigHolder.loadInviteCodes();
                    
                    return buildInviteCodeResponse(newInviteCode, 0);
                }
            }
        } catch (Exception e) {
            log.error("生成邀请码异常", e);
            return new CommonResp("生成邀请码失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }

    /**
     * 使用邀请码激活
     */
    private CommonResp handleUseInviteCode(String inviteCode, String targetId, String userId) {
        try {
            // 检查是否是新用户（未在bot_user表中）
            BotUserExample userExample = new BotUserExample();
            userExample.createCriteria().andIdEqualTo(targetId);
            int userCount = userMapper.countByExample(userExample);
            
            if (userCount > 0) {
                // 不是新用户，不做任何返回
                return null;
            }
            
            // 是新用户，开通服务90天
            BotUser newUser = new BotUser();
            newUser.setSignDay(0);
            newUser.setId(targetId);
            newUser.setStatus(ENRegStatus.FOREVER.getValue());
            newUser.setType(ENRegType.GROUP.getValue());
            newUser.setDeadLineDate(DateUtil.offsetDay(new Date(), 90));
            userMapper.insert(newUser);
            
            // 刷新用户缓存
            systemConfigHolder.loadUsers();
            
            // 查找邀请人
            String inviterId = SystemConfigCache.userInviteCodeMap.get(inviteCode);
            if (StrUtil.isEmpty(inviterId)) {
                log.error("邀请码[{}]找不到对应的邀请人", inviteCode);
                return new CommonResp("服务已开通90天，感谢使用！", ENRespType.TEXT.getType());
            }
            
            // 给邀请人增加计数
            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
            scoreExample.createCriteria().andUserIdEqualTo(inviterId);
            List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(scoreExample);
            
            if (!CollectionUtil.isEmpty(scores)) {
                BotGameUserScore inviterScore = scores.get(0);
                int oldCount = inviterScore.getInviteCount() != null ? inviterScore.getInviteCount() : 0;
                int newCount = oldCount + 1;
                inviterScore.setInviteCount(newCount);
                
                // 更新积分
                int oldScore = inviterScore.getScore() != null ? inviterScore.getScore() : 0;
                inviterScore.setScore(oldScore + 5);
                
                gameUserScoreMapper.updateByPrimaryKey(inviterScore);
                
                // 检查是否达到奖励条件，发放词条奖励
                grantInviteRewards(inviterId, newCount, oldCount);
            }
            
            return new CommonResp("服务已开通90天，感谢使用！", ENRespType.TEXT.getType());
            
        } catch (Exception e) {
            log.error("使用邀请码异常", e);
            return new CommonResp("激活失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }

    /**
     * 生成唯一邀请码（使用AI生成并判重）
     */
    private String generateUniqueInviteCode() throws Exception {
        int maxRetries = 5;
        for (int i = 0; i < maxRetries; i++) {
            String code = callAIToGenerateCode();
            
            // 如果AI调用失败，直接抛出异常
            if (code == null) {
                throw new Exception("AI生成邀请码失败");
            }
            
            // 检查是否重复
            if (!SystemConfigCache.userInviteCodeMap.containsKey(code)) {
                return code;
            }
            
            log.warn("生成的邀请码[{}]已存在，重新生成", code);
        }
        
        // 如果5次都重复，抛出异常
        throw new Exception("生成邀请码失败，多次尝试后仍然重复");
    }

    /**
     * 调用AI生成邀请码
     * @return 邀请码，失败返回null
     */
    private String callAIToGenerateCode() {
        try {
            String response = HttpSenderUtil.postJsonDataWithToken(chatUrl,
                    JSONUtil.toJsonStr(new DeepChatReq(new JSONObject(), "生成邀请码", "blocking", null, IdUtil.fastUUID())),
                    inviteCodeKey);
            StringBuilder answer = new StringBuilder();
            JSONObject json = JSONUtil.parseObj(response);
            answer = new StringBuilder(json.getStr("answer"));
            return UnicodeUtil.toString(answer.toString());
        } catch (Exception e) {
            log.error("AI生成邀请码异常", e);
            return null;
        }
    }

    /**
     * 构建邀请码返回消息
     */
    private CommonResp buildInviteCodeResponse(String inviteCode, int inviteCount) {
        StringBuilder message = new StringBuilder();
        message.append("━━━━━━━━━━━━\n");
        message.append("🎁 您的专属邀请码 🎁\n");
        message.append("━━━━━━━━━━━━\n\n");
        message.append(String.format("📜 邀请码：%s\n\n", inviteCode));
        message.append(String.format("👥 已邀请：%d 人\n\n", inviteCount));
        
        message.append("【奖励进度】\n");
        message.append(String.format("%s 邀请1人：积分+5，词条「小林推广大使」\n", 
                inviteCount >= 1 ? "✅" : "⏳"));
        message.append(String.format("%s 邀请3人：积分+15，词条「摆渡人」\n", 
                inviteCount >= 3 ? "✅" : "⏳"));
        message.append(String.format("%s 邀请5人：积分+25，词条「松烟荐友人」\n", 
                inviteCount >= 5 ? "✅" : "⏳"));
        message.append(String.format("%s 邀请10人：积分+50，词条「星夜引路人」\n\n", 
                inviteCount >= 10 ? "✅" : "⏳"));
        
        message.append("━━━━━━━━━━━━\n");
        message.append("💡 新用户在新群聊中发送此邀请码即可激活服务");
        
        return new CommonResp(message.toString(), ENRespType.TEXT.getType());
    }

    /**
     * 发放邀请奖励（词条）
     */
    private void grantInviteRewards(String userId, int newCount, int oldCount) {
        try {
            // 检查是否跨越了奖励阈值
            ENSystemWord[] rewards = {
                ENSystemWord.INVITE_ONE,    // 1人
                ENSystemWord.INVITE_THREE,  // 3人
                ENSystemWord.INVITE_FIVE,   // 5人
                ENSystemWord.INVITE_TEN     // 10人
            };
            
            int[] thresholds = {1, 3, 5, 10};
            
            for (int i = 0; i < thresholds.length; i++) {
                int threshold = thresholds[i];
                ENSystemWord reward = rewards[i];
                
                // 如果新计数达到阈值，且旧计数未达到，则发放奖励
                if (newCount >= threshold && oldCount < threshold) {
                    grantSystemWord(userId, reward);
                }
            }
        } catch (Exception e) {
            log.error("发放邀请奖励异常", e);
        }
    }

    /**
     * 发放系统词条
     */
    private void grantSystemWord(String userId, ENSystemWord systemWord) {
        try {
            // 1. 检查用户是否已经拥有该词条
            BotUserWordExample checkExample = new BotUserWordExample();
            checkExample.createCriteria()
                    .andUserIdEqualTo(userId)
                    .andWordIdEqualTo(systemWord.getId());
            int existCount = userWordMapper.countByExample(checkExample);
            
            if (existCount > 0) {
                // 已经拥有，不重复发放
                return;
            }
            
            // 2. 发放词条
            BotUserWord userWord = new BotUserWord();
            userWord.setUserId(userId);
            userWord.setWordId(systemWord.getId());
            userWord.setWordContent(systemWord.getWord());
            userWord.setRarity(systemWord.getRariy());
            userWord.setMerit(systemWord.getMerit());
            userWord.setFetchDate(DateUtil.now());
            userWordMapper.insert(userWord);
            
            // 3. 增加用户魅力值
            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
            scoreExample.createCriteria().andUserIdEqualTo(userId);
            List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(scoreExample);
            
            if (!CollectionUtil.isEmpty(scores)) {
                BotGameUserScore userScore = scores.get(0);
                int currentMerit = userScore.getAccumulateMerit() != null ? userScore.getAccumulateMerit() : 0;
                userScore.setAccumulateMerit(currentMerit + systemWord.getMerit());
                gameUserScoreMapper.updateByPrimaryKey(userScore);
            }
            
            log.info("成功为用户[{}]发放奖励词条[{}]", userId, systemWord.getWord());
            
        } catch (Exception e) {
            log.error("发放系统词条异常", e);
        }
    }
}
