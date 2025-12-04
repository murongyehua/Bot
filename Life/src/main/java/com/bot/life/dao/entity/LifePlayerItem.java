package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 玩家道具实体
 * @author Assistant
 */
@Data
public class LifePlayerItem {
    private Long id;
    private Long playerId;
    private Long itemId;
    private Integer quantity;
    private Integer usedCount;
    private Date createTime;
    private Date updateTime;
    
    // 关联对象
    private LifeItem item;
}
