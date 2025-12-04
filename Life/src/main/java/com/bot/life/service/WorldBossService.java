package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifeWorldBoss;

import java.util.List;

/**
 * 世界BOSS服务接口
 * @author Assistant
 */
public interface WorldBossService {
    
    /**
     * 获取当前活跃的世界BOSS
     * @return 活跃的世界BOSS列表
     */
    List<LifeWorldBoss> getCurrentActiveWorldBosses();
    
    /**
     * 检查玩家是否可以挑战指定世界BOSS
     * @param player 玩家信息
     * @param worldBossId 世界BOSS ID
     * @return 是否可以挑战
     */
    boolean canChallengeWorldBoss(LifePlayer player, Long worldBossId);
    
    /**
     * 挑战世界BOSS
     * @param player 玩家信息
     * @param worldBossId 世界BOSS ID
     * @return 挑战结果描述
     */
    String challengeWorldBoss(LifePlayer player, Long worldBossId);
    
    /**
     * 获取世界BOSS信息显示
     * @param mapId 地图ID
     * @return 世界BOSS信息
     */
    String getWorldBossInfo(Long mapId);
    
    /**
     * 获取玩家今日世界BOSS挑战记录
     * @param playerId 玩家ID
     * @return 挑战记录描述
     */
    String getTodayWorldBossChallenges(Long playerId);
    
    /**
     * 检查指定时间是否有世界BOSS活动
     * @return 是否有活动
     */
    boolean isWorldBossActiveNow();
}
