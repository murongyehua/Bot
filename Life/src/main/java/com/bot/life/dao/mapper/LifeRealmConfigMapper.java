package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeRealmConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 境界配置Mapper
 * @author Assistant
 */
public interface LifeRealmConfigMapper {
    
    /**
     * 根据主键查询
     */
    LifeRealmConfig selectByPrimaryKey(Long id);
    
    /**
     * 根据等级查询境界配置
     */
    LifeRealmConfig selectByLevel(@Param("level") Integer level);
    
    /**
     * 查询下一个境界配置
     */
    LifeRealmConfig selectNextRealm(@Param("currentLevel") Integer currentLevel);
    
    /**
     * 查询所有境界配置
     */
    List<LifeRealmConfig> selectAll();
    
    /**
     * 插入记录
     */
    int insert(LifeRealmConfig record);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeRealmConfig record);
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
}
