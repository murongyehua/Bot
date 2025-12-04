package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dto.BattleContext;

/**
 * 探索服务接口
 * @author Assistant
 */
public interface ExplorationService {
    
    /**
     * 游历探索
     * @param player 玩家信息
     * @return 探索结果（可能是战斗、发现道具、遇到NPC等）
     */
    String explore(LifePlayer player);
    
    /**
     * 随机遭遇怪物
     * @param player 玩家信息
     * @return 战斗上下文，如果没有遭遇则返回null
     */
    BattleContext encounterMonster(LifePlayer player);
    
    /**
     * 检查体力是否足够
     * @param player 玩家信息
     * @return 是否有足够体力
     */
    boolean hasEnoughStamina(LifePlayer player);
    
    /**
     * 消耗体力
     * @param player 玩家信息
     * @param amount 消耗量
     */
    void consumeStamina(LifePlayer player, int amount);
}
