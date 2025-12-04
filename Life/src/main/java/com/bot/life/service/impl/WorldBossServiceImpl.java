package com.bot.life.service.impl;

import com.bot.life.dao.entity.*;
import com.bot.life.dao.mapper.*;
import com.bot.life.dto.BattleResult;
import com.bot.life.service.BattleService;
import com.bot.life.service.WorldBossService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 世界BOSS服务实现
 * @author Assistant
 */
@Service
public class WorldBossServiceImpl implements WorldBossService {
    
    @Autowired
    private LifeWorldBossMapper worldBossMapper;
    
    @Autowired
    private LifeWorldBossRewardMapper worldBossRewardMapper;
    
    @Autowired
    private LifeWorldBossChallengeMapper worldBossChallengeMapper;
    
    @Autowired
    private LifeMonsterMapper monsterMapper;
    
    @Autowired
    private LifeMapMapper mapMapper;
    
    @Autowired
    private BattleService battleService;
    
    private final Random random = new Random();
    
    @Override
    public List<LifeWorldBoss> getCurrentActiveWorldBosses() {
        if (!isWorldBossActiveNow()) {
            return new ArrayList<>();
        }
        return worldBossMapper.selectActiveWorldBosses();
    }
    
    @Override
    public boolean canChallengeWorldBoss(LifePlayer player, Long worldBossId) {
        // 检查世界BOSS是否存在且活跃
        LifeWorldBoss worldBoss = worldBossMapper.selectByPrimaryKey(worldBossId);
        if (worldBoss == null || worldBoss.getIsActive() != 1) {
            return false;
        }
        
        // 检查时间是否在活动范围内
        if (!isWorldBossActiveNow()) {
            return false;
        }
        
        // 检查玩家是否在正确的地图
        if (!player.getCurrentMapId().equals(worldBoss.getMapId())) {
            return false;
        }
        
        // 检查今日挑战次数
        int todayChallengeCount = worldBossChallengeMapper.selectTodayChallengeCount(player.getId(), worldBossId);
        return todayChallengeCount < worldBoss.getMaxChallengeCount();
    }
    
    @Override
    public String challengeWorldBoss(LifePlayer player, Long worldBossId) {
        if (!canChallengeWorldBoss(player, worldBossId)) {
            return "无法挑战该世界BOSS！请检查时间、地点和挑战次数。";
        }
        
        LifeWorldBoss worldBoss = worldBossMapper.selectByPrimaryKey(worldBossId);
        LifeMonster bossMonster = monsterMapper.selectByPrimaryKey(worldBoss.getMonsterId());
        
        if (bossMonster == null) {
            return "世界BOSS数据异常！";
        }
        
        // 创建BOSS副本进行战斗
        LifeMonster battleBoss = cloneMonster(bossMonster);
        
        // 世界BOSS战斗是自动进行的，玩家只能普通攻击
        long totalDamage = simulateWorldBossBattle(player, battleBoss);
        
        // 根据伤害计算奖励
        LifeWorldBossReward reward = calculateReward(worldBossId, totalDamage);
        
        // 记录挑战
        recordChallenge(player.getId(), worldBossId, totalDamage, reward);
        
        // 返回结果
        StringBuilder result = new StringBuilder();
        result.append("『世界BOSS挑战完成！』\n\n");
        result.append(String.format("对『%s』造成伤害：%d\n\n", bossMonster.getName(), totalDamage));
        
        if (reward != null) {
            result.append("『获得奖励』\n");
            if (reward.getSpiritReward() > 0) {
                result.append(String.format("灵粹：%d\n", reward.getSpiritReward()));
            }
            if (reward.getItemRewards() != null && !reward.getItemRewards().isEmpty()) {
                result.append("道具：").append(reward.getItemRewards()).append("\n");
            }
        } else {
            result.append("伤害不足，未获得奖励。");
        }
        
        return result.toString();
    }
    
