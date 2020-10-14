package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.PlayerAppellation;

public interface PlayerAppellationMapper {
    int deleteByPrimaryKey(String id);

    int insert(PlayerAppellation record);

    int insertSelective(PlayerAppellation record);

    PlayerAppellation selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PlayerAppellation record);

    int updateByPrimaryKey(PlayerAppellation record);
}