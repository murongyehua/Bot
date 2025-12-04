package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifeAchievement;
import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifePlayerAchievement;
import com.bot.life.dao.mapper.LifeAchievementMapper;
import com.bot.life.dao.mapper.LifePlayerAchievementMapper;
import com.bot.life.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 成就服务实现
 * @author Assistant
 */
@Service
public class AchievementServiceImpl implements AchievementService {
    
    @Autowired
    private LifeAchievementMapper achievementMapper;
    
    @Autowired
    private LifePlayerAchievementMapper playerAchievementMapper;
    
    @Override
    public List<String> checkAndTriggerAchievements(LifePlayer player) {
        List<String> newAchievements = new ArrayList<>();
        List<LifeAchievement> allAchievements = achievementMapper.selectAll();
        
        for (LifeAchievement achievement : allAchievements) {
            // 检查是否已经完成
            if (playerAchievementMapper.selectByPlayerIdAndAchievementId(player.getId(), achievement.getId()) != null) {
                continue;
            }
            
            // 检查是否满足条件
            if (checkAchievementCondition(player, achievement)) {
                // 完成成就
                completeAchievement(player.getId(), achievement.getId());
                
                String achievementText = String.format("『恭喜！』\n\n完成成就『%s』\n%s", 
                                                     achievement.getName(), 
                                                     achievement.getDescription());
                newAchievements.add(achievementText);
            }
        }
        
        return newAchievements;
    }
    
    @Override
    public String getPlayerAchievements(Long playerId) {
        List<LifePlayerAchievement> playerAchievements = playerAchievementMapper.selectByPlayerId(playerId);
        
        if (playerAchievements.isEmpty()) {
            return "『成就』\n\n暂无完成的成就";
        }
        
        StringBuilder display = new StringBuilder();
        display.append("『已完成成就』\n\n");
        
        for (LifePlayerAchievement playerAchievement : playerAchievements) {
            LifeAchievement achievement = achievementMapper.selectByPrimaryKey(playerAchievement.getAchievementId());
            if (achievement != null) {
                display.append("『").append(achievement.getName()).append("』\n");
                display.append(achievement.getDescription()).append("\n");
                display.append("完成时间：").append(formatDate(playerAchievement.getCompletedTime())).append("\n\n");
            }
        }
        
        return display.toString();
    }
    
    @Override
    public boolean isAchievementCompleted(LifePlayer player, Long achievementId) {
        return playerAchievementMapper.selectByPlayerIdAndAchievementId(player.getId(), achievementId) != null;
    }
    
    @Override
    public boolean completeAchievement(Long playerId, Long achievementId) {
        try {
            // 检查是否已经完成
            if (playerAchievementMapper.selectByPlayerIdAndAchievementId(playerId, achievementId) != null) {
                return false;
            }
            
            LifePlayerAchievement playerAchievement = new LifePlayerAchievement();
            playerAchievement.setPlayerId(playerId);
            playerAchievement.setAchievementId(achievementId);
            playerAchievement.setCompletedTime(new Date());
            
            playerAchievementMapper.insert(playerAchievement);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean checkAchievementCondition(LifePlayer player, LifeAchievement achievement) {
        try {
            String targetField = achievement.getConditionTarget();
            Long requiredValue = achievement.getConditionValue();
            
            // 使用反射获取玩家属性值
            Field field = LifePlayer.class.getDeclaredField(targetField);
            field.setAccessible(true);
            Object fieldValue = field.get(player);
            
            if (fieldValue instanceof Integer) {
                return ((Integer) fieldValue).longValue() >= requiredValue;
            } else if (fieldValue instanceof Long) {
                return (Long) fieldValue >= requiredValue;
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private String formatDate(Date date) {
        if (date == null) {
            return "未知";
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(date);
    }
}
