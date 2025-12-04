package com.bot.life.dao.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 玩家角色实体
 * @author Assistant
 */
@Data
public class LifePlayer {
    private Long id;
    private String userId;
    private String nickname;
    private Integer attribute; // 角色属性：1金2木3水4火5土
    private Integer level;
    private Long experience; // 经验值
    private Long cultivation;
    private Integer cultivationSpeed;
    private Date lastCultivationTime;
    
    // 基础属性
    private Integer speed;
    private Integer constitution;
    private Integer spiritPower;
    private Integer strength;
    
    // 拓展属性(战斗属性)
    private Integer health;
    private Integer maxHealth;
    private Integer defense;
    private BigDecimal criticalRate;
    private BigDecimal criticalDamage;
    private BigDecimal armorBreak;
    private Integer attackPower;
    
    private Integer stamina;
    private Integer maxStamina;
    private Date lastStaminaTime;
    
    private Long spirit; // 灵粹（游戏货币）
    private Date lastBattleTime; // 最后战斗时间
    private Date lastHpRecoveryTime; // 最后血量恢复时间
    
    private Long currentMapId;
    private Integer gameStatus; // 游戏状态：0正常1战斗中2组队中
    
    private Date createTime;
    private Date updateTime;
    
    /**
     * 根据基础属性计算拓展属性
     */
    public void calculateExtendedAttributes() {
        // 每增加1点速度，破防随之增加0.005%
        this.armorBreak = BigDecimal.valueOf(speed * 0.005);
        
        // 每增加1点体质，血量随之增加10点，防御随之增加1点
        this.maxHealth = 10 + constitution * 10;
        this.defense = 1 + constitution;
        
        // 每增加1点灵力，会心增加0.01%，会心效果增加0.005%
        this.criticalRate = BigDecimal.valueOf(spiritPower * 0.01);
        this.criticalDamage = BigDecimal.valueOf(110 + spiritPower * 0.005);
        
        // 每增加1点力量，攻击力增加6点，破防增加0.01%
        this.attackPower = 6 + strength * 6;
        this.armorBreak = this.armorBreak.add(BigDecimal.valueOf(strength * 0.01));
        
        // 如果当前血量超过最大血量，则设置为最大血量
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }
    
    /**
     * 恢复体力
     */
    public void recoverStamina() {
        if (this.stamina >= this.maxStamina) {
            return;
        }
        
        long now = System.currentTimeMillis();
        long lastTime = this.lastStaminaTime.getTime();
        long diffMinutes = (now - lastTime) / (1000 * 60);
        
        // 每5分钟恢复1点体力
        int recoverAmount = (int) (diffMinutes / 5);
        if (recoverAmount > 0) {
            this.stamina = Math.min(this.maxStamina, this.stamina + recoverAmount);
            this.lastStaminaTime = new Date();
        }
    }
    
    /**
     * 获取修为（带上限检查）
     */
    public long gainCultivation(Long maxCultivation) {
        long now = System.currentTimeMillis();
        long lastTime = this.lastCultivationTime.getTime();
        long diffMinutes = (now - lastTime) / (1000 * 60);
        
        // 根据修炼速度计算获得的修为
        long gainedCultivation = diffMinutes * this.cultivationSpeed;
        if (gainedCultivation > 0) {
            // 检查修为上限
            if (maxCultivation != null && this.cultivation >= maxCultivation) {
                // 已达上限，不再获得修为
                return 0;
            }
            
            // 如果加上获得的修为会超过上限，则只加到上限
            if (maxCultivation != null && this.cultivation + gainedCultivation > maxCultivation) {
                long actualGained = maxCultivation - this.cultivation;
                this.cultivation = maxCultivation;
                this.lastCultivationTime = new Date();
                return actualGained;
            } else {
                this.cultivation += gainedCultivation;
                this.lastCultivationTime = new Date();
                return gainedCultivation;
            }
        }
        return 0;
    }
}
