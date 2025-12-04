package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 世界BOSS挑战记录实体
 * @author Assistant
 */
@Data
public class LifeWorldBossChallenge {
    private Long id;
    private Long playerId;
    private Long worldBossId;
    private Long damageDealt; // 造成的伤害
    private Integer spiritReward; // 获得的灵粹奖励
    private String itemRewards; // 获得的道具奖励（JSON格式）
    private Date challengeTime;
    
    // 关联对象
    private LifePlayer player;
    private LifeWorldBoss worldBoss;
}
