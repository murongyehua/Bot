package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;

/**
 * 玩家服务接口
 * @author Assistant
 */
public interface PlayerService {
    
    /**
     * 根据用户ID获取玩家信息
     * @param userId 用户ID
     * @return 玩家信息
     */
    LifePlayer getPlayerByUserId(String userId);
    
    /**
     * 创建新角色
     * @param userId 用户ID
     * @param nickname 昵称
     * @param attribute 属性
     * @return 是否创建成功
     */
    boolean createPlayer(String userId, String nickname, Integer attribute);
    
    /**
     * 更新玩家信息
     * @param player 玩家信息
     * @return 是否更新成功
     */
    boolean updatePlayer(LifePlayer player);
    
    /**
     * 检查昵称是否可用
     * @param nickname 昵称
     * @return 是否可用
     */
    boolean isNicknameAvailable(String nickname);
    
    /**
     * 获取玩家当前状态描述
     * @param userId 用户ID
     * @return 状态描述
     */
    String getPlayerStatusDescription(String userId);
    
    /**
     * 根据昵称获取玩家信息
     * @param nickname 昵称
     * @return 玩家信息
     */
    LifePlayer getPlayerByNickname(String nickname);
    
    /**
     * 根据玩家ID获取玩家信息
     * @param playerId 玩家ID
     * @return 玩家信息
     */
    LifePlayer getPlayerById(Long playerId);
    
    /**
     * 增加经验值并检查是否升级
     * @param player 玩家对象
     * @param expGain 获得的经验值
     * @return 是否升级
     */
    boolean gainExperience(LifePlayer player, long expGain);
    
    /**
     * 计算下一等级所需经验值
     * @param currentLevel 当前等级
     * @return 下一等级所需经验值
     */
    long getNextLevelExperience(int currentLevel);
}
