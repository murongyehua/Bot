package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 地图实体
 * @author Assistant
 */
@Data
public class LifeMap {
    private Long id;
    private String name;
    private Integer type; // 地图类型：1可传送2内置地图
    private Integer minLevel; // 最低境界要求
    private String description;
    private Date createTime;
}
