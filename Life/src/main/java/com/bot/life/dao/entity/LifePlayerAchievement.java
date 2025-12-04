package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 玩家成就实体
 * @author Assistant
 */
@Data
public class LifePlayerAchievement {
    private Long id;
    private Long playerId;
    private Long achievementId;
    private Date completedTime;
    
    // 关联对象
    private LifeAchievement achievement;
}
