package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.dto.ActivityAwardDTO;
import com.bot.common.enums.ENAwardType;
import com.bot.common.enums.ENRespType;
import com.bot.game.dao.entity.BotActivityUser;
import com.bot.game.dao.entity.BotActivityUserExample;
import com.bot.game.dao.entity.BotUserAward;
import com.bot.game.dao.entity.BotUserAwardExample;
import com.bot.game.dao.mapper.BotActivityUserMapper;
import com.bot.game.dao.mapper.BotUserAwardMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("activityServiceImpl")
public class ActivityServiceImpl implements BaseService {

    @Resource
    private BotActivityUserMapper activityUserMapper;

    @Resource
    private BotUserAwardMapper userAwardMapper;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token) {
        String[] reqs =  reqContent.split(StrUtil.SPACE);
        if (reqs.length == 2 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_GET_AWARD)) {
            return new CommonResp(this.getAward(token), ENRespType.TEXT.getType());
        }
        if (reqs.length == 2 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_MY_AWARD)) {
            return new CommonResp(this.fetchMyAward(token), ENRespType.TEXT.getType());
        }
        if (reqs.length == 2 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_ALL_AWARD)) {
            return new CommonResp(this.allAwardInfo(), ENRespType.TEXT.getType());
        }
        if (reqs.length == 3 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_BIND)) {
            return new CommonResp(this.bindAccount(token, reqs[2]), ENRespType.TEXT.getType());
        }
        return new CommonResp(BaseConsts.Activity.UN_KNOW, ENRespType.TEXT.getType());
    }

    private String bindAccount(String token, String account) {
        BotActivityUserExample example = new BotActivityUserExample();
        example.createCriteria().andIdEqualTo(token);
        List<BotActivityUser> userList = activityUserMapper.selectByExample(example);
        if (CollectionUtil.isNotEmpty(userList)) {
            BotActivityUser botActivityUser = userList.get(0);
            botActivityUser.setBindId(account);
            activityUserMapper.updateByExampleSelective(botActivityUser, example);
        }else {
            BotActivityUser botActivityUser = new BotActivityUser();
            botActivityUser.setId(token);
            botActivityUser.setBindId(account);
            activityUserMapper.insert(botActivityUser);
        }
        return "绑定成功。";
    }

    private synchronized String getAward(String token) {
        if (CollectionUtil.isEmpty(SystemConfigCache.activityAwardList)) {
            return "当前没有开启的抽奖活动";
        }
        // 是否绑了游戏id
        BotActivityUserExample activityUserExample = new BotActivityUserExample();
        activityUserExample.createCriteria().andIdEqualTo(token);
        if (CollectionUtil.isEmpty(activityUserMapper.selectByExample(activityUserExample))) {
            return "请先绑定游戏id";
        }
        // 是否参与过
        BotUserAwardExample userAwardExample = new BotUserAwardExample();
        userAwardExample.createCriteria()
                .andActivityIdEqualTo(SystemConfigCache.activityAwardList.get(0).getActivityId())
                .andUserIdEqualTo(token);
        List<BotUserAward> userAwardList = userAwardMapper.selectByExample(userAwardExample);
        if (userAwardList.size() >= 1) {
            return BaseConsts.Activity.REPEAT;
        }
        // 抽奖
        ActivityAwardDTO baoDiDTO = null;
        ActivityAwardDTO awardDTO = null;
        for (ActivityAwardDTO activityAwardDTO :  SystemConfigCache.activityAwardList) {
            if (ENAwardType.DI_BAO.getValue().equals(activityAwardDTO.getType())) {
                baoDiDTO = activityAwardDTO;
            }else {
                int x = RandomUtil.randomInt(0, 100);
                if (activityAwardDTO.getNumber() > 0 && x < Integer.parseInt(activityAwardDTO.getPercent())) {
                    awardDTO = activityAwardDTO;
                    activityAwardDTO.setNumber(activityAwardDTO.getNumber() - 1);
                    break;
                }
            }
        }
        if (awardDTO == null && baoDiDTO != null) {
            awardDTO = baoDiDTO;
        }
        if (awardDTO == null) {
            return "很遗憾，未中奖";
        }else {
            // 保存
            BotUserAward botUserAward = new BotUserAward();
            botUserAward.setId(IdUtil.simpleUUID());
            botUserAward.setAwardName(String.format("【%s】%s", awardDTO.getPrefix(), awardDTO.getAwardName()));
            botUserAward.setUserId(token);
            botUserAward.setActivityId(awardDTO.getActivityId());
            botUserAward.setActivityAwardId(awardDTO.getId());
            userAwardMapper.insert(botUserAward);
            return String.format("恭喜您抽到了【%s】%s！", awardDTO.getPrefix(), awardDTO.getAwardName());
        }
    }

    private String fetchMyAward(String token) {
        BotUserAwardExample userAwardExample = new BotUserAwardExample();
        userAwardExample.createCriteria().andUserIdEqualTo(token);
        List<BotUserAward> userAwardList = userAwardMapper.selectByExample(userAwardExample);
        if (CollectionUtil.isEmpty(userAwardList)) {
            return "您当前无中奖信息。";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("您当前已中奖项如下：").append(StrUtil.CRLF);
        userAwardList.forEach(x -> {
            stringBuilder.append(x.getAwardName()).append(StrUtil.CRLF);
        });
        return stringBuilder.toString();
    }

    private String allAwardInfo() {
        List<BotUserAward> userAwardList = userAwardMapper.selectByExample(new BotUserAwardExample());
        if (CollectionUtil.isEmpty(userAwardList)) {
            return "无中奖信息。";
        }
        Map<String, String> tokenAccountMap = activityUserMapper.selectByExample(new BotActivityUserExample()).stream().collect(Collectors.toMap(BotActivityUser::getId, BotActivityUser::getBindId));
        Map<String, List<BotUserAward>> groupAwardMap = userAwardList.stream().collect(Collectors.groupingBy(BotUserAward::getUserId));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("中奖汇总：").append(StrUtil.CRLF);
        for (String token : groupAwardMap.keySet()) {
            stringBuilder.append("游戏id：").append(tokenAccountMap.get(token))
                    .append(StrUtil.CRLF).append("奖品：").append(StrUtil.CRLF);
            groupAwardMap.get(token).forEach(x -> {
                stringBuilder.append(x.getAwardName()).append(StrUtil.CRLF);
            });
        }
        return stringBuilder.toString();
    }

}
