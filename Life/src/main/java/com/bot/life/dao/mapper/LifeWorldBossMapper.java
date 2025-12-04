package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeWorldBoss;

import java.util.List;

/**
 * 世界BOSSMapper接口
 * @author Assistant
 */
public interface LifeWorldBossMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeWorldBoss record);
    
    /**
     * 根据主键查询
     */
    LifeWorldBoss selectByPrimaryKey(Long id);
    
    /**
     * 查询所有活跃的世界BOSS
     */
    List<LifeWorldBoss> selectActiveWorldBosses();
    
    /**
     * 根据地图ID查询世界BOSS
     */
    List<LifeWorldBoss> selectByMapId(Long mapId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeWorldBoss record);
}
