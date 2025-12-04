package com.bot.life.dao.entity;

import lombok.Data;

/**
 * 世界BOSS奖励实体
 * @author Assistant
 */
@Data
public class LifeWorldBossReward {
    private Long id;
    private Long worldBossId;
    private Long minDamage;
    private Long maxDamage;
    private Integer spiritReward;
    private String itemRewards; // JSON格式的道具奖励
}
