package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENChatEngine;
import com.bot.common.enums.ENRespType;
import com.bot.common.enums.ENYesOrNo;
import com.bot.common.util.ThreadPoolManager;
import com.bot.game.dao.entity.BotUserConfig;
import com.bot.game.dao.entity.BotUserConfigExample;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import com.bot.game.service.SystemConfigHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("workDailyServiceImpl")
public class WorkDailyServiceImpl implements BaseService {

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @Resource
    private BotUserConfigMapper userConfigMapper;



    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        switch (reqContent) {
            case BaseConsts.WorkDaily.ACTIVE_WORK_DAILY:
                insertOrUpdate(groupId == null ? token : groupId, DateUtil.format(DateUtil.yesterday(), DatePattern.NORM_DATE_PATTERN));
                systemConfigHolder.loadUserConfig();
                return new CommonResp(BaseConsts.WorkDaily.DAILY_ACTIVE_SUCCESS, ENRespType.TEXT.getType());
            case BaseConsts.WorkDaily.CLOSE_WORK_DAILY:
                insertOrUpdate(groupId == null ? token : groupId, null);
                systemConfigHolder.loadUserConfig();
                return new CommonResp(BaseConsts.WorkDaily.DAILY_CLOSE_SUCCESS, ENRespType.TEXT.getType());
            default:
                return null;
        }
    }

    private void insertOrUpdate(String token, String type) {
        BotUserConfigExample example = new BotUserConfigExample();
        example.createCriteria().andUserIdEqualTo(token);
        List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(example);
        if (CollectionUtil.isEmpty(userConfigList)) {
            BotUserConfig userConfig = new BotUserConfig();
            userConfig.setId(IdUtil.simpleUUID());
            userConfig.setUserId(token);
            userConfig.setChatEngine(ENChatEngine.DEFAULT.getValue());
            userConfigMapper.insert(userConfig);
            return;
        }
        BotUserConfig userConfig = userConfigList.get(0);
        userConfig.setWorkDailyConfig(type);
        userConfigMapper.updateByExample(userConfig, example);
    }
}
