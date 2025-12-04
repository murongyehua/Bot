package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeMonsterDrop;

import java.util.List;

/**
 * 怪物掉落配置Mapper接口
 * @author Assistant
 */
public interface LifeMonsterDropMapper {
    
    /**
     * 根据怪物ID查询掉落配置
     */
    List<LifeMonsterDrop> selectByMonsterId(Long monsterId);
    
    /**
     * 插入记录
     */
    int insert(LifeMonsterDrop record);
    
    /**
     * 根据主键查询
     */
    LifeMonsterDrop selectByPrimaryKey(Long id);
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeMonsterDrop record);
}

