package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeMonster;

import java.util.List;

/**
 * 怪物Mapper接口
 * @author Assistant
 */
public interface LifeMonsterMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeMonster record);
    
    /**
     * 根据主键查询
     */
    LifeMonster selectByPrimaryKey(Long id);
    
    /**
     * 根据地图ID查询普通怪物
     */
    List<LifeMonster> selectNormalMonstersByMapId(Long mapId);
    
    /**
     * 根据地图ID和怪物类型查询
     */
    List<LifeMonster> selectByMapIdAndType(Long mapId, Integer monsterType);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeMonster record);
}
