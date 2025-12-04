package com.bot.life.dao.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 境界配置实体
 * @author Assistant
 */
@Data
public class LifeRealmConfig {
    private Long id;
    private String realmName; // 境界名称
    private Integer minLevel; // 最低等级
    private Integer maxLevel; // 最高等级
    private Long requiredCultivation; // 突破所需修为
    private Long maxCultivation; // 该境界修为上限
    private BigDecimal successRate; // 突破成功率
    private String attributeBonus; // 突破奖励属性(JSON格式)
    private String specialAbilities; // 境界特殊能力描述
    private Date createTime;
}
