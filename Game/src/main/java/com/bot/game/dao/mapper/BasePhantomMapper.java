package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.BasePhantom;

public interface BasePhantomMapper {
    int deleteByPrimaryKey(String id);

    int insert(BasePhantom record);

    int insertSelective(BasePhantom record);

    BasePhantom selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BasePhantom record);

    int updateByPrimaryKey(BasePhantom record);
}