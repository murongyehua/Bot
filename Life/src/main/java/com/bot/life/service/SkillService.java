package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;

/**
 * 技能服务接口
 * @author Assistant
 */
public interface SkillService {
    
    /**
     * 获取玩家技能列表
     * @param player 玩家信息
     * @return 技能列表描述
     */
    String getPlayerSkills(LifePlayer player);
    
    /**
     * 学习技能
     * @param player 玩家信息
     * @param skillId 技能ID
     * @return 学习结果
     */
    String learnSkill(LifePlayer player, Long skillId);
    
    /**
     * 升级技能
     * @param player 玩家信息
     * @param skillId 技能ID
     * @return 升级结果
     */
    String upgradeSkill(LifePlayer player, Long skillId);
    
    /**
     * 检查技能是否可用（冷却时间）
     * @param playerId 玩家ID
     * @param skillId 技能ID
     * @return 是否可用
     */
    boolean isSkillAvailable(Long playerId, Long skillId);
    
    /**
     * 使用技能后设置冷却时间
     * @param playerId 玩家ID
     * @param skillId 技能ID
     */
    void setSkillCooldown(Long playerId, Long skillId);
    
    /**
     * 获取可学习的技能列表
     * @param player 玩家信息
     * @return 可学习技能列表
     */
    String getAvailableSkills(LifePlayer player);
    
    /**
     * 初始化玩家基础技能
     * @param playerId 玩家ID
     * @param attribute 玩家属性
     */
    void initPlayerBasicSkills(Long playerId, Integer attribute);
}
