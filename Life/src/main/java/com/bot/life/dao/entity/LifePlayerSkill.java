package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 玩家技能实体
 * @author Assistant
 */
@Data
public class LifePlayerSkill {
    private Long id;
    private Long playerId;
    private Long skillId;
    private Integer skillLevel;
    private Integer currentCooldown;
    private Date lastUsedTime;
    private Date learnTime;
    
    // 关联对象
    private LifeSkill skill;
}
