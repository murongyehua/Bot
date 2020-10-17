package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.BasePhantom;

import java.util.List;

public interface BasePhantomMapper {
    int deleteByPrimaryKey(String id);

    int insert(BasePhantom record);

    int insertSelective(BasePhantom record);

    BasePhantom selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BasePhantom record);

    int updateByPrimaryKey(BasePhantom record);

    List<BasePhantom> selectBySelective(BasePhantom record);
}