package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifePlayerAchievement;

import java.util.List;

/**
 * 玩家成就Mapper接口
 * @author Assistant
 */
public interface LifePlayerAchievementMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifePlayerAchievement record);
    
    /**
     * 根据主键查询
     */
    LifePlayerAchievement selectByPrimaryKey(Long id);
    
    /**
     * 根据玩家ID查询所有成就
     */
    List<LifePlayerAchievement> selectByPlayerId(Long playerId);
    
    /**
     * 根据玩家ID和成就ID查询
     */
    LifePlayerAchievement selectByPlayerIdAndAchievementId(Long playerId, Long achievementId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifePlayerAchievement record);
}
