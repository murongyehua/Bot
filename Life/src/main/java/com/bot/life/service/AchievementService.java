package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;

import java.util.List;

/**
 * 成就服务接口
 * @author Assistant
 */
public interface AchievementService {
    
    /**
     * 检查并触发玩家成就
     * @param player 玩家信息
     * @return 新获得的成就描述列表
     */
    List<String> checkAndTriggerAchievements(LifePlayer player);
    
    /**
     * 获取玩家成就列表
     * @param playerId 玩家ID
     * @return 成就描述
     */
    String getPlayerAchievements(Long playerId);
    
    /**
     * 检查特定成就是否完成
     * @param player 玩家信息
     * @param achievementId 成就ID
     * @return 是否完成
     */
    boolean isAchievementCompleted(LifePlayer player, Long achievementId);
    
    /**
     * 完成成就
     * @param playerId 玩家ID
     * @param achievementId 成就ID
     * @return 是否成功
     */
    boolean completeAchievement(Long playerId, Long achievementId);
}
