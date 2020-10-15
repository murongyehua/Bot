package com.bot.game.service.impl;

import com.bot.commom.constant.GameConsts;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.mapper.GamePlayerMapper;

/**
 * @author liul
 * @version 1.0 2020/10/15
 */
public class ChangeAppellationServiceImpl extends CommonPlayer {

    public ChangeAppellationServiceImpl(String title) {
        this.title = title;
    }

    @Override
    public String doPlay(String token) {
        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setId(token);
        gamePlayer.setAppellation(this.title);
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        gamePlayerMapper.updateByPrimaryKeySelective(gamePlayer);
        return GameConsts.CommonTip.PLAY_SUCCESS;
    }

}
