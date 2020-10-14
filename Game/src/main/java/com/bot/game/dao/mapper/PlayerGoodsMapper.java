package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.PlayerGoods;

public interface PlayerGoodsMapper {
    int deleteByPrimaryKey(String id);

    int insert(PlayerGoods record);

    int insertSelective(PlayerGoods record);

    PlayerGoods selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PlayerGoods record);

    int updateByPrimaryKey(PlayerGoods record);
}