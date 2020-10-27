package com.bot.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.dao.entity.BaseMonster;
import com.bot.game.dao.entity.PlayerPhantom;

/**
 * @author liul
 * @version 1.0 2020/10/27
 */
public class FriendCompareServiceImpl extends CommonPlayer {

    private PlayerPhantom playerPhantom;

    private PlayerPhantom friendPhantom;

    public FriendCompareServiceImpl(PlayerPhantom playerPhantom, PlayerPhantom friendPhantom) {
        this.title = String.format(GameConsts.Battle.TITLE,
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel());
        this.playerPhantom = playerPhantom;
        this.friendPhantom = friendPhantom;
    }

    @Override
    public String doPlay(String token) {
        BaseMonster baseMonster = new BaseMonster();
        BeanUtil.copyProperties(friendPhantom, baseMonster);
        BattleServiceImpl battleService = new BattleServiceImpl(baseMonster, playerPhantom, true);
        return battleService.doPlay(token);
    }
}
