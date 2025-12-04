package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifeMonster;
import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.mapper.LifeMonsterMapper;
import com.bot.life.dto.BattleContext;
import com.bot.life.service.BattleService;
import com.bot.life.service.ExplorationService;
import com.bot.life.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * 探索服务实现
 * @author Assistant
 */
@Service
public class ExplorationServiceImpl implements ExplorationService {
    
    @Autowired
    private LifeMonsterMapper monsterMapper;
    
    @Autowired
    private BattleService battleService;
    
    @Autowired
    private PlayerService playerService;
    
    private final Random random = new Random();
    
    @Override
    public String explore(LifePlayer player) {
        // 检查体力
        if (!hasEnoughStamina(player)) {
            return "体力不足，无法进行游历探索！\n\n体力每5分钟恢复1点，当前体力：" + player.getStamina() + "/" + player.getMaxStamina();
        }
        
        // 消耗体力
        consumeStamina(player, 1);
        
        // 随机事件概率
        int eventRoll = random.nextInt(100);
        
        if (eventRoll < 70) {
            // 70% 概率遭遇怪物
            return encounterMonsterEvent(player);
        } else if (eventRoll < 85) {
            // 15% 概率发现道具
            return findItemEvent(player);
        } else if (eventRoll < 95) {
            // 10% 概率遇到NPC
            return meetNpcEvent(player);
        } else {
            // 5% 概率特殊事件
            return specialEvent(player);
        }
    }
    
    @Override
    public BattleContext encounterMonster(LifePlayer player) {
        List<LifeMonster> monsters = monsterMapper.selectNormalMonstersByMapId(player.getCurrentMapId());
        if (monsters.isEmpty()) {
            return null;
        }
        
        // 随机选择一个怪物
        LifeMonster monster = monsters.get(random.nextInt(monsters.size()));
        
        // 创建怪物副本（避免修改原始数据）
        LifeMonster battleMonster = cloneMonster(monster);
        
        return battleService.startBattle(player, battleMonster, 1);
    }
    
    @Override
    public boolean hasEnoughStamina(LifePlayer player) {
        // 先尝试恢复体力
        player.recoverStamina();
        return player.getStamina() >= 1;
    }
    
    @Override
    public void consumeStamina(LifePlayer player, int amount) {
        player.setStamina(Math.max(0, player.getStamina() - amount));
        playerService.updatePlayer(player);
    }
    
    private String encounterMonsterEvent(LifePlayer player) {
        List<LifeMonster> monsters = monsterMapper.selectNormalMonstersByMapId(player.getCurrentMapId());
        if (monsters.isEmpty()) {
            // 即使没有怪物，也给予少量经验值
            boolean leveledUp = playerService.gainExperience(player, 10);
            String result = "『游历探索』\n\n你在这片区域游历了一番，但没有遇到任何怪物。\n\n获得经验值：10\n体力-1";
            if (leveledUp) {
                result += "\n\n『恭喜！』你升级了！";
            }
            return result;
        }
        
        LifeMonster monster = monsters.get(random.nextInt(monsters.size()));
        return String.format("『遭遇怪物！』\n\n你在游历中遇到了『%s』！\n\n是否要与它战斗？\n\n发送『战斗』开始战斗\n发送『逃跑』尝试逃跑", 
                           monster.getName());
    }
    
    private String findItemEvent(LifePlayer player) {
        // TODO: 实现发现道具逻辑
        String[] items = {"小修为丹", "回春丹", "灵石", "神秘卷轴"};
        String foundItem = items[random.nextInt(items.length)];
        
        return String.format("『发现宝物！』\n\n你在游历中发现了『%s』！\n\n（道具系统开发中，暂未实际获得）\n\n体力-1", foundItem);
    }
    
    private String meetNpcEvent(LifePlayer player) {
        String[] npcNames = {"神秘商人", "云游道士", "采药老人", "剑客"};
        String npcName = npcNames[random.nextInt(npcNames.length)];
        
        StringBuilder result = new StringBuilder();
        result.append("『遇到NPC！』\n\n");
        result.append(String.format("你在游历中遇到了『%s』！\n\n", npcName));
        
        // 根据不同NPC提供不同的交互
        switch (npcName) {
            case "神秘商人":
                result.append("神秘商人：「年轻的修士，我这里有些好东西，要不要看看？」\n\n");
                result.append("他向你展示了一些珍贵的道具...\n\n");
                // 随机给予道具或灵粹
                if (random.nextBoolean()) {
                    int spiritReward = random.nextInt(50) + 10; // 10-59灵粹
                    player.setSpirit((player.getSpirit() != null ? player.getSpirit() : 0) + spiritReward);
                    result.append(String.format("获得了 %d 灵粹！", spiritReward));
                } else {
                    result.append("获得了『小修为丹』一颗！");
                    // TODO: 实际添加道具到背包
                }
                break;
                
            case "云游道士":
                result.append("云游道士：「小友，看你修为不错，我来指点你几句。」\n\n");
                result.append("道士传授了你一些修炼心得...\n\n");
                // 获得修为奖励
                long cultivationReward = random.nextInt(1000) + 500; // 500-1499修为
                player.setCultivation(player.getCultivation() + cultivationReward);
                result.append(String.format("修为增加了 %d 点！", cultivationReward));
                break;
                
            case "采药老人":
                result.append("采药老人：「哎呀，这位小友面色不佳，来，服下这颗丹药。」\n\n");
                result.append("老人给了你一颗回春丹...\n\n");
                // 恢复血量
                int healAmount = random.nextInt(20) + 10; // 10-29血量
                int oldHealth = player.getHealth();
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
                int actualHeal = player.getHealth() - oldHealth;
                result.append(String.format("血量恢复了 %d 点！当前血量：%d/%d", actualHeal, player.getHealth(), player.getMaxHealth()));
                break;
                
            case "剑客":
                result.append("剑客：「小友，我看你资质不错，这本剑谱送给你了。」\n\n");
                result.append("剑客传授了你一些战斗技巧...\n\n");
                // 临时提升攻击力
                result.append("从剑客那里学到了战斗技巧，攻击力在下次战斗中将提升20%！");
                // TODO: 实现临时buff系统
                break;
        }
        
        result.append("\n\n体力-1");
        
        // 消耗体力
        player.setStamina(Math.max(0, player.getStamina() - 1));
        
        // 保存玩家状态
        playerService.updatePlayer(player);
        
        return result.toString();
    }
    
    private String specialEvent(LifePlayer player) {
        String[] events = {
            "『奇遇！』\n\n你发现了一处隐秘的修炼之地，在此修炼了一番。\n\n修为+1000\n体力-1",
            "『顿悟！』\n\n你在游历中突然有所感悟，境界有所提升！\n\n所有属性+1\n体力-1",
            "『灵泉！』\n\n你发现了一眼灵泉，饮用后精神大振！\n\n体力完全恢复\n体力-1（已抵消）"
        };
        
        String event = events[random.nextInt(events.length)];
        
        // 根据事件类型给予奖励
        if (event.contains("修为+1000")) {
            player.setCultivation(player.getCultivation() + 1000);
        } else if (event.contains("所有属性+1")) {
            player.setSpeed(player.getSpeed() + 1);
            player.setConstitution(player.getConstitution() + 1);
            player.setSpiritPower(player.getSpiritPower() + 1);
            player.setStrength(player.getStrength() + 1);
            player.calculateExtendedAttributes();
        } else if (event.contains("体力完全恢复")) {
            player.setStamina(player.getMaxStamina());
        }
        
        playerService.updatePlayer(player);
        return event;
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
