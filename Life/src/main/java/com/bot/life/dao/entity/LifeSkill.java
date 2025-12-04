package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 技能基础实体
 * @author Assistant
 */
@Data
public class LifeSkill {
    private Long id;
    private String name;
    private Integer type; // 技能类型：1直接伤害2增益3减益
    private Integer attribute; // 技能属性：0无属性1金2木3水4火5土
    private Integer power; // 技能威力
    private Integer cooldown; // 冷却时间（秒）
    private Integer requiredLevel; // 需要等级
    private Integer requiredCultivation; // 需要修为
    private Integer maxLevel; // 最大等级
    private String description;
}
