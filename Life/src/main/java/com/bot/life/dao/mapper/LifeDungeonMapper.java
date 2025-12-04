package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeDungeon;

import java.util.List;

/**
 * 副本Mapper接口
 * @author Assistant
 */
public interface LifeDungeonMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeDungeon record);
    
    /**
     * 根据主键查询
     */
    LifeDungeon selectByPrimaryKey(Long id);
    
    /**
     * 查询所有激活的副本
     */
    List<LifeDungeon> selectActiveDungeons();
    
    /**
     * 根据难度查询副本
     */
    List<LifeDungeon> selectByDifficulty(Integer difficulty);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeDungeon record);
}
