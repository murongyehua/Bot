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
import com.bot.base.dto.DeepChatReq;
import com.bot.base.dto.MorningReq;
import com.bot.base.dto.UserTempInfoDTO;
import com.bot.base.service.impl.DefaultChatServiceImpl;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENChineseNumber;
import com.bot.common.enums.ENMorningType;
import com.bot.common.util.HttpSenderUtil;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import com.bot.game.dao.entity.BotDrinkRecord;
import com.bot.game.dao.entity.BotDrinkRecordExample;
import com.bot.game.dao.entity.BotUserConfig;
import com.bot.game.dao.entity.BotUserConfigExample;
import com.bot.game.dao.mapper.BotDrinkRecordMapper;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 状态监控者
 * @author murongyehua
 * @version 1.0 2020/9/28
 */
@Slf4j
@Component
public class StatusMonitor {

    @Value("${work.daily.urls}")
    private String workDailyUrls;

    @Resource
    private BotUserConfigMapper userConfigMapper;

    @Resource
    private BotDrinkRecordMapper drinkRecordMapper;

    @Value("${chat.url}")
    private String defaultUrl;

    @Value("${drink.chat.key}")
    private String drinkToken;

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
                    // 喝水记录发送
                    this.drinkSender();
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
            // 清除慢生图记录
            DefaultChatServiceImpl.SLOW_CREATE_TOKEN_TIMES_MAP.clear();
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
     * 喝水记录，每天12:00 和 18:00各发送一次
     */
    public void drinkSender() {
        // 是否在11:59-12:04, 17:59-18:04
        Date now = new Date();
        if (DateUtil.isIn(now,
                DateUtil.parse(DateUtil.today() + " 11:00:00", DatePattern.NORM_DATETIME_PATTERN),
                DateUtil.parse(DateUtil.today() + " 11:05:00", DatePattern.NORM_DATETIME_PATTERN))
                || DateUtil.isIn(now,
                DateUtil.parse(DateUtil.today() + " 16:59:00", DatePattern.NORM_DATETIME_PATTERN),
                DateUtil.parse(DateUtil.today() + " 17:04:00", DatePattern.NORM_DATETIME_PATTERN))) {
            // 开了开关才推送
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andDrinkSwitchEqualTo("1");
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            if (CollectionUtil.isEmpty(userConfigList)) {
                return;
            }
            List<String> activeUserIds = userConfigList.stream().map(BotUserConfig::getUserId).collect(Collectors.toList());
            // 根据记录的groupId是不是空，来判断是私聊记录的还是群聊记录的
            // 如果是个人，直接推送，如果是群，推送群聊里所有今天记录了的（私聊记录的也在群里推送）
            BotDrinkRecordExample drinkRecordExample = new BotDrinkRecordExample();
            drinkRecordExample.createCriteria().andDrinkTimeBetween(
                    DateUtil.format(DateUtil.beginOfDay(new Date()), DatePattern.NORM_DATETIME_FORMAT),
                    DateUtil.format(DateUtil.endOfDay(new Date()), DatePattern.NORM_DATETIME_FORMAT));
            List<BotDrinkRecord> botDrinkRecordList = drinkRecordMapper.selectByExample(drinkRecordExample);
            if (CollectionUtil.isEmpty(botDrinkRecordList)) {
                return;
            }
            Map<String, List<BotDrinkRecord>> drinkMap = botDrinkRecordList.stream().collect(Collectors.groupingBy(BotDrinkRecord::getUserId));
            for (String userId : drinkMap.keySet()) {
                // 按人遍历，三种情况
                // 1.只在私聊记录，则只发私聊
                // 2.只在群聊记录，则只发群聊
                // 3.私聊群聊都有记录，两边都发
                List<BotDrinkRecord> userDrinkRecordList = drinkMap.get(userId);
                List<String> hasRecordGroupIdList = userDrinkRecordList.stream()
                        .map(BotDrinkRecord::getGroupId)
                        .distinct()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (CollectionUtil.isEmpty(hasRecordGroupIdList)) {
                    // 第一种情况 只发私聊
                    if (activeUserIds.contains(userId)) {
                        SendMsgUtil.sendMsg(userId, this.getDrinkResult(userDrinkRecordList));
                    }
                    continue;
                }
                long groupCount = userDrinkRecordList.stream().filter(x -> x.getGroupId() != null).count();
                if (groupCount == userDrinkRecordList.size()) {
                    // 第二种情况 只发群聊 且每个群都要发
                    hasRecordGroupIdList.forEach(groupId -> {
                        if (activeUserIds.contains(groupId)) {
                            SendMsgUtil.sendGroupMsg(groupId, this.getDrinkResult(userDrinkRecordList), userId);
                        }
                    });
                    continue;
                }
                // 第三种情况，都发先发私聊再发群聊
                if (activeUserIds.contains(userId)) {
                    SendMsgUtil.sendMsg(userId, this.getDrinkResult(userDrinkRecordList));
                }
                hasRecordGroupIdList.forEach(groupId -> {
                    if (activeUserIds.contains(groupId)) {
                        SendMsgUtil.sendGroupMsg(groupId, this.getDrinkResult(userDrinkRecordList), userId);
                    }
                });
            }
        }

        // 每晚8点，逐一发送日报
        if (DateUtil.isIn(now,
                DateUtil.parse(DateUtil.today() + " 20:00:00", DatePattern.NORM_DATETIME_PATTERN),
                DateUtil.parse(DateUtil.today() + " 20:05:00", DatePattern.NORM_DATETIME_PATTERN))) {
            // 开了开关才推送
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andDrinkSwitchEqualTo("1");
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            if (CollectionUtil.isEmpty(userConfigList)) {
                return;
            }
            List<String> activeUserIds = userConfigList.stream().map(BotUserConfig::getUserId).collect(Collectors.toList());
            Map<String, String> userId2DrinkChatIdMap = userConfigList.stream().collect(Collectors.toMap(BotUserConfig::getUserId, BotUserConfig::getDrinkChatId));
            // 根据记录的groupId是不是空，来判断是私聊记录的还是群聊记录的
            // 如果是个人，直接推送，如果是群，推送群聊里所有今天记录了的（私聊记录的也在群里推送）
            BotDrinkRecordExample drinkRecordExample = new BotDrinkRecordExample();
            drinkRecordExample.createCriteria().andDrinkTimeBetween(
                    DateUtil.format(DateUtil.beginOfDay(new Date()), DatePattern.NORM_DATETIME_FORMAT),
                    DateUtil.format(DateUtil.endOfDay(new Date()), DatePattern.NORM_DATETIME_FORMAT));
            List<BotDrinkRecord> botDrinkRecordList = drinkRecordMapper.selectByExample(drinkRecordExample);
            if (CollectionUtil.isEmpty(botDrinkRecordList)) {
                return;
            }
            Map<String, List<BotDrinkRecord>> drinkMap = botDrinkRecordList.stream().collect(Collectors.groupingBy(BotDrinkRecord::getUserId));
            for (String userId : drinkMap.keySet()) {
                // 按人遍历，三种情况
                // 1.只在私聊记录，则只发私聊
                // 2.只在群聊记录，则只发群聊
                // 3.私聊群聊都有记录，两边都发
                List<BotDrinkRecord> userDrinkRecordList = drinkMap.get(userId);
                List<String> hasRecordGroupIdList = userDrinkRecordList.stream()
                        .map(BotDrinkRecord::getGroupId)
                        .distinct()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (CollectionUtil.isEmpty(hasRecordGroupIdList)) {
                    // 第一种情况 只发私聊
                    if (activeUserIds.contains(userId)) {
                        SendMsgUtil.sendMsg(userId, this.get4DrinkAIData(userDrinkRecordList, userId, userId2DrinkChatIdMap));
                    }
                    continue;
                }
                long groupCount = userDrinkRecordList.stream().filter(x -> x.getGroupId() != null).count();
                if (groupCount == userDrinkRecordList.size()) {
                    // 第二种情况 只发群聊 且每个群都要发
                    String content = this.get4DrinkAIData(userDrinkRecordList, userId, userId2DrinkChatIdMap);
                    hasRecordGroupIdList.forEach(groupId -> {
                        if (activeUserIds.contains(groupId)) {
                            SendMsgUtil.sendGroupMsg(groupId, content, userId);
                        }
                    });
                    continue;
                }
                // 第三种情况，都发先发私聊再发群聊
                String content = this.get4DrinkAIData(userDrinkRecordList, userId, userId2DrinkChatIdMap);
                if (activeUserIds.contains(userId)) {
                    SendMsgUtil.sendMsg(userId, content);
                }
                hasRecordGroupIdList.forEach(groupId -> {
                    if (activeUserIds.contains(groupId)) {
                        SendMsgUtil.sendGroupMsg(groupId, content, userId);
                    }
                });
            }
        }
    }

