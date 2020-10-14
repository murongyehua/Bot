package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.BaseSkill;

public interface BaseSkillMapper {
    int deleteByPrimaryKey(String id);

    int insert(BaseSkill record);

    int insertSelective(BaseSkill record);

    BaseSkill selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BaseSkill record);

    int updateByPrimaryKey(BaseSkill record);
}