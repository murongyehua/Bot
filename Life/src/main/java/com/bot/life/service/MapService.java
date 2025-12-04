package com.bot.life.service;

import com.bot.life.dao.entity.LifeMap;
import com.bot.life.dao.entity.LifePlayer;

import java.util.List;

/**
 * 地图服务接口
 * @author Assistant
 */
public interface MapService {
    
    /**
     * 获取玩家可传送的地图列表
     * @param player 玩家信息
     * @return 可传送的地图列表
     */
    List<LifeMap> getAvailableMaps(LifePlayer player);
    
    /**
     * 根据ID获取地图信息
     * @param mapId 地图ID
     * @return 地图信息
     */
    LifeMap getMapById(Long mapId);
    
    /**
     * 传送到指定地图
     * @param player 玩家信息
     * @param targetMapId 目标地图ID
     * @return 传送结果描述
     */
    String teleportToMap(LifePlayer player, Long targetMapId);
    
    /**
     * 获取地图详细信息
     * @param mapId 地图ID
     * @return 地图详细描述
     */
    String getMapDescription(Long mapId);
}
