package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeWorldBossChallenge;

import java.util.List;

/**
 * 世界BOSS挑战记录Mapper接口
 * @author Assistant
 */
public interface LifeWorldBossChallengeMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeWorldBossChallenge record);
    
    /**
     * 根据主键查询
     */
    LifeWorldBossChallenge selectByPrimaryKey(Long id);
    
    /**
     * 查询玩家今日对指定世界BOSS的挑战次数
     */
    int selectTodayChallengeCount(Long playerId, Long worldBossId);
    
    /**
     * 查询玩家今日所有世界BOSS挑战记录
     */
    List<LifeWorldBossChallenge> selectTodayChallengesByPlayerId(Long playerId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeWorldBossChallenge record);
}
