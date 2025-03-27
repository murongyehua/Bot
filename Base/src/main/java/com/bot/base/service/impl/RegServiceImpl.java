package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bot.base.service.BaseService;
import com.bot.base.service.RegService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRegDay;
import com.bot.common.enums.ENRegStatus;
import com.bot.common.enums.ENRegType;
import com.bot.common.util.SendMsgUtil;
import com.bot.game.dao.entity.BotUser;
import com.bot.game.dao.entity.BotUserConfig;
import com.bot.game.dao.entity.BotUserConfigExample;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import com.bot.game.dao.mapper.BotUserMapper;
import com.bot.game.service.SystemConfigHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class RegServiceImpl implements RegService {

    @Resource
    private BotUserMapper botUserMapper;

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @Resource
    private BotUserConfigMapper userConfigMapper;

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
        botUser.setDeadLineDate(DateUtil.offsetDay(new Date(), 7));
        botUserMapper.insert(botUser);
        BotUserConfig botUserConfig = new BotUserConfig();
        botUserConfig.setId(IdUtil.simpleUUID());
        botUserConfig.setUserId(activeId);
        userConfigMapper.insert(botUserConfig);
        systemConfigHolder.loadUsers();
        return String.format(BaseConsts.SystemManager.REG_SUCCESS, DateUtil.format(botUser.getDeadLineDate(), DatePattern.NORM_DATETIME_FORMAT));
    }

    @Override
    public String tryReg(String activeId, String inviteCode, ENRegType regType) {
        // 检查邀请码
        BotUserConfigExample userConfigExample = new BotUserConfigExample();
        userConfigExample.createCriteria().andInviteCodeEqualTo(inviteCode);
        List<BotUserConfig> botUserConfigs = userConfigMapper.selectByExample(userConfigExample);
        if (!SystemConfigCache.tempInviteCode.containsKey(inviteCode) && CollectionUtil.isEmpty(botUserConfigs)) {
            return BaseConsts.SystemManager.INVITE_CODE_ERROR;
        }
        // 是否是用户邀请
        boolean isUserInviteFlag = false;
        ENRegDay enRegDay = SystemConfigCache.tempInviteCode.get(inviteCode);
        if (enRegDay == null) {
            // 为空代表不是系统邀请码，是用户邀请的
            enRegDay = ENRegDay.MONTH;
            isUserInviteFlag = true;
        }
        // 先看之前是否用过
        if (SystemConfigCache.userDateMap.containsKey(activeId)) {
            // 用过并且是用户邀请，不能使用
            if (isUserInviteFlag) {
                return "此会话已经是小林的老用户了，无法使用该激活码开通服务呢，可以发送”生成邀请码”获取当前会话的邀请码，然后去邀请新用户来增加使用时长吧！";
            }
            // 用过 需要根据之前的过期时间来判断从哪个时间上加
            // 之前未到期，续期
            if (SystemConfigCache.userDateMap.get(activeId).after(new Date())) {
               BotUser botUser = new BotUser();
               botUser.setId(activeId);
               botUser.setStatus(ENRegStatus.FOREVER.getValue());
               botUser.setDeadLineDate(DateUtil.offsetDay(SystemConfigCache.userDateMap.get(activeId), enRegDay.getDayNumber()));
               botUserMapper.updateByPrimaryKeySelective(botUser);
               systemConfigHolder.loadUsers();
               return String.format(BaseConsts.SystemManager.REG_SUCCESS, DateUtil.format(botUser.getDeadLineDate(), DatePattern.NORM_DATETIME_FORMAT));
            }
            // 已到期，新开通
            BotUser botUser = new BotUser();
            botUser.setId(activeId);
            botUser.setStatus(ENRegStatus.FOREVER.getValue());
            botUser.setDeadLineDate(DateUtil.offsetDay(new Date(), enRegDay.getDayNumber()));
            botUserMapper.updateByPrimaryKeySelective(botUser);
            systemConfigHolder.loadUsers();
            return String.format(BaseConsts.SystemManager.REG_SUCCESS, DateUtil.format(botUser.getDeadLineDate(), DatePattern.NORM_DATETIME_FORMAT));
        }
        // 没用过 直接加
        BotUser botUser = new BotUser();
        botUser.setId(activeId);
        botUser.setStatus(ENRegStatus.FOREVER.getValue());
        botUser.setType(regType.getValue());
        botUser.setDeadLineDate(DateUtil.offsetDay(new Date(), enRegDay.getDayNumber()));
        botUserMapper.insert(botUser);
        BotUserConfig botUserConfig = new BotUserConfig();
        botUserConfig.setId(IdUtil.simpleUUID());
        botUserConfig.setUserId(activeId);
        userConfigMapper.insert(botUserConfig);
        systemConfigHolder.loadUsers();
        // 如果是用户邀请，还需要给邀请人加时长
        if (isUserInviteFlag) {
            BotUserConfig userConfig = botUserConfigs.get(0);
            // 邀请群聊要人数>5才给加
            if (activeId.contains("@")) {
                int number = SendMsgUtil.getChatRoomUserCount(activeId);
                if (number < 5) {
                    return String.format(BaseConsts.SystemManager.REG_SUCCESS, DateUtil.format(botUser.getDeadLineDate(), DatePattern.NORM_DATETIME_FORMAT));
                }
            }
            BotUser oldUser = new BotUser();
            oldUser.setId(userConfig.getUserId());
            oldUser.setStatus(ENRegStatus.FOREVER.getValue());
            oldUser.setDeadLineDate(DateUtil.offsetDay(SystemConfigCache.userDateMap.get(userConfig.getUserId()), enRegDay.getDayNumber()));
            botUserMapper.updateByPrimaryKeySelective(botUser);
            if (userConfig.getUserId().contains("@")) {
                SendMsgUtil.sendGroupMsg(userConfig.getUserId(), "恭喜您成功邀请一个新用户，有效期已延长，可发送“到期时间”来获取使用时长~", null);
            }else {
                SendMsgUtil.sendMsg(userConfig.getUserId(), "恭喜您成功邀请一个新用户，有效期已延长，可发送“到期时间”来获取使用时长~");
            }
        }
        systemConfigHolder.loadUsers();
        // 清除邀请码
        SystemConfigCache.tempInviteCode.remove(inviteCode);
        return String.format(BaseConsts.SystemManager.REG_SUCCESS, DateUtil.format(botUser.getDeadLineDate(), DatePattern.NORM_DATETIME_FORMAT));
    }

    @Override
    public String queryDeadLineDate(String token) {
        BotUser botUser = botUserMapper.selectByPrimaryKey(token);
        if (botUser == null) {
            return BaseConsts.SystemManager.OVER_TIME_TIP;
        }
        return DateUtil.format(botUser.getDeadLineDate(), DatePattern.NORM_DATETIME_PATTERN);
    }

    @Override
    public String createInviteCode(String token) {
        BotUserConfigExample configExample = new BotUserConfigExample();
        configExample.createCriteria().andUserIdEqualTo(token);
        List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(configExample);
        if (CollectionUtil.isEmpty(userConfigList)) {
            return "未使用过的会话无法生成邀请码，请先试用或激活";
        }
        BotUserConfig userConfig = userConfigList.get(0);
        String inviteCode = IdUtil.nanoId(8);
        userConfig.setInviteCode(inviteCode);
        userConfigMapper.updateByPrimaryKey(userConfig);
        return "您的专属邀请码是：" + inviteCode;
    }
}
