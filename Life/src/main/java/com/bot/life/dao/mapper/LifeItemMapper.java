package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeItem;

import java.util.List;

/**
 * 道具Mapper接口
 * @author Assistant
 */
public interface LifeItemMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeItem record);
    
    /**
     * 根据主键查询
     */
    LifeItem selectByPrimaryKey(Long id);
    
    /**
     * 查询所有道具
     */
    List<LifeItem> selectAll();
    
    /**
     * 根据类型查询道具
     */
    List<LifeItem> selectByType(Integer type);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeItem record);
}
