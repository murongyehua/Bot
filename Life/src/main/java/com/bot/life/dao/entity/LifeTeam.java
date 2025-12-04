package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 队伍实体
 * @author Assistant
 */
@Data
public class LifeTeam {
    private Long id;
    private Long leaderId;
    private String teamName;
    private Integer maxMembers;
    private Integer currentMembers;
    private Integer teamStatus; // 队伍状态：0待组队1已满员2副本中
    private Long dungeonId; // 当前副本ID
    private Date createTime;
    private Date updateTime;
    
    // 关联对象
    private LifePlayer leader;
}