    @Override
    public String getWorldBossInfo(Long mapId) {
        List<LifeWorldBoss> worldBosses = worldBossMapper.selectByMapId(mapId);
        
        if (worldBosses.isEmpty()) {
            return "";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("『世界BOSS』\n\n");
        
        if (!isWorldBossActiveNow()) {
            info.append("世界BOSS活动时间：11:00-12:00, 19:00-20:00\n");
            info.append("当前不在活动时间内\n");
            return info.toString();
        }
        
        for (LifeWorldBoss worldBoss : worldBosses) {
            LifeMonster monster = monsterMapper.selectByPrimaryKey(worldBoss.getMonsterId());
            if (monster != null) {
                info.append(String.format("『%s』已出现！\n", monster.getName()));
                info.append(String.format("等级：%d  血量：%d\n", monster.getLevel(), monster.getHealth()));
                info.append(String.format("今日可挑战次数：%d\n", worldBoss.getMaxChallengeCount()));
                info.append("发送『挑战世界BOSS』参与战斗\n\n");
            }
        }
        
        return info.toString();
    }
    
    @Override
    public String getTodayWorldBossChallenges(Long playerId) {
        List<LifeWorldBossChallenge> challenges = worldBossChallengeMapper.selectTodayChallengesByPlayerId(playerId);
        
        if (challenges.isEmpty()) {
            return "『世界BOSS挑战记录』\n\n今日暂无挑战记录";
        }
        
        StringBuilder record = new StringBuilder();
        record.append("『今日世界BOSS挑战记录』\n\n");
        
        for (LifeWorldBossChallenge challenge : challenges) {
            LifeWorldBoss worldBoss = worldBossMapper.selectByPrimaryKey(challenge.getWorldBossId());
            if (worldBoss != null) {
                LifeMonster monster = monsterMapper.selectByPrimaryKey(worldBoss.getMonsterId());
                if (monster != null) {
                    record.append(String.format("『%s』\n", monster.getName()));
                    record.append(String.format("造成伤害：%d\n", challenge.getDamageDealt()));
                    if (challenge.getSpiritReward() > 0) {
                        record.append(String.format("获得灵粹：%d\n", challenge.getSpiritReward()));
                    }
                    record.append("\n");
                }
            }
        }
        
        return record.toString();
    }
    
    @Override
    public boolean isWorldBossActiveNow() {
        LocalTime now = LocalTime.now();
        
        // 设定世界BOSS活动时间：11:00-12:00 和 19:00-20:00
        LocalTime morning1 = LocalTime.of(11, 0);
        LocalTime morning2 = LocalTime.of(12, 0);
        LocalTime evening1 = LocalTime.of(19, 0);
        LocalTime evening2 = LocalTime.of(20, 0);
        
        return (now.isAfter(morning1) && now.isBefore(morning2)) ||
               (now.isAfter(evening1) && now.isBefore(evening2));
    }
    
    private long simulateWorldBossBattle(LifePlayer player, LifeMonster boss) {
        // 简化的世界BOSS战斗模拟
        // 玩家对BOSS造成的总伤害基于玩家属性和随机因素
        
        int baseAttack = player.getAttackPower();
        double attributeBonus = 1.0;
        
        // 属性克制加成
        if (isAttributeAdvantage(player.getAttribute(), boss.getAttribute())) {
            attributeBonus = 1.2;
        } else if (isAttributeDisadvantage(player.getAttribute(), boss.getAttribute())) {
            attributeBonus = 0.8;
        }
        
        // 模拟10回合攻击
        long totalDamage = 0;
        for (int round = 0; round < 10; round++) {
            double damage = baseAttack * attributeBonus;
            
            // 会心判断
            if (random.nextDouble() * 100 < player.getCriticalRate().doubleValue()) {
                damage *= player.getCriticalDamage().doubleValue() / 100.0;
            }
            
            // 随机波动 ±20%
            damage *= (0.8 + random.nextDouble() * 0.4);
            
            totalDamage += (long) Math.max(1, damage);
        }
        
        return totalDamage;
    }
    
    private boolean isAttributeAdvantage(Integer playerAttr, Integer bossAttr) {
        if (playerAttr == null || bossAttr == null) return false;
        // 金克木，木克土，土克水，水克火，火克金
        return (playerAttr == 1 && bossAttr == 2) ||
               (playerAttr == 2 && bossAttr == 5) ||
               (playerAttr == 5 && bossAttr == 3) ||
               (playerAttr == 3 && bossAttr == 4) ||
               (playerAttr == 4 && bossAttr == 1);
    }
    
    private boolean isAttributeDisadvantage(Integer playerAttr, Integer bossAttr) {
        return isAttributeAdvantage(bossAttr, playerAttr);
    }
    
    private LifeWorldBossReward calculateReward(Long worldBossId, long damage) {
        List<LifeWorldBossReward> rewards = worldBossRewardMapper.selectByWorldBossId(worldBossId);
        
        for (LifeWorldBossReward reward : rewards) {
            if (damage >= reward.getMinDamage() && damage <= reward.getMaxDamage()) {
                return reward;
            }
        }
        
        return null;
    }
    
    private void recordChallenge(Long playerId, Long worldBossId, long damage, LifeWorldBossReward reward) {
        try {
            LifeWorldBossChallenge challenge = new LifeWorldBossChallenge();
            challenge.setPlayerId(playerId);
            challenge.setWorldBossId(worldBossId);
            challenge.setDamageDealt(damage);
            challenge.setSpiritReward(reward != null ? reward.getSpiritReward() : 0);
            challenge.setItemRewards(reward != null ? reward.getItemRewards() : "");
            challenge.setChallengeTime(new Date());
            
            worldBossChallengeMapper.insert(challenge);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private LifeMonster cloneMonster(LifeMonster original) {
        LifeMonster clone = new LifeMonster();
        clone.setId(original.getId());
        clone.setName(original.getName());
        clone.setMapId(original.getMapId());
        clone.setMonsterType(original.getMonsterType());
        clone.setAttribute(original.getAttribute());
        clone.setLevel(original.getLevel());
        clone.setHealth(original.getHealth());
        clone.setAttackPower(original.getAttackPower());
        clone.setDefense(original.getDefense());
        clone.setSpeed(original.getSpeed());
        clone.setCriticalRate(original.getCriticalRate());
        clone.setCriticalDamage(original.getCriticalDamage());
        clone.setArmorBreak(original.getArmorBreak());
        return clone;
    }
}
