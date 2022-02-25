package com.bot.base.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.service.RegService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRegStatus;
import com.bot.common.enums.ENRegType;
import com.bot.game.dao.entity.BotUser;
import com.bot.game.dao.mapper.BotUserMapper;
import com.bot.game.service.SystemConfigHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class RegServiceImpl implements RegService {

    @Resource
    private BotUserMapper botUserMapper;

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @Override
    public String tryTempReg(String activeId, String inviteCode, ENRegType regType) {
        // 先看是不是已经试用过
        if (SystemConfigCache.userDateMap.containsKey(activeId)) {
            return BaseConsts.SystemManager.REPEAT_TEMP_REG_TIP;
        }
        // 检查邀请码
        if (ObjectUtil.notEqual(inviteCode, SystemConfigCache.inviteCode)) {
            return BaseConsts.SystemManager.INVITE_CODE_ERROR;
        }
        BotUser botUser = new BotUser();
        botUser.setId(activeId);
        botUser.setStatus(ENRegStatus.TEMP.getValue());
        botUser.setType(regType.getValue());
        botUser.setDeadLineDate(DateUtil.offsetDay(new Date(), 1));
        botUserMapper.insert(botUser);
        systemConfigHolder.loadUsers();
        return String.format(BaseConsts.SystemManager.REG_SUCCESS, DateUtil.format(botUser.getDeadLineDate(), DatePattern.NORM_DATETIME_FORMAT));
    }

    @Override
    public String tryReg(String activeId, String inviteCode, ENRegType regType) {
        // 检查邀请码
        if (StrUtil.isEmpty(SystemConfigCache.tempInviteCode)) {
            return BaseConsts.SystemManager.INVITE_CODE_ERROR;
        }
        if (ObjectUtil.notEqual(SystemConfigCache.tempInviteCode, inviteCode)) {
            return BaseConsts.SystemManager.INVITE_CODE_ERROR;
        }
        // 清除邀请码
        SystemConfigCache.tempInviteCode = "";
        // 先看之前是否用过
        if (SystemConfigCache.userDateMap.containsKey(activeId)) {
            // 用过 需要根据之前的过期时间来判断从哪个时间上加
            // 之前未到期，续期
            if (SystemConfigCache.userDateMap.get(activeId).after(new Date())) {
               BotUser botUser = new BotUser();
               botUser.setId(activeId);
               botUser.setStatus(ENRegStatus.FOREVER.getValue());
               botUser.setDeadLineDate(DateUtil.offsetDay(SystemConfigCache.userDateMap.get(activeId), 30));
               botUserMapper.updateByPrimaryKeySelective(botUser);
               systemConfigHolder.loadUsers();
               return String.format(BaseConsts.SystemManager.REG_SUCCESS, DateUtil.format(botUser.getDeadLineDate(), DatePattern.NORM_DATETIME_FORMAT));
            }
            // 已到期，新开通
            BotUser botUser = new BotUser();
            botUser.setId(activeId);
            botUser.setStatus(ENRegStatus.FOREVER.getValue());
            botUser.setDeadLineDate(DateUtil.offsetDay(new Date(), 30));
            botUserMapper.updateByPrimaryKeySelective(botUser);
            systemConfigHolder.loadUsers();
            return String.format(BaseConsts.SystemManager.REG_SUCCESS, DateUtil.format(botUser.getDeadLineDate(), DatePattern.NORM_DATETIME_FORMAT));
        }
        // 没用过 直接加
        BotUser botUser = new BotUser();
        botUser.setId(activeId);
        botUser.setStatus(ENRegStatus.FOREVER.getValue());
        botUser.setType(regType.getValue());
        botUser.setDeadLineDate(DateUtil.offsetDay(new Date(), 30));
        botUserMapper.insert(botUser);
        systemConfigHolder.loadUsers();
        return String.format(BaseConsts.SystemManager.REG_SUCCESS, DateUtil.format(botUser.getDeadLineDate(), DatePattern.NORM_DATETIME_FORMAT));
    }
}
