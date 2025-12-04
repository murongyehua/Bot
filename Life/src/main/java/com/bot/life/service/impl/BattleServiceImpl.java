package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifeMonster;
import com.bot.life.dao.entity.LifePlayerSkill;
import com.bot.life.dao.entity.LifeSkill;
import com.bot.life.dao.mapper.LifeSkillMapper;
import com.bot.life.dao.mapper.LifePlayerSkillMapper;
import com.bot.life.dto.BattleContext;
import com.bot.life.dto.BattleResult;
import com.bot.life.enums.ENAttribute;
import com.bot.life.enums.ENBattleAction;
import com.bot.life.enums.ENSkillType;
import com.bot.life.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * 战斗服务实现
 * @author Assistant
 */
@Service
public class BattleServiceImpl implements BattleService {
    
    @Autowired
    private LifeSkillMapper skillMapper;
    
    @Autowired
    private LifePlayerSkillMapper playerSkillMapper;
    
    private final Random random = new Random();
    
    @Override
    public BattleContext startBattle(LifePlayer player, LifeMonster monster, Integer battleType) {
        BattleContext context = new BattleContext();
        context.setPlayer(player);
        context.setMonster(monster);
        context.setBattleType(battleType);
        context.setPlayerEffects(new HashMap<>());
        context.setMonsterEffects(new HashMap<>());
        
        // 根据速度决定出手顺序
        context.setPlayerTurn(player.getSpeed() >= monster.getSpeed());
        
        context.addLog("『战斗开始！』");
        context.addLog(String.format("『%s』 VS 『%s』", player.getNickname(), monster.getName()));
        
        return context;
    }
    
    @Override
    public BattleContext executePlayerAction(BattleContext battleContext, ENBattleAction action, String actionParam) {
        LifePlayer player = battleContext.getPlayer();
        LifeMonster monster = battleContext.getMonster();
        
        switch (action) {
            case NORMAL_ATTACK:
                executeNormalAttack(battleContext, new PlayerBattleUnit(player), new MonsterBattleUnit(monster));
                break;
            case USE_SKILL:
                // TODO: 实现技能使用
                battleContext.addLog("『使用技能功能待实现』");
                break;
            case DEFEND:
                battleContext.addLog(String.format("『%s』选择防御，本回合受到伤害减少50%%", player.getNickname()));
                break;
            case USE_ITEM:
                // TODO: 实现道具使用
                battleContext.addLog("『使用道具功能待实现』");
                break;
            case ESCAPE:
                boolean escaped = tryEscape(player.getSpeed(), monster.getSpeed());
                if (escaped) {
                    battleContext.addLog(String.format("『%s』成功逃跑！", player.getNickname()));
                    battleContext.setBattleEnded(true);
                    battleContext.setPlayerWin(false);
                } else {
                    battleContext.addLog(String.format("『%s』逃跑失败！", player.getNickname()));
                }
                break;
        }
        
        // 检查怪物是否死亡
        if (monster.getHealth() <= 0) {
            battleContext.addLog(String.format("『%s』被击败了！", monster.getName()));
            battleContext.setBattleEnded(true);
            battleContext.setPlayerWin(true);
        }
        
        battleContext.setPlayerTurn(false);
        return battleContext;
    }
    
    @Override
    public BattleContext executeMonsterAction(BattleContext battleContext) {
        LifePlayer player = battleContext.getPlayer();
        LifeMonster monster = battleContext.getMonster();
        
        // 简单AI：只会普通攻击
        executeNormalAttack(battleContext, new MonsterBattleUnit(monster), new PlayerBattleUnit(player));
        
        // 检查玩家是否死亡
        if (player.getHealth() <= 0) {
            battleContext.addLog(String.format("『%s』被击败了！", player.getNickname()));
            battleContext.setBattleEnded(true);
            battleContext.setPlayerWin(false);
        }
        
        battleContext.setPlayerTurn(true);
        battleContext.setCurrentRound(battleContext.getCurrentRound() + 1);
        return battleContext;
    }
    
    private void executeNormalAttack(BattleContext context, BattleUnit attacker, BattleUnit defender) {
        String attackerName = getUnitName(attacker, context);
        String defenderName = getUnitName(defender, context);
        
        Integer damage = calculateDamage(attacker, defender, 1.0, true);
        
        // 属性克制计算
        ENAttribute attackerAttr = ENAttribute.getByCode(attacker.getAttribute());
        ENAttribute defenderAttr = ENAttribute.getByCode(defender.getAttribute());
        
        if (attackerAttr.restrains(defenderAttr)) {
            damage = (int) (damage * 1.2); // 克制伤害增加20%
            context.addLog(String.format("『属性克制！』%s对%s造成额外伤害！", attackerAttr.getDesc(), defenderAttr.getDesc()));
        }
        
        defender.setHealth(Math.max(0, defender.getHealth() - damage));
        
        context.addLog(String.format("『%s』对『%s』造成了『%d』点伤害！", attackerName, defenderName, damage));
        context.addLog(String.format("『%s』剩余血量：『%d』", defenderName, defender.getHealth()));
    }
    
