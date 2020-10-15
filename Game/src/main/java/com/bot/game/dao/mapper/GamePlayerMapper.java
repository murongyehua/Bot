package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.GamePlayer;

import java.util.List;

public interface GamePlayerMapper {
    int deleteByPrimaryKey(String id);

    int insert(GamePlayer record);

    int insertSelective(GamePlayer record);

    GamePlayer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(GamePlayer record);

    int updateByPrimaryKey(GamePlayer record);

    List<GamePlayer> selectBySelective(GamePlayer gamePlayer);
}