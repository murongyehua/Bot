package com.bot.game.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.enums.ENChatEngine;
import com.bot.common.enums.ENSystemConfig;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.*;
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

    @Resource
    private BotBaseWordMapper botBaseWordMapper;

    @Resource
    private BotGameUserScoreMapper gameUserScoreMapper;

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
        SystemConfigCache.chatFrequency.clear();
        SystemConfigCache.chatFrequency.putAll(userConfigList.stream().filter(x -> StrUtil.isNotEmpty(x.getChatFrequency())).collect(Collectors.toMap(BotUserConfig::getUserId, BotUserConfig::getChatFrequency)));
        SystemConfigCache.openServer.clear();
        SystemConfigCache.openServer.addAll(userConfigList.stream().filter(x -> "1".equals(x.getJxOpenServer())).map(BotUserConfig::getUserId).collect(Collectors.toList()));
        SystemConfigCache.emojiUser.clear();
        SystemConfigCache.emojiUser.addAll(userConfigList.stream().filter(x -> "1".equals(x.getEmojiSwitch()) || x.getEmojiSwitch() == null).map(BotUserConfig::getUserId).collect(Collectors.toList()));
        SystemConfigCache.bottleUser.clear();
        SystemConfigCache.bottleUser.addAll(userConfigList.stream().filter(x -> "1".equals(x.getBottleAutoSwitch())).map(BotUserConfig::getUserId).collect(Collectors.toList()));
        SystemConfigCache.welcomeMap.clear();
        SystemConfigCache.welcomeMap.putAll(userConfigList.stream().filter(x -> StrUtil.isNotEmpty(x.getWelcomeContent())).collect(Collectors.toMap(BotUserConfig::getUserId, BotUserConfig::getWelcomeContent)));

        List<BotBaseWord> words = botBaseWordMapper.selectByExample(new BotBaseWordExample());
        SystemConfigCache.wordPrompt.clear();
        SystemConfigCache.wordPrompt.putAll(words.stream().filter(x -> StrUtil.isNotEmpty(x.getPrompt())).collect(Collectors.toMap(BotBaseWord::getWord, BotBaseWord::getPrompt)));

        List<BotGameUserScore> userScoreList = gameUserScoreMapper.selectByExample(new BotGameUserScoreExample());
        SystemConfigCache.userWordMap.clear();
        SystemConfigCache.userWordMap.putAll(userScoreList.stream().filter(x -> StrUtil.isNotEmpty(x.getCurrentWord())).collect(Collectors.toMap(BotGameUserScore::getUserId, BotGameUserScore::getCurrentWord)));

    }

}
