package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENChatEngine;
import com.bot.common.enums.ENRespType;
import com.bot.game.dao.entity.BotUserConfig;
import com.bot.game.dao.entity.BotUserConfigExample;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import com.bot.game.service.SystemConfigHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("changeChatEngineServiceImpl")
public class ChangeChatEngineServiceImpl implements BaseService {

    @Resource
    private BotUserConfigMapper userConfigMapper;

    @Resource
    private SystemConfigHolder systemConfigHolder;


    @Override
    @Deprecated
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        String content =  reqContent.replace(BaseConsts.Change.CHANGE, "").trim();
        String value = ENChatEngine.getValueByKeyWord(content);
        if (value == null) {
            this.insertOrUpdate(ENChatEngine.DEFAULT.getValue(), token);
            return new CommonResp(BaseConsts.Change.NO_ENGINE, ENRespType.TEXT.getType());
        }
        this.insertOrUpdate(value, token);
        return new CommonResp(BaseConsts.Change.CHANGE_SUCCESS_FORMAT, ENRespType.TEXT.getType());
    }

    private void insertOrUpdate(String engineValue, String token) {
        BotUserConfigExample userConfigExample = new BotUserConfigExample();
        userConfigExample.createCriteria().andUserIdEqualTo(token);
        List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
        BotUserConfig userConfig = new BotUserConfig();
        userConfig.setChatEngine(engineValue);
        userConfig.setUserId(token);
        if (CollectionUtil.isEmpty(userConfigList)) {
            userConfig.setId(IdUtil.simpleUUID());
            userConfigMapper.insertSelective(userConfig);
        }
        userConfigMapper.updateByExampleSelective(userConfig, userConfigExample);
        systemConfigHolder.loadUserConfig();
    }

}
