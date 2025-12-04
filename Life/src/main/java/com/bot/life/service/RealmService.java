package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifeRealmConfig;

/**
 * 境界服务接口
 * @author Assistant
 */
public interface RealmService {
    
    /**
     * 尝试突破境界
     * @param playerId 玩家ID
     * @return 突破结果描述
     */
    String attemptBreakthrough(Long playerId);
    
    /**
     * 检查是否可以突破
     * @param player 玩家信息
     * @return 是否可以突破
     */
    boolean canBreakthrough(LifePlayer player);
    
    /**
     * 获取当前境界配置
     * @param level 当前等级
     * @return 境界配置
     */
    LifeRealmConfig getCurrentRealm(Integer level);
    
    /**
     * 获取下一境界配置
     * @param currentLevel 当前等级
     * @return 下一境界配置
     */
    LifeRealmConfig getNextRealm(Integer currentLevel);
    
    /**
     * 查看境界信息
     * @param playerId 玩家ID
     * @return 境界信息描述
     */
    String viewRealmInfo(Long playerId);
    
    /**
     * 应用突破奖励属性
     * @param player 玩家信息
     * @param realmConfig 境界配置
     */
    void applyBreakthroughBonus(LifePlayer player, LifeRealmConfig realmConfig);
}
