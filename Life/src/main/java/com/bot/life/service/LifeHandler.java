package com.bot.life.service;

/**
 * 浮生卷游戏处理接口
 * @author Assistant
 */
public interface LifeHandler {
    
    /**
     * 退出游戏
     * @param userId 用户ID
     * @return 响应消息
     */
    String exit(String userId);
    
    /**
     * 处理游戏指令
     * @param reqContent 请求内容
     * @param userId 用户ID
     * @return 响应消息
     */
    String play(String reqContent, String userId);
    
    /**
     * 管理员操作
     * @param reqContent 请求内容
     * @return 响应消息
     */
    String manage(String reqContent);
}
