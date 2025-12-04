package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 玩家签到记录实体
 * @author Assistant
 */
@Data
public class LifePlayerSignin {
    private Long id;
    private Long playerId; // 玩家ID
    private Date signinDate; // 签到日期
    private Integer spiritReward; // 获得的灵粹奖励
    private Date createTime; // 签到时间
}
