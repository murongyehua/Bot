package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifeRealmConfig;
import com.bot.life.dao.mapper.LifePlayerMapper;
import com.bot.life.dao.mapper.LifeRealmConfigMapper;
import com.bot.life.service.PlayerService;
import com.bot.life.service.AchievementService;
import com.bot.life.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 玩家服务实现
 * @author Assistant
 */
@Service
public class PlayerServiceImpl implements PlayerService {
    
    @Autowired
    private LifePlayerMapper playerMapper;
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private SkillService skillService;
    
    @Autowired
    private LifeRealmConfigMapper realmConfigMapper;
    
    @Override
    public LifePlayer getPlayerByUserId(String userId) {
        return playerMapper.selectByUserId(userId);
    }
    
    @Override
    public boolean createPlayer(String userId, String nickname, Integer attribute) {
        // 检查是否已存在角色
        if (getPlayerByUserId(userId) != null) {
            return false;
        }
        
        // 检查昵称是否可用
        if (!isNicknameAvailable(nickname)) {
            return false;
        }
        
        // 创建新角色
        LifePlayer player = new LifePlayer();
        player.setUserId(userId);
        player.setNickname(nickname);
        player.setAttribute(attribute);
        player.setLevel(1);
        player.setCultivation(0L);
        player.setCultivationSpeed(10);
        player.setLastCultivationTime(new Date());
        
        // 初始化基础属性
        player.setSpeed(1);
        player.setConstitution(1);
        player.setSpiritPower(1);
        player.setStrength(1);
        
        // 初始化拓展属性
        player.setHealth(10);
        player.setMaxHealth(10);
        player.setDefense(1);
        player.setCriticalRate(BigDecimal.ZERO);
        player.setCriticalDamage(BigDecimal.valueOf(110));
        player.setArmorBreak(BigDecimal.ZERO);
        player.setAttackPower(6);
        
        // 初始化体力
        player.setStamina(100);
        player.setMaxStamina(100);
        player.setLastStaminaTime(new Date());
        
        // 初始化灵粹
        player.setSpirit(1000L);
        
        // 设置默认地图
        player.setCurrentMapId(1L);
        player.setGameStatus(0);
        
        player.setCreateTime(new Date());
        player.setUpdateTime(new Date());
        
        // 计算拓展属性
        player.calculateExtendedAttributes();
        
        try {
            playerMapper.insert(player);
            
            // 检查成就
            achievementService.checkAndTriggerAchievements(player);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updatePlayer(LifePlayer player) {
        try {
            player.setUpdateTime(new Date());
            playerMapper.updateByPrimaryKey(player);
            
            // 检查成就
            List<String> newAchievements = achievementService.checkAndTriggerAchievements(player);
            // TODO: 这里可以将新成就信息存储到上下文中，在下次响应时展示
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean isNicknameAvailable(String nickname) {
        return playerMapper.selectByNickname(nickname) == null;
    }
    
    @Override
    public String getPlayerStatusDescription(String userId) {
        LifePlayer player = getPlayerByUserId(userId);
        if (player == null) {
            return "角色不存在，请先创建角色！";
        }
        
        // 获取当前境界配置
        LifeRealmConfig currentRealm = realmConfigMapper.selectByLevel(player.getLevel());
        Long maxCultivation = currentRealm != null ? currentRealm.getMaxCultivation() : null;
        
        // 更新修为（带上限检查）和体力
        long gainedCultivation = player.gainCultivation(maxCultivation);
        player.recoverStamina();
        updatePlayer(player);
        
        StringBuilder status = new StringBuilder();
        status.append("『").append(player.getNickname()).append("』\n");
        status.append("境界：").append(getRealmName(player.getLevel())).append("\n");
        status.append("修为：").append(formatNumber(player.getCultivation())).append("\n");
        status.append("所在地图：").append(getMapName(player.getCurrentMapId()));
        
        return status.toString();
    }
    
    @Override
    public LifePlayer getPlayerByNickname(String nickname) {
        return playerMapper.selectByNickname(nickname);
    }
    
    @Override
    public LifePlayer getPlayerById(Long playerId) {
        return playerMapper.selectByPrimaryKey(playerId);
    }
    
    private String getRealmName(Integer level) {
        LifeRealmConfig realm = realmConfigMapper.selectByLevel(level);
        return realm != null ? realm.getRealmName() : "未知境界";
    }
    
    private String getMapName(Long mapId) {
        // TODO: 从数据库获取地图名称
        return "新手村";
    }
    
    private String formatNumber(Long number) {
        if (number < 10000) {
            return number.toString();
        } else if (number < 100000000) {
            return String.format("%.1f万", number / 10000.0);
        } else {
            return String.format("%.1f亿", number / 100000000.0);
        }
    }
    
    @Override
    public boolean gainExperience(LifePlayer player, long expGain) {
        long currentExp = player.getExperience() != null ? player.getExperience() : 0;
        long newExp = currentExp + expGain;
        player.setExperience(newExp);
        
        boolean leveledUp = false;
        int currentLevel = player.getLevel();
        
        // 检查是否能升级
        while (newExp >= getNextLevelExperience(currentLevel)) {
            newExp -= getNextLevelExperience(currentLevel);
            currentLevel++;
            leveledUp = true;
            
            // 升级时增加属性点
            player.setSpeed(player.getSpeed() + 2);
            player.setConstitution(player.getConstitution() + 3);
            player.setSpiritPower(player.getSpiritPower() + 2);
            player.setStrength(player.getStrength() + 2);
            
            // 更新扩展属性
            player.setMaxHealth(player.getMaxHealth() + 5);
            player.setHealth(player.getMaxHealth()); // 升级时满血
            player.setAttackPower(player.getAttackPower() + 3);
            player.setDefense(player.getDefense() + 1);
        }
        
        player.setLevel(currentLevel);
        player.setExperience(newExp);
        
        if (leveledUp) {
            updatePlayer(player);
        }
        
        return leveledUp;
    }
    
    @Override
    public long getNextLevelExperience(int currentLevel) {
        int nextLevel = currentLevel + 1;
        return nextLevel * 1000L + nextLevel * nextLevel * 500L;
    }
}
