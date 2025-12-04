package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;

/**
 * 图片生成服务接口
 * @author Assistant
 */
public interface ImageGenerationService {
    
    /**
     * 生成游戏文本图片
     * @param content 文本内容
     * @return 图片文件路径
     */
    String generateGameImage(String content);
    
    /**
     * 生成带角色状态的游戏图片
     * @param content 文本内容
     * @param player 玩家信息
     * @return 图片文件路径
     */
    String generateGameImageWithStatus(String content, LifePlayer player);
    
    /**
     * 生成角色状态图片
     * @param userId 用户ID
     * @return 图片文件路径
     */
    String generatePlayerStatusImage(String userId);
    
    /**
     * 生成战斗结果图片
     * @param battleLog 战斗日志
     * @return 图片文件路径
     */
    String generateBattleResultImage(String battleLog);
}
