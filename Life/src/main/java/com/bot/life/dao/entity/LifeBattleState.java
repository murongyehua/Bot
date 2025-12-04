package com.bot.life.dao.entity;

import lombok.Data;
import java.util.Date;

/**
 * 战斗状态实体
 * @author Assistant
 */
@Data
public class LifeBattleState {
    private Long id;
    private Long playerId; // 玩家ID
    private Long monsterId; // 怪物ID
    private Integer currentTurn; // 当前回合数
    private Integer playerHp; // 玩家当前血量
    private Integer monsterHp; // 怪物当前血量
    private Integer monsterMaxHp; // 怪物最大血量
    private String monsterSkillCooldowns; // 怪物技能冷却状态（JSON格式）
    private String playerBuffs; // 玩家buff状态（JSON格式）
    private String monsterBuffs; // 怪物buff状态（JSON格式）
    private Date createTime;
    private Date updateTime;
}
