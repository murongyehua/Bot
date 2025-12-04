package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 玩家装备实体
 * @author Assistant
 */
@Data
public class LifePlayerEquipment {
    private Long id;
    private Long playerId;
    private Long equipmentId;
    private Integer isEquipped; // 是否装备：0未装备1已装备
    private Integer proficiency; // 熟练度(仅法宝使用)
    private Integer level; // 等级(仅法宝使用)
    private Date createTime;
    private Date updateTime;
    
    // 关联对象
    private LifeEquipment equipment;
}
