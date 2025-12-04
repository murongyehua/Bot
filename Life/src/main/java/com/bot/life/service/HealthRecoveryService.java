package com.bot.life.service;

/**
 * 血量自动恢复服务
 * @author Assistant
 */
public interface HealthRecoveryService {
    
    /**
     * 检查并恢复指定玩家的血量
     * 
     * @param playerId 玩家ID
     */
    void checkAndRecoverHealth(Long playerId);
    
    /**
     * 检查并恢复所有玩家的血量（定时任务调用）
     */
    void recoverAllPlayersHealth();
    
    /**
     * 设置玩家最后战斗时间
     * 
     * @param playerId 玩家ID
     */
    void updateLastBattleTime(Long playerId);
}
