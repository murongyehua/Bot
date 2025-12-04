package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 玩家摊位实体
 * @author Assistant
 */
@Data
public class LifePlayerStall {
    private Long id;
    private Long playerId;
    private String stallName;
    private Integer itemType; // 商品类型：1道具2装备
    private Long itemId;
    private Integer quantity;
    private Integer unitPrice;
    private Date createTime;
    
    // 关联对象
    private LifePlayer player;
    private LifeItem item;
    private LifeEquipment equipment;
}
