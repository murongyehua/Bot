package com.bot.game.service.impl;

import com.bot.common.constant.GameConsts;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.PlayerWeapon;
import com.bot.game.dao.mapper.GamePlayerMapper;

/**
 * @author murongyehua
 * @version 1.0 2020/11/4
 */
public class ChangeWeaponServiceImpl extends CommonPlayer {

    private PlayerWeapon playerWeapon;

    public ChangeWeaponServiceImpl(PlayerWeapon playerWeapon) {
        this.playerWeapon = playerWeapon;
        this.title = GameConsts.Weapon.CHANGE;
    }

    @Override
    public String doPlay(String token) {
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        gamePlayer.setPlayerWeaponId(playerWeapon.getId());
        gamePlayerMapper.updateByPrimaryKey(gamePlayer);
        return GameConsts.Weapon.SUCCESS;
    }



}
