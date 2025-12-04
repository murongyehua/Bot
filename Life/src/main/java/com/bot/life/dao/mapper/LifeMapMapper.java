package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeMap;

import java.util.List;

/**
 * 地图Mapper接口
 * @author Assistant
 */
public interface LifeMapMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeMap record);
    
    /**
     * 根据主键查询
     */
    LifeMap selectByPrimaryKey(Long id);
    
    /**
     * 查询所有可传送地图
     */
    List<LifeMap> selectTeleportableMaps();
    
    /**
     * 根据等级要求查询可进入的地图
     */
    List<LifeMap> selectAvailableMaps(Integer playerLevel);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeMap record);
}
