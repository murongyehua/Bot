package com.bot.game.dao.mapper;


import com.bot.game.dao.entity.GoodsBox;

import java.util.List;

public interface GoodsBoxMapper {
    int deleteByPrimaryKey(String id);

    int insert(GoodsBox record);

    int insertSelective(GoodsBox record);

    GoodsBox selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(GoodsBox record);

    int updateByPrimaryKey(GoodsBox record);

    List<GoodsBox> selectBySelective(GoodsBox record);
}