package com.bot.life.dao.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 怪物实体
 * @author Assistant
 */
@Data
public class LifeMonster {
    private Long id;
    private String name;
    private Long mapId;
    private Integer monsterType; // 怪物类型：1普通怪物2副本BOSS3世界BOSS
    private Integer attribute; // 怪物属性：0无属性1金2木3水4火5土
    
    // 战斗属性
    private Integer level;
    private Integer health;
    private Integer attackPower;
    private Integer defense;
    private Integer speed;
    private BigDecimal criticalRate;
    private BigDecimal criticalDamage;
    private BigDecimal armorBreak;
    
    private Date createTime;
}
