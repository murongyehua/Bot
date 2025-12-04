package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeGameStatus;

/**
 * 游戏状态Mapper接口
 * @author Assistant
 */
public interface LifeGameStatusMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeGameStatus record);
    
    /**
     * 根据主键查询
     */
    LifeGameStatus selectByPrimaryKey(Long id);
    
    /**
     * 根据用户ID查询
     */
    LifeGameStatus selectByUserId(String userId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeGameStatus record);
}
