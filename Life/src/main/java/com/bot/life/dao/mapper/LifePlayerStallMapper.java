package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifePlayerStall;

import java.util.List;

/**
 * 玩家摊位Mapper接口
 * @author Assistant
 */
public interface LifePlayerStallMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifePlayerStall record);
    
    /**
     * 根据主键查询
     */
    LifePlayerStall selectByPrimaryKey(Long id);
    
    /**
     * 查询所有摊位
     */
    List<LifePlayerStall> selectAll();
    
    /**
     * 根据玩家ID查询摊位
     */
    List<LifePlayerStall> selectByPlayerId(Long playerId);
    
    /**
     * 根据玩家ID和道具ID查询摊位
     */
    LifePlayerStall selectByPlayerIdAndItemId(Long playerId, Long itemId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifePlayerStall record);
}
