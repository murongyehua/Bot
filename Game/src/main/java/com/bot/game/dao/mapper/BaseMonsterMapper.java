package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.BaseMonster;

import java.util.List;

public interface BaseMonsterMapper {
    int deleteByPrimaryKey(String id);

    int insert(BaseMonster record);

    int insertSelective(BaseMonster record);

    BaseMonster selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BaseMonster record);

    int updateByPrimaryKey(BaseMonster record);

    List<BaseMonster> selectBySelective(BaseMonster record);
}