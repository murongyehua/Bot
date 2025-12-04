package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifeMap;
import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.mapper.LifeMapMapper;
import com.bot.life.service.MapService;
import com.bot.life.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地图服务实现
 * @author Assistant
 */
@Service
public class MapServiceImpl implements MapService {
    
    @Autowired
    private LifeMapMapper mapMapper;
    
    @Autowired
    private PlayerService playerService;
    
    @Override
    public List<LifeMap> getAvailableMaps(LifePlayer player) {
        return mapMapper.selectAvailableMaps(player.getLevel());
    }
    
    @Override
    public LifeMap getMapById(Long mapId) {
        return mapMapper.selectByPrimaryKey(mapId);
    }
    
    @Override
    public String teleportToMap(LifePlayer player, Long targetMapId) {
        LifeMap targetMap = getMapById(targetMapId);
        if (targetMap == null) {
            return "目标地图不存在！";
        }
        
        // 检查等级要求
        if (player.getLevel() < targetMap.getMinLevel()) {
            return String.format("『%s』需要达到%d级才能进入！", 
                               targetMap.getName(), targetMap.getMinLevel());
        }
        
        // 检查是否为可传送地图
        if (targetMap.getType() != 1) {
            return "该地图无法直接传送！";
        }
        
        // 更新玩家位置
        player.setCurrentMapId(targetMapId);
        playerService.updatePlayer(player);
        
        return String.format("『传送成功！』\n\n已到达『%s』\n\n%s", 
                           targetMap.getName(), 
                           targetMap.getDescription());
    }
    
    @Override
    public String getMapDescription(Long mapId) {
        LifeMap map = getMapById(mapId);
        if (map == null) {
            return "未知地图";
        }
        
        StringBuilder desc = new StringBuilder();
        desc.append("『").append(map.getName()).append("』\n\n");
        desc.append(map.getDescription()).append("\n\n");
        desc.append("等级要求：").append(map.getMinLevel()).append("级\n");
        
        if (map.getType() == 1) {
            desc.append("类型：可传送地图");
        } else {
            desc.append("类型：特殊地图");
        }
        
        return desc.toString();
    }
}
