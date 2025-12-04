package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 好友实体
 * @author Assistant
 */
@Data
public class LifeFriend {
    private Long id;
    private Long playerId;
    private Long friendId;
    private Integer status; // 状态：0待确认1已同意
    private Date createTime;
    private Date updateTime;
    
    // 关联对象
    private LifePlayer friend;
}
