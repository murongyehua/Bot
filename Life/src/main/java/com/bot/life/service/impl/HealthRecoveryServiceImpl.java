package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.mapper.LifePlayerMapper;
import com.bot.life.service.HealthRecoveryService;
import com.bot.life.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 血量自动恢复服务实现
 * @author Assistant
 */
@Service
public class HealthRecoveryServiceImpl implements HealthRecoveryService {
    
    @Autowired
    private LifePlayerMapper playerMapper;
    
    @Autowired
    private PlayerService playerService;
    
    @Override
    public void checkAndRecoverHealth(Long playerId) {
        LifePlayer player = playerMapper.selectByPrimaryKey(playerId);
        if (player == null || player.getHealth() >= player.getMaxHealth()) {
            return;
        }
        
        // 检查是否脱离战斗（最后战斗时间超过1分钟或从未战斗）
        Date now = new Date();
        Date lastBattleTime = player.getLastBattleTime();
        
        // 如果从未战斗或距离最后战斗超过1分钟，则可以恢复血量
        boolean canRecover = lastBattleTime == null || 
            (now.getTime() - lastBattleTime.getTime()) >= 60000; // 1分钟 = 60000毫秒
        
        if (!canRecover) {
            return;
        }
        
        // 检查距离上次血量恢复是否超过1分钟
        Date lastRecoveryTime = player.getLastHpRecoveryTime();
        if (lastRecoveryTime != null && 
            (now.getTime() - lastRecoveryTime.getTime()) < 60000) {
            return;
        }
        
        // 恢复血量：每分钟恢复最大血量的20%
        int recoveryAmount = (int) Math.ceil(player.getMaxHealth() * 0.2);
        int newHealth = Math.min(player.getMaxHealth(), player.getHealth() + recoveryAmount);
        
        if (newHealth > player.getHealth()) {
            player.setHealth(newHealth);
            player.setLastHpRecoveryTime(now);
            playerService.updatePlayer(player);
        }
    }
    
    @Override
    public void recoverAllPlayersHealth() {
        // 查询所有血量不满的玩家
        List<LifePlayer> players = playerMapper.selectAllPlayers();
        
        for (LifePlayer player : players) {
            if (player.getHealth() < player.getMaxHealth()) {
                checkAndRecoverHealth(player.getId());
            }
        }
    }
    
    @Override
    public void updateLastBattleTime(Long playerId) {
        LifePlayer player = playerMapper.selectByPrimaryKey(playerId);
        if (player != null) {
            player.setLastBattleTime(new Date());
            playerService.updatePlayer(player);
        }
    }
}
