package com.bot.life.service;

/**
 * 签到服务接口
 * @author Assistant
 */
public interface SigninService {
    
    /**
     * 玩家签到
     * @param playerId 玩家ID
     * @return 签到结果描述
     */
    String signin(Long playerId);
    
    /**
     * 检查玩家今日是否已签到
     * @param playerId 玩家ID
     * @return 是否已签到
     */
    boolean hasSignedToday(Long playerId);
}
