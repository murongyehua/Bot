package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 成就实体
 * @author Assistant
 */
@Data
public class LifeAchievement {
    private Long id;
    private String name;
    private String description;
    private Integer conditionType; // 条件类型：1属性达到2等级达到
    private String conditionTarget; // 条件目标字段
    private Long conditionValue; // 条件数值
    private Date createTime;
}