    private String getUnitName(BattleUnit unit, BattleContext context) {
        if (unit instanceof PlayerBattleUnit) {
            return context.getPlayer().getNickname();
        } else {
            return context.getMonster().getName();
        }
    }
    
    @Override
    public Integer calculateDamage(BattleUnit attacker, BattleUnit defender, Double skillMultiplier, boolean canCritical) {
        double attackPower = attacker.getAttackPower() * skillMultiplier;
        
        // 会心计算
        if (canCritical && random.nextDouble() * 100 < attacker.getCriticalRate().doubleValue()) {
            attackPower *= attacker.getCriticalDamage().doubleValue() / 100.0;
        }
        
        // 防御计算
        double armorBreakRate = Math.min(0.3, attacker.getArmorBreak().doubleValue() / 100.0); // 破防最高30%
        double effectiveDefense = defender.getDefense() * (1.0 - armorBreakRate);
        
        double finalDamage = attackPower - effectiveDefense;
        return Math.max(1, (int) finalDamage); // 最少造成1点伤害
    }
    
    @Override
    public boolean tryEscape(Integer playerSpeed, Integer monsterSpeed) {
        if (playerSpeed >= monsterSpeed) {
            return true; // 速度高于等于对方，必定逃跑成功
        }
        
        double baseSuccessRate = 0.5; // 基础成功率50%
        int speedDiff = monsterSpeed - playerSpeed;
        double penalty = (speedDiff / 10) * 0.02; // 每低10点速度，成功率下降2%
        
        double finalSuccessRate = Math.max(0.1, baseSuccessRate - penalty); // 最低10%成功率
        
        return random.nextDouble() < finalSuccessRate;
    }
    
    @Override
    public BattleResult endBattle(BattleContext battleContext) {
        BattleResult result = new BattleResult();
        result.setVictory(battleContext.isPlayerWin());
        result.setRoundsCount(battleContext.getCurrentRound());
        result.setBattleLog(battleContext.getBattleLog());
        
        // TODO: 计算奖励
        if (result.isVictory()) {
            result.setSpiritReward(100L); // 临时奖励
            result.setCultivationReward(500L);
        }
        
        return result;
    }
    
    // 玩家战斗单位适配器
    private static class PlayerBattleUnit implements BattleUnit {
        private final LifePlayer player;
        
        public PlayerBattleUnit(LifePlayer player) {
            this.player = player;
        }
        
        @Override
        public Integer getHealth() {
            return player.getHealth();
        }
        
        @Override
        public void setHealth(Integer health) {
            player.setHealth(health);
        }
        
        @Override
        public Integer getAttackPower() {
            return player.getAttackPower();
        }
        
        @Override
        public Integer getDefense() {
            return player.getDefense();
        }
        
        @Override
        public Integer getSpeed() {
            return player.getSpeed();
        }
        
        @Override
        public BigDecimal getCriticalRate() {
            return player.getCriticalRate();
        }
        
        @Override
        public BigDecimal getCriticalDamage() {
            return player.getCriticalDamage();
        }
        
        @Override
        public BigDecimal getArmorBreak() {
            return player.getArmorBreak();
        }
        
        @Override
        public Integer getAttribute() {
            return player.getAttribute();
        }
    }
    
    // 怪物战斗单位适配器
    private static class MonsterBattleUnit implements BattleUnit {
        private final LifeMonster monster;
        
        public MonsterBattleUnit(LifeMonster monster) {
            this.monster = monster;
        }
        
        @Override
        public Integer getHealth() {
            return monster.getHealth();
        }
        
        @Override
        public void setHealth(Integer health) {
            monster.setHealth(health);
        }
        
        @Override
        public Integer getAttackPower() {
            return monster.getAttackPower();
        }
        
        @Override
        public Integer getDefense() {
            return monster.getDefense();
        }
        
        @Override
        public Integer getSpeed() {
            return monster.getSpeed();
        }
        
        @Override
        public BigDecimal getCriticalRate() {
            return monster.getCriticalRate();
        }
        
        @Override
        public BigDecimal getCriticalDamage() {
            return monster.getCriticalDamage();
        }
        
        @Override
        public BigDecimal getArmorBreak() {
            return monster.getArmorBreak();
        }
        
        @Override
        public Integer getAttribute() {
            return monster.getAttribute();
        }
    }
}
