package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.PlayerPhantom;

public interface PlayerPhantomMapper {
    int deleteByPrimaryKey(String id);

    int insert(PlayerPhantom record);

    int insertSelective(PlayerPhantom record);

    PlayerPhantom selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PlayerPhantom record);

    int updateByPrimaryKey(PlayerPhantom record);
}