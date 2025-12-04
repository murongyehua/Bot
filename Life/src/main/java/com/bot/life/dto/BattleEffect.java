package com.bot.life.dto;

import lombok.Data;

/**
 * 战斗效果
 * @author Assistant
 */
@Data
public class BattleEffect {
    private String effectId; // 效果ID
    private String effectName; // 效果名称
    private Integer effectType; // 效果类型：1伤害倍率2属性变化3持续效果
    private Integer duration; // 持续回合数
    private Integer timing; // 生效时机：1回合开始前2回合开始后3回合结束
    private Double effectValue; // 效果数值
    private String targetAttribute; // 目标属性
    private Long sourceSkillId; // 来源技能ID
    
    /**
     * 减少持续时间
     */
    public void reduceDuration() {
        if (this.duration > 0) {
            this.duration--;
        }
    }
    
    /**
     * 效果是否过期
     */
    public boolean isExpired() {
        return this.duration <= 0;
    }
}
