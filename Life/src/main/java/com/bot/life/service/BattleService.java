package com.bot.life.service;

import com.bot.life.dto.BattleContext;
import com.bot.life.dto.BattleResult;
import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifeMonster;
import com.bot.life.enums.ENBattleAction;

/**
 * 战斗服务接口
 * @author Assistant
 */
public interface BattleService {
    
    /**
     * 开始战斗
     * @param player 玩家
     * @param monster 怪物
     * @param battleType 战斗类型
     * @return 战斗上下文
     */
    BattleContext startBattle(LifePlayer player, LifeMonster monster, Integer battleType);
    
    /**
     * 执行玩家行动
     * @param battleContext 战斗上下文
     * @param action 行动类型
     * @param actionParam 行动参数（技能ID、道具ID等）
     * @return 更新后的战斗上下文
     */
    BattleContext executePlayerAction(BattleContext battleContext, ENBattleAction action, String actionParam);
    
    /**
     * 执行怪物行动
     * @param battleContext 战斗上下文
     * @return 更新后的战斗上下文
     */
    BattleContext executeMonsterAction(BattleContext battleContext);
    
    /**
     * 计算伤害
     * @param attacker 攻击者属性
     * @param defender 防御者属性
     * @param skillMultiplier 技能倍率
     * @param canCritical 是否可会心
     * @return 伤害值
     */
    Integer calculateDamage(BattleUnit attacker, BattleUnit defender, Double skillMultiplier, boolean canCritical);
    
    /**
     * 尝试逃跑
     * @param playerSpeed 玩家速度
     * @param monsterSpeed 怪物速度
     * @return 是否逃跑成功
     */
    boolean tryEscape(Integer playerSpeed, Integer monsterSpeed);
    
    /**
     * 结束战斗
     * @param battleContext 战斗上下文
     * @return 战斗结果
     */
    BattleResult endBattle(BattleContext battleContext);
    
    /**
     * 战斗单位接口（统一玩家和怪物的战斗属性）
     */
    interface BattleUnit {
        Integer getHealth();
        void setHealth(Integer health);
        Integer getAttackPower();
        Integer getDefense();
        Integer getSpeed();
        java.math.BigDecimal getCriticalRate();
        java.math.BigDecimal getCriticalDamage();
        java.math.BigDecimal getArmorBreak();
        Integer getAttribute();
    }
}
