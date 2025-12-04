package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifeRealmConfig;
import com.bot.life.dao.mapper.LifePlayerMapper;
import com.bot.life.dao.mapper.LifeRealmConfigMapper;
import com.bot.life.service.RealmService;
import com.bot.life.service.PlayerService;
import com.bot.life.service.AchievementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * 境界服务实现
 * @author Assistant
 */
@Service
public class RealmServiceImpl implements RealmService {
    
    @Autowired
    private LifePlayerMapper playerMapper;
    
    @Autowired
    private LifeRealmConfigMapper realmConfigMapper;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private AchievementService achievementService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    
    @Override
    public String attemptBreakthrough(Long playerId) {
        LifePlayer player = playerMapper.selectByPrimaryKey(playerId);
        if (player == null) {
            return "角色不存在！";
        }
        
        // 检查是否可以突破
        if (!canBreakthrough(player)) {
            LifeRealmConfig nextRealm = getNextRealm(player.getLevel());
            if (nextRealm == null) {
                return "『境界突破』\n\n已达最高境界『大乘期』，无法继续突破！";
            }
            
            return String.format("『境界突破』\n\n突破条件不足！\n\n需要修为：%s\n当前修为：%s\n成功率：%.1f%%",
                    formatNumber(nextRealm.getRequiredCultivation()),
                    formatNumber(player.getCultivation()),
                    nextRealm.getSuccessRate().doubleValue() * 100);
        }
        
        LifeRealmConfig nextRealm = getNextRealm(player.getLevel());
        if (nextRealm == null) {
            return "『境界突破』\n\n已达最高境界，无法继续突破！";
        }
        
        // 计算突破成功率
        double successRate = nextRealm.getSuccessRate().doubleValue();
        boolean success = random.nextDouble() < successRate;
        
        StringBuilder result = new StringBuilder();
        result.append("『境界突破』\n\n");
        
        if (success) {
            // 突破成功
            result.append("✨ 突破成功！✨\n\n");
            result.append("恭喜！成功突破到『").append(nextRealm.getRealmName()).append("』！\n\n");
            
            // 消耗修为
            player.setCultivation(player.getCultivation() - nextRealm.getRequiredCultivation());
            
            // 提升等级到下一境界的最低等级
            player.setLevel(nextRealm.getMinLevel());
            
            // 应用突破奖励
            applyBreakthroughBonus(player, nextRealm);
            
            // 更新时间
            player.setUpdateTime(new Date());
            
            // 保存到数据库
            playerMapper.updateByPrimaryKey(player);
            
            // 检查成就
            achievementService.checkAndTriggerAchievements(player);
            
            result.append("境界提升：").append(getRealmName(player.getLevel())).append("\n");
            result.append("剩余修为：").append(formatNumber(player.getCultivation())).append("\n\n");
            result.append("『突破奖励』\n").append(getAttributeBonusDescription(nextRealm.getAttributeBonus())).append("\n\n");
            result.append("『境界能力』\n").append(nextRealm.getSpecialAbilities());
            
        } else {
            // 突破失败
            result.append("💥 突破失败！💥\n\n");
            result.append("突破过程中出现意外，境界突破失败...\n\n");
            
            // 失败时损失部分修为（20%）
            long lostCultivation = nextRealm.getRequiredCultivation() / 5;
            player.setCultivation(Math.max(0, player.getCultivation() - lostCultivation));
            
            // 更新时间
            player.setUpdateTime(new Date());
            
            // 保存到数据库
            playerMapper.updateByPrimaryKey(player);
            
            result.append("损失修为：").append(formatNumber(lostCultivation)).append("\n");
            result.append("剩余修为：").append(formatNumber(player.getCultivation())).append("\n\n");
            result.append("不要灰心，继续修炼，下次一定能成功突破！");
        }
        
        return result.toString();
    }
    
    @Override
    public boolean canBreakthrough(LifePlayer player) {
        LifeRealmConfig nextRealm = getNextRealm(player.getLevel());
        if (nextRealm == null) {
            return false; // 已达最高境界
        }
        
        // 检查修为是否足够
        return player.getCultivation() >= nextRealm.getRequiredCultivation();
    }
    
    @Override
    public LifeRealmConfig getCurrentRealm(Integer level) {
        return realmConfigMapper.selectByLevel(level);
    }
    
    @Override
    public LifeRealmConfig getNextRealm(Integer currentLevel) {
        return realmConfigMapper.selectNextRealm(currentLevel);
    }
    
