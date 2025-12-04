package com.bot.life.dao.entity;

import lombok.Data;

/**
 * 怪物技能实体
 * @author Assistant
 */
@Data
public class LifeMonsterSkill {
    private Long id;
    private Long monsterId; // 怪物ID
    private Long skillId; // 技能ID
    
    // 关联对象
    private LifeSkill skill;
}
