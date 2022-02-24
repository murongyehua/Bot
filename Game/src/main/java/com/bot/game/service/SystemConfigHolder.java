package com.bot.game.service;

import com.bot.common.config.SystemConfigCache;
import com.bot.common.enums.ENSystemConfig;
import com.bot.game.dao.entity.SystemConfig;
import com.bot.game.dao.entity.SystemConfigExample;
import com.bot.game.dao.mapper.SystemConfigMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SystemConfigHolder {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @PostConstruct
    public void init() {
        Map<String, String> configMap = systemConfigMapper.selectByExample(new SystemConfigExample())
                .stream().collect(Collectors.toMap(SystemConfig::getConfigType, SystemConfig::getConfigValue));
        SystemConfigCache.baseUrl = configMap.get(ENSystemConfig.BASE_URL.getValue());
        SystemConfigCache.wId = configMap.get(ENSystemConfig.WID.getValue());
        SystemConfigCache.token = configMap.get(ENSystemConfig.TOKEN.getValue());
        SystemConfigCache.inviteCode = configMap.get(ENSystemConfig.INVITE_CODE.getValue());
    }

}
