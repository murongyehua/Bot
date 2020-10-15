package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.Game;

import java.util.List;

public interface GameMapper {
    int deleteByPrimaryKey(String id);

    int insert(Game record);

    int insertSelective(Game record);

    Game selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Game record);

    int updateByPrimaryKey(Game record);

    List<Game> selectAll();
}