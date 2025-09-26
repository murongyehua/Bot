package com.bot.game.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.enums.ENChatEngine;
import com.bot.common.enums.ENSystemConfig;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import com.bot.game.dao.mapper.BotUserMapper;
import com.bot.game.dao.mapper.SystemConfigMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SystemConfigHolder {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Resource
    private BotUserMapper botUserMapper;

    @Resource
    private BotUserConfigMapper botUserConfigMapper;

    @PostConstruct
    public void init() {
        Map<String, String> configMap = systemConfigMapper.selectByExample(new SystemConfigExample())
                .stream().collect(Collectors.toMap(SystemConfig::getConfigType, SystemConfig::getConfigValue));
        SystemConfigCache.baseUrl = configMap.get(ENSystemConfig.BASE_URL.getValue());
        SystemConfigCache.wId = configMap.get(ENSystemConfig.WID.getValue());
        SystemConfigCache.token = configMap.get(ENSystemConfig.TOKEN.getValue());
        SystemConfigCache.inviteCode = configMap.get(ENSystemConfig.INVITE_CODE.getValue());
        SystemConfigCache.topToken = Arrays.asList(configMap.get(ENSystemConfig.TOP_TOKEN.getValue()).split(","));
        SystemConfigCache.signToken = Arrays.asList(configMap.get(ENSystemConfig.SIGN_TOKEN.getValue()).split(","));
        SystemConfigCache.isMaintenance = configMap.get(ENSystemConfig.MAINTENANCE_FLAG.getValue());
        SystemConfigCache.tuilanToken = configMap.get(ENSystemConfig.TUILAN_TOKEN.getValue());
        this.loadUsers();
        this.loadUserConfig();
    }

    public void loadUsers() {
        List<BotUser> userList = botUserMapper.selectByExample(new BotUserExample());
        if(CollectionUtil.isEmpty(userList)) {
            return;
        }
        SystemConfigCache.userDateMap.clear();
        SystemConfigCache.userDateMap.putAll(userList.stream().collect(Collectors.toMap(BotUser::getId, BotUser::getDeadLineDate)));
    }

    public void loadUserConfig() {
        List<BotUserConfig> userConfigList = botUserConfigMapper.selectByExample(new BotUserConfigExample());
        if (CollectionUtil.isEmpty(userConfigList)) {
            return;
        }
        SystemConfigCache.userChatEngine.clear();
        SystemConfigCache.userChatEngine.putAll(userConfigList.stream().collect(Collectors.toMap(BotUserConfig::getUserId, x -> ENChatEngine.getByValue(x.getChatEngine()))));
        SystemConfigCache.userMorningMap.clear();
        SystemConfigCache.userMorningMap.putAll(userConfigList.stream().filter(x -> StrUtil.isNotEmpty(x.getMorningType())).collect(Collectors.toMap(BotUserConfig::getUserId, BotUserConfig::getMorningType)));
        SystemConfigCache.userWorkDaily.clear();
        SystemConfigCache.userWorkDaily.addAll(userConfigList.stream().filter(x -> StrUtil.isNotEmpty(x.getWorkDailyConfig())).map(BotUserConfig::getUserId).collect(Collectors.toList()));
    }

}
