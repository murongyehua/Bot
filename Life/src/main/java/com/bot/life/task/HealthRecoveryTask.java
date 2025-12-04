package com.bot.life.task;

import com.bot.life.service.HealthRecoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 血量自动恢复定时任务
 * @author Assistant
 */
@Component
public class HealthRecoveryTask {
    
    @Autowired
    private HealthRecoveryService healthRecoveryService;
    
    /**
     * 每分钟执行一次血量恢复检查
     */
    @Scheduled(fixedRate = 60000) // 60秒 = 60000毫秒
    public void recoverHealthForAllPlayers() {
        try {
            healthRecoveryService.recoverAllPlayersHealth();
        } catch (Exception e) {
            // 记录错误但不影响其他功能
            System.err.println("血量恢复定时任务执行异常: " + e.getMessage());
        }
    }
}
