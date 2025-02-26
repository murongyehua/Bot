package com.bot.base.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.MorningReq;
import com.bot.base.dto.UserTempInfoDTO;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENChineseNumber;
import com.bot.common.enums.ENMorningType;
import com.bot.common.util.HttpSenderUtil;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import com.bot.game.dao.entity.BotUserConfig;
import com.bot.game.dao.entity.BotUserConfigExample;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 状态监控者
 * @author murongyehua
 * @version 1.0 2020/9/28
 */
@Slf4j
@Component
public class StatusMonitor {

    @Value("${morning.url}")
    private String morningUrl;

    @Value("${history.today.url}")
    private String historyUrl;

    @Value("${history.today.key}")
    private String historyKey;

    @Value("${wenroutxt.url}")
    private String wenroutxt;

    @Value("${work.daily.urls}")
    private String workDailyUrls;

    @Resource
    private BotUserConfigMapper userConfigMapper;

    @PostConstruct
    public void systemManagerMonitor () {
        ThreadPoolManager.addBaseTask(() -> {
            try {
                while (true) {
                    UserTempInfoDTO userTempInfo = SystemManager.userTempInfo;
                    Date now = new Date();
                    if (userTempInfo != null && now.getTime() - userTempInfo.getOutTime().getTime() > 0 ) {
                        SystemManager.userTempInfo = null;
                        SendMsgUtil.sendMsg(userTempInfo.getToken(), BaseConsts.SystemManager.MANAGE_OUT_TIME);
                    }
                    // 日报发送
//                    this.morningSender();
                    // 打工日历发送
                    this.workDailySender();
                    Thread.sleep(5 * 60 * 1000);
                }
            }catch (Exception e) {
                log.error("管理模式状态监控出现异常", e);
            }
        });
    }

    /**
     * 打工日历发送 每天10:00之后 只要当日没发过的都发一遍
     */
    private void workDailySender() {
        // 是否过了当日10点
        if (new Date().getTime() > DateUtil.parse(DateUtil.today() + " 10:00:00", DatePattern.NORM_DATETIME_PATTERN).getTime()) {
            // 查询当天发送情况
            Map<String, String> userWorkDailySendMap = userConfigMapper.selectByExample(new BotUserConfigExample()).stream().filter(botUserConfig -> StrUtil.isNotBlank(botUserConfig.getWorkDailyConfig())).collect(Collectors.toMap(BotUserConfig::getUserId, BotUserConfig::getWorkDailyConfig));
            String today = DateUtil.today();
            String[] urls = workDailyUrls.split(StrUtil.COMMA);
            for(String token : SystemConfigCache.userWorkDaily) {
                String lastSendDate = userWorkDailySendMap.get(token);
                if (ObjectUtil.notEqual(today, lastSendDate)) {
                    // 当日没有发送 执行发送
                    for (String url : urls) {
                        SendMsgUtil.sendImg(token, url);
                    }
                }
                // 更新最后发送日期
                BotUserConfig botUserConfig = new BotUserConfig();
                botUserConfig.setWorkDailyConfig(today);
                BotUserConfigExample example = new BotUserConfigExample();
                example.createCriteria().andUserIdEqualTo(token);
                userConfigMapper.updateByExampleSelective(botUserConfig, example);
            }
        }
    }

    /**
     * 日报发送
     */
    private void morningSender() {
        Date now = new Date();
        String day = DateUtil.format(now, DatePattern.NORM_DATE_PATTERN);
        boolean isInMorningTime = DateUtil.isIn(now, DateUtil.parse(day + " 08:30:00", DatePattern.NORM_DATETIME_PATTERN), DateUtil.parse(day + " 09:00:00", DatePattern.NORM_DATETIME_PATTERN));
        boolean isInAfternoonTime = DateUtil.isIn(now, DateUtil.parse(day + " 14:00:00", DatePattern.NORM_DATETIME_PATTERN), DateUtil.parse(day + " 14:30:00", DatePattern.NORM_DATETIME_PATTERN));
        boolean isInEveningTime = DateUtil.isIn(now, DateUtil.parse(day + " 18:00:00", DatePattern.NORM_DATETIME_PATTERN), DateUtil.parse(day + " 18:30:00", DatePattern.NORM_DATETIME_PATTERN));

        if (isInMorningTime) {
            // 获取内容
            String content = this.fetchMorningContent();
            this.doSendContent(content, ENMorningType.MORNING);
        }
        if (isInAfternoonTime) {
            // 获取内容
            String content = this.fetchMorningContent();
            this.doSendContent(content, ENMorningType.AFTERNOON);
        }
        if (isInEveningTime) {
            // 获取内容
            String content = this.fetchMorningContent();
            this.doSendContent(content, ENMorningType.EVENING);
        }

    }

    private void doSendContent(String content, ENMorningType morningType) {
        if (CollectionUtil.isEmpty(SystemConfigCache.userMorningMap)) {
            return;
        }
        SystemConfigCache.morningSendMap.computeIfAbsent(morningType.getValue(), k -> new ArrayList<>());
        for (String token : SystemConfigCache.userMorningMap.keySet()) {
            String types = SystemConfigCache.userMorningMap.get(token);
            if (!types.contains(morningType.getValue())) {
                continue;
            }
            if (SystemConfigCache.morningSendMap.get(morningType.getValue()).contains(token)) {
                continue;
            }
            JSONArray array = (JSONArray) JSONUtil.parseObj(HttpSenderUtil.get(historyUrl + "?format=json&apiKey=" + historyKey, null)).get("content");
            String msg = String.format(BaseConsts.Morning.MORNING_FORMAT,
                    morningType.getFull(),
                    DateUtil.thisMonth() + 1,
                    DateUtil.thisDayOfMonth(),
                    ENChineseNumber.getLabelByValue(String.valueOf(DateUtil.thisDayOfWeek() - 1)),
                    array.get(RandomUtil.randomInt(0, array.size() - 1)),
                    content,
                    HttpSenderUtil.get(wenroutxt, null));
            if (token.contains("@chatroom")) {
                SendMsgUtil.sendGroupMsg(token, msg, null);
                SystemConfigCache.morningSendMap.get(morningType.getValue()).add(token);
                continue;
            }
            SendMsgUtil.sendMsg(token, msg);
            SystemConfigCache.morningSendMap.get(morningType.getValue()).add(token);
        }
    }

    private String fetchMorningContent() {
        return this.getMorning("weibo") +
                this.getMorning("baidu") +
                this.getMorning("zhihu");
    }

    private String getMorning(String type) {
        try {
            String weiboResponse = HttpSenderUtil.get(String.format(morningUrl, type), null);
            JSONArray dataList = (JSONArray) JSONUtil.parseObj(weiboResponse).get("data");
            StringBuilder stringBuilder = new StringBuilder();
            for(int index = 0; index < 8; index++) {
                JSONObject object = (JSONObject) dataList.get(index);
                stringBuilder.append(StrUtil.CRLF);
                stringBuilder.append("『").append((String) object.get("title")).append("』").append(StrUtil.CRLF);
                stringBuilder.append((String) object.get("mobilUrl"));
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
