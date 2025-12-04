package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifePlayerItem;

import java.util.List;

/**
 * 背包服务接口
 * @author Assistant
 */
public interface InventoryService {
    
    /**
     * 获取玩家背包内容
     * @param player 玩家信息
     * @return 背包道具列表
     */
    List<LifePlayerItem> getPlayerItems(LifePlayer player);
    
    /**
     * 添加道具到背包
     * @param playerId 玩家ID
     * @param itemId 道具ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean addItem(Long playerId, Long itemId, Integer quantity);
    
    /**
     * 使用道具
     * @param player 玩家信息
     * @param itemId 道具ID
     * @return 使用结果描述
     */
    String useItem(LifePlayer player, Long itemId);
    
    /**
     * 检查是否可以使用道具
     * @param player 玩家信息
     * @param itemId 道具ID
     * @return 是否可以使用
     */
    boolean canUseItem(LifePlayer player, Long itemId);
    
    /**
     * 获取背包显示内容
     * @param player 玩家信息
     * @return 背包内容描述
     */
    String getInventoryDisplay(LifePlayer player);
}
