package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeAchievement;

import java.util.List;

/**
 * 成就Mapper接口
 * @author Assistant
 */
public interface LifeAchievementMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeAchievement record);
    
    /**
     * 根据主键查询
     */
    LifeAchievement selectByPrimaryKey(Long id);
    
    /**
     * 查询所有成就
     */
    List<LifeAchievement> selectAll();
    
    /**
     * 根据条件类型查询成就
     */
    List<LifeAchievement> selectByConditionType(Integer conditionType);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeAchievement record);
}
