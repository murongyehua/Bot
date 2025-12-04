package com.bot.life.dao.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 怪物掉落配置实体
 * @author Assistant
 */
@Data
public class LifeMonsterDrop {
    private Long id;
    private Long monsterId;
    private Integer dropType; // 掉落类型：1道具2灵粹
    private Long itemId; // 道具ID
    private Integer spiritAmount; // 灵粹数量
    private BigDecimal dropRate; // 掉落概率
    private Integer minQuantity; // 最小掉落数量
    private Integer maxQuantity; // 最大掉落数量
    private Date createTime;
    
    // 关联对象
    private LifeItem item;
}

