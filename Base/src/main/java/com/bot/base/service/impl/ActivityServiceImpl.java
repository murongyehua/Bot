package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.jx.DailyQeq;
import com.bot.base.dto.jx.OpenReq;
import com.bot.base.service.BaseService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.dto.ActivityAwardDTO;
import com.bot.common.enums.ENAwardType;
import com.bot.common.enums.ENChatEngine;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.BotActivityUserMapper;
import com.bot.game.dao.mapper.BotUserAwardMapper;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("activityServiceImpl")
public class ActivityServiceImpl implements BaseService {

    @Resource
    private BotActivityUserMapper activityUserMapper;

    @Resource
    private BotUserAwardMapper userAwardMapper;

    @Resource
    private BotUserConfigMapper botUserConfigMapper;

    @Value("${jx3.url}")
    private String jx3Url;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        BotUserConfigExample example = new BotUserConfigExample();
        // 这里只按群查，但如果不是群就把token赋给群
        // 主要是抽奖功能还是按个人来的，暂时还不想弃用，就都保留了做兼容
        if (groupId == null) {
            groupId = token;
        }
        example.createCriteria().andUserIdEqualTo(groupId);
        List<BotUserConfig> botUserConfigs = botUserConfigMapper.selectByExample(example);
        String serverName = "";
        if (CollectionUtil.isNotEmpty(botUserConfigs)) {
            serverName = botUserConfigs.get(0).getJxServer();
        }
        String[] reqs =  reqContent.split(StrUtil.SPACE);
        // 绑定服务器
        if (reqs.length == 3 && reqs[1].equals(BaseConsts.Activity.BIND_SERVER)) {
            return new CommonResp(this.bindServer(groupId, reqs[2], botUserConfigs), ENRespType.TEXT.getType());
        }
        if (StrUtil.isEmpty(serverName)) {
            return new CommonResp("请先绑定服务器，格式为【剑三+空格+绑定区服+空格+区服名称】", ENRespType.TEXT.getType());
        }
        if (reqs.length == 2 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_GET_AWARD)) {
            return new CommonResp(this.getAward(token), ENRespType.TEXT.getType());
        }
        if (reqs.length == 2 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_MY_AWARD)) {
            return new CommonResp(this.fetchMyAward(token), ENRespType.TEXT.getType());
        }
        if (reqs.length == 2 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_ALL_AWARD)) {
            return new CommonResp(this.allAwardInfo(), ENRespType.TEXT.getType());
        }
        // 绑定抽奖id
        if (reqs.length == 3 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_BIND)) {
            return new CommonResp(this.bindAccount(token, reqs[2]), ENRespType.TEXT.getType());
        }
        // 活动日历
        if (reqs.length == 2 && (reqs[1].equals(BaseConsts.Activity.TODAY_DAILY)
                || reqs[1].equals(BaseConsts.Activity.TOMORROW_DAILY)
                || reqs[1].equals(BaseConsts.Activity.TOMORROW_TOMORROW_DAILY))) {
            return new CommonResp(this.dailyQuery(reqs[1], serverName), ENRespType.TEXT.getType());
        }
        // 开服情况
        if ((reqs.length == 2 || reqs.length == 3) && reqs[1].equals(BaseConsts.Activity.OPEN_SERVER)) {
            if (reqs.length == 3) {
                // 查非绑定区服
                return new CommonResp(this.openServer(reqs[2]), ENRespType.TEXT.getType());
            }
            return new CommonResp(this.openServer(serverName), ENRespType.TEXT.getType());
        }
        return new CommonResp(BaseConsts.Activity.UN_KNOW, ENRespType.TEXT.getType());
    }

    private String openServer(String serverName) {
        try {
            String response = HttpSenderUtil.postJsonData(jx3Url + "/server/check", JSONUtil.toJsonStr(new OpenReq(serverName)));
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer) respObj.get("code");
            if (code != 200) {
                log.error("获取开服状态失败，返回信息：{}", response);
                return "获取失败，请联系管理员检查";
            }
            // 解析
            JSONObject data = JSONUtil.parseObj(respObj.get("data"));
            String zone = (String) data.get("zone");
            String server = (String) data.get("server");
            int status = (Integer) data.get("status");
//            int time = (Integer) data.get("time");
            return String.format("【%s】%s,%s", zone, server, status == 1? "已开服" : "维护中");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return "获取失败，请联系管理员检查";
        }
    }

    private String bindServer(String token, String serverName, List<BotUserConfig> botUserConfigs) {
        if (CollectionUtil.isEmpty(botUserConfigs)) {
            BotUserConfig botUserConfig = new BotUserConfig();
            botUserConfig.setId(IdUtil.simpleUUID());
            botUserConfig.setUserId(token);
            botUserConfig.setChatEngine(ENChatEngine.DEFAULT.getValue());
            botUserConfig.setJxServer(serverName);
            botUserConfigMapper.insert(botUserConfig);
            return "绑定成功。";
        }
        BotUserConfig botUserConfig = botUserConfigs.get(0);
        botUserConfig.setJxServer(serverName);
        botUserConfigMapper.updateByPrimaryKey(botUserConfig);
        return "绑定成功。";
    }

    private String dailyQuery(String content, String serverName) {
        try {
            String response = null;
            if (content.equals(BaseConsts.Activity.TODAY_DAILY)) {
                response = HttpSenderUtil.postJsonData(jx3Url + "/active/calendar", JSONUtil.toJsonStr(new DailyQeq(serverName, 0)));
            }else if (content.equals(BaseConsts.Activity.TOMORROW_DAILY)) {
                response = HttpSenderUtil.postJsonData(jx3Url + "/active/calendar", JSONUtil.toJsonStr(new DailyQeq(serverName, 1)));
            }else if (content.equals(BaseConsts.Activity.TOMORROW_TOMORROW_DAILY)) {
                response = HttpSenderUtil.postJsonData(jx3Url + "/active/calendar", JSONUtil.toJsonStr(new DailyQeq(serverName, 2)));
            }else {
                return "指令错误，暂只支持查3日内的日常。";
            }
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer)respObj.get("code");
            if (code != 200) {
                log.error("获取日常失败，返回信息：{}", response);
                return "获取失败，请联系管理员检查";
            }
            // 解析
            JSONObject data = JSONUtil.parseObj(respObj.get("data"));
            String date = (String) data.get("date");
            String war = (String) data.get("war");
            String battle = (String) data.get("battle");
            String orecar = (String) data.get("orecar");
            String school = (String) data.get("school");
            String rescue = (String) data.get("rescue");
            JSONArray lucks = (JSONArray) data.get("luck");
            JSONArray teams = (JSONArray) data.get("team");
            String draw = (String) data.get("draw");
            if (draw != null) {
                return String.format(BaseConsts.Activity.DAILY_RETURN_FORMAT, content, date, war, battle, orecar, school, rescue, draw, lucks.get(0), lucks.get(1), lucks.get(2),
                        teams.get(0), teams.get(1), teams.get(2));
            }
            return String.format(BaseConsts.Activity.DAILY_RETURN_FORMAT_WITHOUT_DRAW, content, date, war, battle, orecar, school, rescue, lucks.get(0), lucks.get(1), lucks.get(2),
                    teams.get(0), teams.get(1), teams.get(2));
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return "获取失败，请联系管理员检查";
        }
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
