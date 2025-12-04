package com.bot.life.dao.entity;

import lombok.Data;

/**
 * 副本实体
 * @author Assistant
 */
@Data
public class LifeDungeon {
    private Long id;
    private String name;
    private String description;
    private Integer minLevel;
    private Integer maxLevel;
    private Integer requiredMembers;
    private Integer difficulty; // 难度：1简单2普通3困难
    private String rewards; // 奖励信息（JSON格式）
    private Integer isActive;
}
