package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;

/**
 * 鬼市服务接口
 * @author Assistant
 */
public interface MarketService {
    
    /**
     * 获取鬼市主界面
     * @param player 玩家信息
     * @return 鬼市界面描述
     */
    String getMarketMainMenu(LifePlayer player);
    
    /**
     * 获取神秘商人商店
     * @return 商店商品列表
     */
    String getMysteriousShop();
    
    /**
     * 获取商品详情
     * @param index 商品序号
     * @param player 玩家信息
     * @return 商品详情和购买选项
     */
    String getShopItemDetail(int index, LifePlayer player);
    
    /**
     * 从神秘商人购买商品
     * @param player 玩家信息
     * @param itemId 商品ID
     * @param quantity 购买数量
     * @return 购买结果
     */
    String buyFromShop(LifePlayer player, Long itemId, Integer quantity);
    
    /**
     * 向神秘商人出售道具
     * @param player 玩家信息
     * @param itemId 道具ID
     * @param quantity 出售数量
     * @return 出售结果
     */
    String sellToShop(LifePlayer player, Long itemId, Integer quantity);
    
    /**
     * 获取所有玩家摊位
     * @return 摊位列表
     */
    String getAllPlayerStalls();
    
    /**
     * 创建玩家摊位
     * @param player 玩家信息
     * @param stallName 摊位名称
     * @param itemType 商品类型
     * @param itemId 商品ID
     * @param quantity 数量
     * @param unitPrice 单价
     * @return 创建结果
     */
    String createPlayerStall(LifePlayer player, String stallName, Integer itemType, 
                           Long itemId, Integer quantity, Integer unitPrice);
    
    /**
     * 从玩家摊位购买
     * @param buyer 买家信息
     * @param stallId 摊位ID
     * @param quantity 购买数量
     * @return 购买结果
     */
    String buyFromPlayerStall(LifePlayer buyer, Long stallId, Integer quantity);
    
    /**
     * 刷新神秘商人商店（每日）
     */
    void refreshMysteriousShop();
}
