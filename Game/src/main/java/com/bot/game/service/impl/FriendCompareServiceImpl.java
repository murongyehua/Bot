package com.bot.game.service.impl;

import com.bot.common.constant.GameConsts;
import com.bot.game.dao.entity.PlayerPhantom;

/**
 * @author murongyehua
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
        BattleServiceImpl battleService = new BattleServiceImpl(friendPhantom, playerPhantom, true, false);
        return battleService.doPlay(token);
    }
}
