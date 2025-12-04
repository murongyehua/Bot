package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifePlayerItem;

import java.util.List;

/**
 * 玩家道具Mapper接口
 * @author Assistant
 */
public interface LifePlayerItemMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifePlayerItem record);
    
    /**
     * 根据主键查询
     */
    LifePlayerItem selectByPrimaryKey(Long id);
    
    /**
     * 根据玩家ID查询所有道具
     */
    List<LifePlayerItem> selectByPlayerId(Long playerId);
    
    /**
     * 根据玩家ID和道具ID查询
     */
    LifePlayerItem selectByPlayerIdAndItemId(Long playerId, Long itemId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifePlayerItem record);
}