    private String getDrinkResult(List<BotDrinkRecord> recordList) {
        AtomicInteger all = new AtomicInteger();
        recordList.forEach(x -> all.addAndGet(x.getDrinkNumber()));
        BigDecimal result = new BigDecimal(all.get()).divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
        return String.format(BaseConsts.Drink.ALL_TITLE + StrUtil.CRLF + StrUtil.CRLF + BaseConsts.Drink.QUERY_ALL, recordList.size(), result);
    }

    private String get4DrinkAIData(List<BotDrinkRecord> recordList, String token, Map<String, String> token2DrunkChatIdMap) {
        StringBuilder stringBuilder = new StringBuilder();
        AtomicInteger all = new AtomicInteger();
        recordList.forEach(x -> {
            stringBuilder.append(String.format(BaseConsts.Drink.QUERY_RECORD, x.getDrinkTime().split(StrUtil.SPACE)[1], x.getDrinkNumber())).append(StrUtil.CRLF);
            all.addAndGet(x.getDrinkNumber());
        });
        BigDecimal result = new BigDecimal(all.get()).divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
        stringBuilder.append(StrUtil.CRLF).append(String.format(BaseConsts.Drink.QUERY_ALL, recordList.size(), result));

        String conversationId = token2DrunkChatIdMap.get(token) == null ? "" : token2DrunkChatIdMap.get(token);
        JSONObject json;
        try {
            json = JSONUtil.parseObj(HttpSenderUtil.postJsonDataWithToken(defaultUrl,
                    JSONUtil.toJsonStr(new DeepChatReq(new JSONObject(), stringBuilder.toString(), "blocking", conversationId, token)),
                    drinkToken));
        } catch (Exception e) {
            e.printStackTrace();
            return "喝水日报发送异常，请检查";
        }
        String conversation_id = json.getStr("conversation_id");
        if (conversation_id == null) {
            log.error("调用失败，返回内容：[{}]", JSONUtil.toJsonStr(json));
            return "喝水日报发送异常，请检查。";
        }
        BotUserConfigExample userConfigExample = new BotUserConfigExample();
        userConfigExample.createCriteria().andUserIdEqualTo(token);
        BotUserConfig botUserConfig = new BotUserConfig();
        botUserConfig.setDrinkChatId(conversation_id);
        userConfigMapper.updateByExampleSelective(botUserConfig, userConfigExample);
        return json.getStr("answer");
    }


}
