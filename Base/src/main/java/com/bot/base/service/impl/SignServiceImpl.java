package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import com.bot.common.enums.ENStatus;
import com.bot.common.enums.ENUserGoodType;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.BotUserBoxMapper;
import com.bot.game.dao.mapper.BotUserMapper;
import com.bot.game.dao.mapper.BotUserSignMapper;
import com.bot.game.dao.mapper.UserBindMapper;
import com.bot.game.enums.ENMessageType;
import com.bot.game.service.SystemConfigHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service("signServiceImpl")
public class SignServiceImpl implements BaseService {

    @Resource
    private BotUserMapper userMapper;

    @Resource
    private BotUserSignMapper userSignMapper;

    @Resource
    private BotUserBoxMapper userBoxMapper;

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @Resource
    private UserBindMapper userBindMapper;

    @Resource
    private UserBindServiceImpl userBindService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (ObjectUtil.equals("签到", reqContent)) {
            // 先看有没有绑定账号，有的话取多的一边作为此次签到的token
            UserBindExample userBindExample = new UserBindExample();
            if ("qq".equals(channel)) {
                userBindExample.createCriteria().andQqUserTokenEqualTo(token);
            }else {
                userBindExample.createCriteria().andWxUserTokenEqualTo(token);
            }
            List<UserBind> userBindList = userBindMapper.selectByExample(userBindExample);
            if (CollectionUtil.isNotEmpty(userBindList)) {
                UserBind userBind = userBindList.get(0);
                BotUserBoxExample userBoxExample = new BotUserBoxExample();
                userBoxExample.createCriteria().andGoodTypeEqualTo(ENUserGoodType.MONEY.getValue()).andUserIdEqualTo(userBind.getQqUserToken());
                int qqMoney = userBoxMapper.selectByExample(userBoxExample).get(0).getNumber();
                userBoxExample.clear();
                userBoxExample.createCriteria().andGoodTypeEqualTo(ENUserGoodType.MONEY.getValue()).andUserIdEqualTo(userBind.getWxUserToken());
                int wxMoney = userBoxMapper.selectByExample(userBoxExample).get(0).getNumber();
                if (wxMoney > qqMoney) {
                    token = userBind.getWxUserToken();
                }else {
                    token = userBind.getQqUserToken();
                }
            }
            BotUserSignExample signExample = new BotUserSignExample();
            signExample.createCriteria().andSignDateEqualTo(DateUtil.today()).andUserIdEqualTo(token);
            int todayCount = userSignMapper.countByExample(signExample);
            if (todayCount > 0) {
                return new CommonResp(BaseConsts.Sign.SIGN_FAIL, ENRespType.TEXT.getType());
            }
            // 今天没签到 先看连签天数
            // 昨天没签到 则今天是第一天 昨天签到了 则取记录的连签天数+1
            signExample.clear();
            signExample.createCriteria()
                    .andSignDateEqualTo(DateUtil.format(DateUtil.yesterday(), DatePattern.NORM_DATE_FORMAT))
                    .andUserIdEqualTo(token);
            int yesterdayCount = userSignMapper.countByExample(signExample);
            if (yesterdayCount > 0) {
                BotUser user = userMapper.selectByPrimaryKey(token);
                int signDay = user.getSignDay() + 1;
                // 第一天1 第二天2 后面每天都是3
                int money = signDay == 1 ? 1 : signDay == 2 ? 2 : 3;
                this.signSuccess(token, money);
                // 更新连签天数
                user.setSignDay(signDay);
                userMapper.updateByPrimaryKey(user);
                return new CommonResp(String.format(BaseConsts.Sign.SIGN_TIP, signDay, money), ENRespType.TEXT.getType());
            }
            // 昨天没签到，有可能是首次签到
            BotUser user = userMapper.selectByPrimaryKey(token);
            if (user == null) {
                // 没记录，首次注册，过期时间设置成现在
                user = new BotUser();
                user.setSignDay(1);
                user.setId(token);
                user.setStatus(ENStatus.NORMAL.getValue());
                user.setType("wx".equals(channel) ? "1" : "3");
                user.setDeadLineDate(DateUtil.date());
                userMapper.insert(user);
                this.signSuccess(token, 1);
                systemConfigHolder.loadUsers();
                return new CommonResp(String.format(BaseConsts.Sign.SIGN_TIP, 1, 1), ENRespType.TEXT.getType());
            }else {
                // 有记录，连签重置为1
                this.signSuccess(token, 1);
                user.setSignDay(1);
                userMapper.updateByPrimaryKey(user);
                return new CommonResp(String.format(BaseConsts.Sign.SIGN_TIP, 1, 1), ENRespType.TEXT.getType());
            }
        }
        if (reqContent.startsWith("跨平台绑定")) {
            String[] reqs = reqContent.split(StrUtil.SPACE);
            if (reqs.length != 2) {
                return new CommonResp("跨平台绑定账号指定错误，请检查", ENRespType.TEXT.getType());
            }
            if ("qq".equals(channel)) {
                return userBindService.bindUser(token, reqs[1]);
            }else {
                return userBindService.bindUser(reqs[1], token);
            }
        }
        return null;
    }

    /**
     * 签到成功 添加签到记录 添加碎玉
     */
    private void signSuccess(String token, int money) {
        BotUserSign userSign = new BotUserSign();
        userSign.setId(IdUtil.simpleUUID());
        userSign.setSignDate(DateUtil.today());
        userSign.setUserId(token);
        userSignMapper.insert(userSign);

        BotUserBoxExample userBoxExample = new BotUserBoxExample();
        userBoxExample.createCriteria().andUserIdEqualTo(token).andGoodTypeEqualTo(ENUserGoodType.MONEY.getValue());
        List<BotUserBox> userBoxList = userBoxMapper.selectByExample(userBoxExample);
        if (CollectionUtil.isEmpty(userBoxList)) {
            // 新增
            BotUserBox userBox = new BotUserBox();
            userBox.setUserId(token);
            userBox.setId(IdUtil.simpleUUID());
            userBox.setGoodType(ENUserGoodType.MONEY.getValue());
            userBox.setNumber(money);
            userBoxMapper.insert(userBox);
            return;
        }
        // 修改
        BotUserBox userBox = userBoxList.get(0);
        userBox.setNumber(userBox.getNumber() + money);
        userBoxMapper.updateByPrimaryKey(userBox);
    }


}
