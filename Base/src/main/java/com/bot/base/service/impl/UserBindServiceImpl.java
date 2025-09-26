package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.base.dto.CommonResp;
import com.bot.common.enums.ENRespType;
import com.bot.common.enums.ENUserGoodType;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.BotUserBoxMapper;
import com.bot.game.dao.mapper.BotUserMapper;
import com.bot.game.dao.mapper.UserBindMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserBindServiceImpl {

    @Resource
    private BotUserMapper userMapper;

    @Resource
    private UserBindMapper userBindMapper;

    @Transactional(rollbackFor = Exception.class)
    public CommonResp bindUser(String qqToken, String wxToken) {
        // 先查有没有绑过
        UserBindExample userBindExample = new UserBindExample();
        userBindExample.createCriteria().andWxUserTokenEqualTo(wxToken);
        int wxBindCount = userBindMapper.countByExample(userBindExample);
        if (wxBindCount > 0) {
            return new CommonResp("此次绑定的微信已经有过绑定记录，无法再次绑定。", ENRespType.TEXT.getType());
        }
        userBindExample.clear();
        userBindExample.createCriteria().andQqUserTokenEqualTo(qqToken);
        int qqBindCount = userBindMapper.countByExample(userBindExample);
        if (qqBindCount > 0) {
            return new CommonResp("此次绑定的QQ已经有过绑定记录，无法再次绑定。", ENRespType.TEXT.getType());
        }
        // 检查是否存在
        BotUserExample userExample = new BotUserExample();
        userExample.createCriteria().andTypeEqualTo("1").andIdEqualTo(wxToken);
        int wxCount = userMapper.countByExample(userExample);
        if (wxCount == 0) {
            return new CommonResp("此次绑定的微信用户不存在，请先在对应平台的签到群签到一次后再绑定。", ENRespType.TEXT.getType());
        }
        userExample.clear();
        userExample.createCriteria().andTypeEqualTo("3").andIdEqualTo(qqToken);
        int qqCount = userMapper.countByExample(userExample);
        if (qqCount == 0) {
            return new CommonResp("此次绑定的QQ用户不存在，请先在对应平台的签到群签到一次后再绑定。", ENRespType.TEXT.getType());
        }
        // 绑定
        UserBind userBind = new UserBind();
        userBind.setQqUserToken(qqToken);
        userBind.setWxUserToken(wxToken);
        userBindMapper.insert(userBind);
        return new CommonResp("恭喜绑定成功，从此两边碎玉可以共享了！\r\n（绑定成功后碎玉以较多的一边为准，仅碎玉共享，资格不共享，连续签到天数不共享）", ENRespType.TEXT.getType());
    }

}
