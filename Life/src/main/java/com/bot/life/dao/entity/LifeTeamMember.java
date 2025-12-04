package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 队伍成员实体
 * @author Assistant
 */
@Data
public class LifeTeamMember {
    private Long id;
    private Long teamId;
    private Long playerId;
    private Integer memberStatus; // 成员状态：0申请中1已同意
    private Date joinTime;
    
    // 关联对象
    private LifePlayer player;
    private LifeTeam team;
}
