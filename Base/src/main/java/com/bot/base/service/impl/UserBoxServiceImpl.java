package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.enums.ENRegStatus;
import com.bot.common.enums.ENRegType;
import com.bot.common.enums.ENRespType;
import com.bot.common.enums.ENUserGoodType;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.BotUserBoxMapper;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import com.bot.game.dao.mapper.BotUserMapper;
import com.bot.game.dao.mapper.UserBindMapper;
import com.bot.game.service.SystemConfigHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service("userBoxServiceImpl")
public class UserBoxServiceImpl implements BaseService {

    @Resource
    private BotUserBoxMapper userBoxMapper;

    @Resource
    private BotUserConfigMapper userConfigMapper;

    @Resource
    private BotUserMapper userMapper;

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @Value("${sign.group.url}")
    private String signGroup;

    @Value("${add.bot.url}")
    private String addBot;

    @Resource
    private UserBindMapper userBindMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (ObjectUtil.equals("签到", reqContent)) {
            return new CommonResp("只有签到群才能进行签到哦~", ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("签到群", reqContent)) {
            return new CommonResp(signGroup, ENRespType.IMG.getType());
        }
        if (ObjectUtil.equals("引入小林", reqContent) || ObjectUtil.equals("引入", reqContent)) {
            return new CommonResp(addBot, ENRespType.IMG.getType());
        }
        if (ObjectUtil.equals("我的背包", reqContent)) {
            BotUserBoxExample userBoxExample = new BotUserBoxExample();
            // 这里不查碎玉，碎玉根据是否绑定了跨平台账号单独查
            userBoxExample.createCriteria().andUserIdEqualTo(token).andGoodTypeNotEqualTo(ENUserGoodType.MONEY.getValue());
            List<BotUserBox> userBoxList = userBoxMapper.selectByExample(userBoxExample);
            // 查碎玉
            BotUserBox moneyBox = this.getMoney(token, channel);
            if (moneyBox != null) {
                userBoxList.add(moneyBox);
            }
            if (CollectionUtil.isEmpty(userBoxList)) {
                return new CommonResp("你的背包是空的~", ENRespType.TEXT.getType());
            }
            List<String> boxList = new ArrayList<>();
            for (BotUserBox userBox : userBoxList) {
                String boxBuilder = ENUserGoodType.getLabelByValue(userBox.getGoodType()) +
                        " x " + userBox.getNumber();
                boxList.add(boxBuilder);
            }
            return new CommonResp(CollectionUtil.join(boxList, "\r\n"), ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("碎玉商店", reqContent)) {
            if ("wx".equals(channel)) {
                String wxShop = "碎玉商店\r\n个人资格    6碎玉\r\n群聊资格    18碎玉\r\n\r\n发送“兑换”+空格+物品名称可使用碎玉兑换";
                return new CommonResp(wxShop, ENRespType.TEXT.getType());
            }else {
                String qqShop = "碎玉商店\r\n群聊资格    30碎玉\r\n\r\n发送“兑换”+空格+物品名称可使用碎玉兑换";
                return new CommonResp(qqShop, ENRespType.TEXT.getType());
            }

        }
        if (ObjectUtil.equals("兑换 个人资格", reqContent)) {
            if ("qq".equals(channel)) {
                return new CommonResp("QQ暂不支持兑换个人资格，如有需要请至微信使用", ENRespType.TEXT.getType());
            }
            BotUserBox userBox = this.getMoney(token, channel);
            if (userBox == null) {
                return new CommonResp("碎玉不足，兑换失败", ENRespType.TEXT.getType());
            }
            if (userBox.getNumber() < 6) {
                return new CommonResp("碎玉不足，兑换失败", ENRespType.TEXT.getType());
            }
            userBox.setNumber(userBox.getNumber() - 6);
            userBoxMapper.updateByPrimaryKey(userBox);
            BotUserBoxExample userBoxExample = new BotUserBoxExample();
            userBoxExample.createCriteria().andGoodTypeEqualTo(ENUserGoodType.PERSONAL.getValue()).andUserIdEqualTo(token);
            List<BotUserBox> userBoxList1 = userBoxMapper.selectByExample(userBoxExample);
            if (CollectionUtil.isEmpty(userBoxList1)) {
                // 新增
                BotUserBox userBox1 = new BotUserBox();
                userBox1.setUserId(token);
                userBox1.setId(IdUtil.simpleUUID());
                userBox1.setNumber(1);
                userBox1.setGoodType(ENUserGoodType.PERSONAL.getValue());
                userBoxMapper.insert(userBox1);
                return new CommonResp("兑换成功，可到需要使用的聊天界面使用。", ENRespType.TEXT.getType());
            }
            // 修改
            BotUserBox userBox1 = userBoxList1.get(0);
            userBox1.setNumber(userBox1.getNumber() + 1);
            userBoxMapper.updateByPrimaryKey(userBox1);
            return new CommonResp("兑换成功，可到需要使用的聊天界面使用。", ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("兑换 群聊资格", reqContent)) {
            BotUserBox userBox = this.getMoney(token, channel);
            if (userBox == null) {
                return new CommonResp("碎玉不足，兑换失败", ENRespType.TEXT.getType());
            }
            if ("wx".equals(channel)) {
                if (userBox.getNumber() < 18) {
                    return new CommonResp("碎玉不足，兑换失败", ENRespType.TEXT.getType());
                }
                userBox.setNumber(userBox.getNumber() - 18);
            }else {
                if (userBox.getNumber() < 30) {
                    return new CommonResp("碎玉不足，兑换失败", ENRespType.TEXT.getType());
                }
                userBox.setNumber(userBox.getNumber() - 30);
            }
            userBoxMapper.updateByPrimaryKey(userBox);
            BotUserBoxExample userBoxExample = new BotUserBoxExample();
            userBoxExample.clear();
            userBoxExample.createCriteria().andGoodTypeEqualTo(ENUserGoodType.GROUP.getValue()).andUserIdEqualTo(token);
            List<BotUserBox> userBoxList1 = userBoxMapper.selectByExample(userBoxExample);
            if (CollectionUtil.isEmpty(userBoxList1)) {
                // 新增
                BotUserBox userBox1 = new BotUserBox();
                userBox1.setUserId(token);
                userBox1.setId(IdUtil.simpleUUID());
                userBox1.setNumber(1);
                userBox1.setGoodType(ENUserGoodType.GROUP.getValue());
                userBoxMapper.insert(userBox1);
                return new CommonResp("兑换成功，可到需要使用的聊天界面使用。", ENRespType.TEXT.getType());
            }
            // 修改
            BotUserBox userBox1 = userBoxList1.get(0);
            userBox1.setNumber(userBox1.getNumber() + 1);
            userBoxMapper.updateByPrimaryKey(userBox1);
            return new CommonResp("兑换成功，可到需要使用的聊天界面使用。", ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("使用 个人资格", reqContent)) {
            BotUserBoxExample userBoxExample = new BotUserBoxExample();
            userBoxExample.createCriteria().andUserIdEqualTo(token).andGoodTypeEqualTo(ENUserGoodType.PERSONAL.getValue());
            List<BotUserBox> userBoxList = userBoxMapper.selectByExample(userBoxExample);
            if (CollectionUtil.isEmpty(userBoxList)) {
                return new CommonResp("没有可用资格，使用失败", ENRespType.TEXT.getType());
            }
            BotUserBox userBox = userBoxList.get(0);
            if (userBox.getNumber() < 1) {
                return new CommonResp("没有可用资格，使用失败", ENRespType.TEXT.getType());
            }
            if (userBox.getNumber() == 1) {
                userBoxMapper.deleteByPrimaryKey(userBox.getId());
            }else {
                userBox.setNumber(userBox.getNumber() - 1);
                userBoxMapper.updateByPrimaryKey(userBox);
            }
            this.reg(token, ENRegType.PERSONNEL.getValue());
            return new CommonResp("使用成功，有效期延长30天！可以发送“到期时间”查询~", ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("使用 群聊资格", reqContent)) {
            log.info("1---" + token + "----" + groupId + "----" + channel);
            if (groupId == null) {
                return new CommonResp("请在需要使用群聊资格的群内进行操作", ENRespType.TEXT.getType());
            }
            BotUserBoxExample userBoxExample = new BotUserBoxExample();
            userBoxExample.createCriteria().andUserIdEqualTo(token).andGoodTypeEqualTo(ENUserGoodType.GROUP.getValue());
            List<BotUserBox> userBoxList = userBoxMapper.selectByExample(userBoxExample);
            log.info("2---" + token + "----" + groupId + "----" + channel);
            if (CollectionUtil.isEmpty(userBoxList)) {
                return new CommonResp("没有可用资格，使用失败", ENRespType.TEXT.getType());
            }
            BotUserBox userBox = userBoxList.get(0);
            if (userBox.getNumber() < 1) {
                return new CommonResp("没有可用资格，使用失败", ENRespType.TEXT.getType());
            }
            log.info("3---" + token + "----" + groupId + "----" + channel);
            if (userBox.getNumber() == 1) {
                userBoxMapper.deleteByPrimaryKey(userBox.getId());
            }else {
                userBox.setNumber(userBox.getNumber() - 1);
                userBoxMapper.updateByPrimaryKey(userBox);
            }
            log.info("4---" + token + "----" + groupId + "----" + channel);
            this.reg(groupId, ENRegType.GROUP.getValue());
            return new CommonResp("使用成功，有效期延长30天！可以发送“到期时间”查询~", ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("嘻嘻哈哈乌拉乌拉", reqContent)) {
            if (groupId != null) {
                this.reg(groupId, ENRegType.GROUP.getValue());
            }else {
                this.reg(token, ENRegType.PERSONNEL.getValue());
            }
            return new CommonResp("暗号正确，有效期延长90天！可以发送“到期时间”查询。", ENRespType.TEXT.getType());
        }

        if (reqContent.startsWith("设置回复频率")) {
            String[] split = reqContent.split(StrUtil.SPACE);
            if (split.length != 2) {
                return new CommonResp("格式错误，请按照格式发送“设置回复频率 0-1的两位小数”", ENRespType.TEXT.getType());
            }
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId != null ? groupId : token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setChatFrequency(split[1]);
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("设置成功，回复频率为：" + split[1], ENRespType.TEXT.getType());
        }

        if (reqContent.equals("禁止表情包")) {
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId != null ? groupId : token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setEmojiSwitch("0");
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("设置成功，将不再收集后续的表情包，也不会回复表情包。", ENRespType.TEXT.getType());
        }
        if (reqContent.equals("开启表情包")) {
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId != null ? groupId : token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setEmojiSwitch("1");
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("设置成功，将随机回复表情包，也会收集后续的表情包", ENRespType.TEXT.getType());
        }
        if (reqContent.equals("开启漂流瓶推送")) {
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId != null ? groupId : token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setBottleAutoSwitch("1");
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("开启成功。", ENRespType.TEXT.getType());
        }

        if (reqContent.equals("关闭漂流瓶推送")) {
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId != null ? groupId : token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setBottleAutoSwitch("0");
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("关闭成功。", ENRespType.TEXT.getType());
        }
        if(reqContent.startsWith("设置欢迎语")) {
            if (groupId == null) {
                return new CommonResp("请在群聊内使用该指令。", ENRespType.TEXT.getType());
            }
            String content = reqContent.replaceFirst("设置欢迎语 ", "");
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setWelcomeContent(content);
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("设置成功。", ENRespType.TEXT.getType());
        }
        return null;
    }

    private void reg(String token, String regType) {
        if (SystemConfigCache.userDateMap.containsKey(token)) {
            log.info("5---" + token + "----" + regType);
            // 用过 需要根据之前的过期时间来判断从哪个时间上加
            // 之前未到期，续期
            if (SystemConfigCache.userDateMap.get(token).after(new Date())) {
                BotUser botUser = new BotUser();
                botUser.setId(token);
                botUser.setStatus(ENRegStatus.FOREVER.getValue());
                botUser.setDeadLineDate(DateUtil.offsetDay(SystemConfigCache.userDateMap.get(token), 90));
                userMapper.updateByPrimaryKeySelective(botUser);
                systemConfigHolder.loadUsers();
                return;
            }
            // 已到期，新开通
            BotUser botUser = new BotUser();
            botUser.setId(token);
            botUser.setStatus(ENRegStatus.FOREVER.getValue());
            botUser.setDeadLineDate(DateUtil.offsetDay(new Date(), 90));
            userMapper.updateByPrimaryKeySelective(botUser);
            systemConfigHolder.loadUsers();
            return;
        }
        log.info("6---" + token + "----" + regType);
        // 没用过 直接加
        BotUser botUser = new BotUser();
        botUser.setSignDay(0);
        botUser.setId(token);
        botUser.setStatus(ENRegStatus.FOREVER.getValue());
        botUser.setType(regType);
        botUser.setDeadLineDate(DateUtil.offsetDay(new Date(), 90));
        userMapper.insert(botUser);
        log.info("7---" + token + "----" + regType);
        BotUserConfig botUserConfig = new BotUserConfig();
        botUserConfig.setId(IdUtil.simpleUUID());
        botUserConfig.setUserId(token);
        userConfigMapper.insert(botUserConfig);
        log.info("8---" + token + "----" + regType);
        systemConfigHolder.loadUsers();
        log.info("9---" + token + "----" + regType);
    }

    private BotUserBox getMoney(String token, String channel) {
        UserBindExample userBindExample = new UserBindExample();
        if ("qq".equals(channel)) {
            userBindExample.createCriteria().andQqUserTokenEqualTo(token);
        }else {
            userBindExample.createCriteria().andWxUserTokenEqualTo(token);
        }
        List<UserBind> userBindList = userBindMapper.selectByExample(userBindExample);
        BotUserBoxExample userBoxExample = new BotUserBoxExample();
        if (CollectionUtil.isNotEmpty(userBindList)) {
            UserBind userBind = userBindList.get(0);
            userBoxExample.createCriteria().andGoodTypeEqualTo(ENUserGoodType.MONEY.getValue()).andUserIdEqualTo(userBind.getQqUserToken());
            BotUserBox wxBox = userBoxMapper.selectByExample(userBoxExample).get(0);
            userBoxExample.clear();
            userBoxExample.createCriteria().andGoodTypeEqualTo(ENUserGoodType.MONEY.getValue()).andUserIdEqualTo(userBind.getWxUserToken());
            BotUserBox qqBox = userBoxMapper.selectByExample(userBoxExample).get(0);
            if (wxBox.getNumber() > qqBox.getNumber()) {
                return wxBox;
            }else {
                return qqBox;
            }
        }
        userBoxExample.createCriteria().andUserIdEqualTo(token).andGoodTypeEqualTo(ENUserGoodType.MONEY.getValue());
        List<BotUserBox> userBoxList = userBoxMapper.selectByExample(userBoxExample);
        if (CollectionUtil.isEmpty(userBoxList)) {
            return null;
        }
        return userBoxList.get(0);
    }

}
