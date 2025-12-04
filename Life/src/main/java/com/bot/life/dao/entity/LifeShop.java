package com.bot.life.dao.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商店实体
 * @author Assistant
 */
@Data
public class LifeShop {
    private Long id;
    private Integer itemType; // 商品类型：1道具2装备
    private Long itemId;
    private Integer basePrice;
    private Integer currentPrice;
    private BigDecimal discount;
    private Boolean inStock; // 是否有库存
    private Date lastRefreshDate;
    private Date createTime;
    private Date updateTime;
    
    // 关联对象
    private LifeItem item;
    private LifeEquipment equipment;
}
