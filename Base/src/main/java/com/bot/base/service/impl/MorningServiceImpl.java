package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENChatEngine;
import com.bot.common.enums.ENMorningType;
import com.bot.common.enums.ENRespType;
import com.bot.game.dao.entity.BotUserConfig;
import com.bot.game.dao.entity.BotUserConfigExample;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import com.bot.game.service.SystemConfigHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("morningServiceImpl")
public class MorningServiceImpl implements BaseService {

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @Resource
    private BotUserConfigMapper userConfigMapper;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token) {
        switch (reqContent) {
            case BaseConsts.Morning.PICK:
                this.insertOrUpdate(token, ENMorningType.ALL.getValue());
                systemConfigHolder.loadUserConfig();
                return new CommonResp(BaseConsts.Morning.PICK_SUCCESS, ENRespType.TEXT.getType());
            case BaseConsts.Morning.CANCEL:
                this.insertOrUpdate(token, StrUtil.EMPTY);
                systemConfigHolder.loadUserConfig();
                return new CommonResp(BaseConsts.Morning.CANCEL_SUCCESS, ENRespType.TEXT.getType());
            default:
                // 默认是切换
                String content = reqContent.replaceAll(BaseConsts.Morning.MORNING, StrUtil.EMPTY).trim();
                String[] contentArray = content.split(StrUtil.EMPTY);
                if (contentArray.length < 1) {
                    return new CommonResp(BaseConsts.Morning.CHANGE_FAIL, ENRespType.TEXT.getType());
                }
                StringBuilder typeContent = new StringBuilder();
                for (String type : contentArray) {
                    String value = ENMorningType.getValueByLabel(content);
                    if (value == null) {
                        return new CommonResp(BaseConsts.Morning.CHANGE_FAIL, ENRespType.TEXT.getType());
                    }
                    typeContent.append(value).append(StrUtil.COMMA);
                }
                this.insertOrUpdate(token, typeContent.substring(0, typeContent.length() - 1));
                systemConfigHolder.loadUserConfig();
                return new CommonResp(String.format(BaseConsts.Morning.CHANGE_SUCCESS_FORMAT, content), ENRespType.TEXT.getType());
        }
    }

    private void insertOrUpdate(String token, String type) {
        BotUserConfigExample example = new BotUserConfigExample();
        example.createCriteria().andUserIdEqualTo(token);
        List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(example);
        BotUserConfig userConfig = new BotUserConfig();
        userConfig.setUserId(token);
        userConfig.setMorningType(type);
        if (CollectionUtil.isEmpty(userConfigList)) {
            userConfig.setId(IdUtil.simpleUUID());
            userConfig.setChatEngine(ENChatEngine.DEFAULT.getValue());
            userConfigMapper.insert(userConfig);
            return;
        }
        userConfigMapper.updateByExampleSelective(userConfig, example);
    }
}