    @Override
    public String viewRealmInfo(Long playerId) {
        LifePlayer player = playerMapper.selectByPrimaryKey(playerId);
        if (player == null) {
            return "角色不存在！";
        }
        
        LifeRealmConfig currentRealm = getCurrentRealm(player.getLevel());
        LifeRealmConfig nextRealm = getNextRealm(player.getLevel());
        
        StringBuilder info = new StringBuilder();
        info.append("『境界信息』\n\n");
        
        // 当前境界信息
        if (currentRealm != null) {
            info.append("当前境界：").append(currentRealm.getRealmName()).append("\n");
            info.append("境界等级：").append(player.getLevel()).append("级\n");
            info.append("当前修为：").append(formatNumber(player.getCultivation())).append("\n");
            info.append("修为上限：").append(formatNumber(currentRealm.getMaxCultivation())).append("\n\n");
            info.append("『境界能力』\n").append(currentRealm.getSpecialAbilities()).append("\n\n");
            
            // 修为进度条
            double progress = (double) player.getCultivation() / currentRealm.getMaxCultivation() * 100;
            info.append("修为进度：").append(String.format("%.1f%%", progress)).append("\n");
            info.append(generateProgressBar(progress)).append("\n\n");
        }
        
        // 下一境界信息
        if (nextRealm != null) {
            info.append("『下一境界』\n");
            info.append("境界名称：").append(nextRealm.getRealmName()).append("\n");
            info.append("突破需要：").append(formatNumber(nextRealm.getRequiredCultivation())).append(" 修为\n");
            info.append("成功率：").append(String.format("%.1f%%", nextRealm.getSuccessRate().doubleValue() * 100)).append("\n");
            
            if (canBreakthrough(player)) {
                info.append("\n✨ 可以尝试突破！输入『突破』开始突破！");
            } else {
                long needed = nextRealm.getRequiredCultivation() - player.getCultivation();
                info.append("\n还需修为：").append(formatNumber(needed));
            }
        } else {
            info.append("『已达最高境界』\n");
            info.append("恭喜！您已达到修仙的巅峰境界！");
        }
        
        return info.toString();
    }
    
    @Override
    public void applyBreakthroughBonus(LifePlayer player, LifeRealmConfig realmConfig) {
        try {
            String bonusJson = realmConfig.getAttributeBonus();
            if (bonusJson == null || bonusJson.trim().isEmpty() || "{}".equals(bonusJson.trim())) {
                return;
            }
            
            Map<String, Object> bonus = objectMapper.readValue(bonusJson, new TypeReference<Map<String, Object>>() {});
            
            for (Map.Entry<String, Object> entry : bonus.entrySet()) {
                String attribute = entry.getKey();
                Integer value = Integer.valueOf(entry.getValue().toString());
                
                switch (attribute) {
                    case "speed":
                        player.setSpeed(player.getSpeed() + value);
                        break;
                    case "constitution":
                        player.setConstitution(player.getConstitution() + value);
                        break;
                    case "spirit_power":
                        player.setSpiritPower(player.getSpiritPower() + value);
                        break;
                    case "strength":
                        player.setStrength(player.getStrength() + value);
                        break;
                    case "cultivation_speed":
                        player.setCultivationSpeed(player.getCultivationSpeed() + value);
                        break;
                    default:
                        // 忽略未知属性
                        break;
                }
            }
            
            // 重新计算拓展属性（战斗属性）
            player.calculateExtendedAttributes();
            
        } catch (Exception e) {
            e.printStackTrace();
            // 如果解析失败，不影响突破流程
        }
    }
    
    private String getRealmName(Integer level) {
        LifeRealmConfig realm = getCurrentRealm(level);
        return realm != null ? realm.getRealmName() : "未知境界";
    }
    
    private String formatNumber(Long number) {
        if (number == null) return "0";
        if (number >= 100000000) return String.format("%.1f亿", number / 100000000.0);
        if (number >= 10000) return String.format("%.1f万", number / 10000.0);
        return number.toString();
    }
    
    private String getAttributeBonusDescription(String bonusJson) {
        try {
            if (bonusJson == null || bonusJson.trim().isEmpty() || "{}".equals(bonusJson.trim())) {
                return "无属性奖励";
            }
            
            Map<String, Object> bonus = objectMapper.readValue(bonusJson, new TypeReference<Map<String, Object>>() {});
            StringBuilder desc = new StringBuilder();
            
            for (Map.Entry<String, Object> entry : bonus.entrySet()) {
                String attribute = entry.getKey();
                Integer value = Integer.valueOf(entry.getValue().toString());
                
                switch (attribute) {
                    case "speed":
                        desc.append("速度 +").append(value).append("\n");
                        break;
                    case "constitution":
                        desc.append("体质 +").append(value).append("\n");
                        break;
                    case "spirit_power":
                        desc.append("灵力 +").append(value).append("\n");
                        break;
                    case "strength":
                        desc.append("力量 +").append(value).append("\n");
                        break;
                    case "cultivation_speed":
                        desc.append("修炼速度 +").append(value).append("/分钟\n");
                        break;
                }
            }
            
            return desc.length() > 0 ? desc.toString() : "无属性奖励";
            
        } catch (Exception e) {
            return "属性奖励解析失败";
        }
    }
    
    private String generateProgressBar(double progress) {
        int totalBars = 20;
        int filledBars = (int) (progress / 100.0 * totalBars);
        StringBuilder bar = new StringBuilder("[");
        
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");
        
        return bar.toString();
    }
}
