package com.bot.life.dao.entity;

import lombok.Data;

import java.sql.Time;

/**
 * 世界BOSS实体
 * @author Assistant
 */
@Data
public class LifeWorldBoss {
    private Long id;
    private Long monsterId;
    private Long mapId;
    private Time startTime;
    private Time endTime;
    private Integer maxChallengeCount;
    private Integer isActive;
    
    // 关联对象
    private LifeMonster monster;
    private LifeMap map;
}
