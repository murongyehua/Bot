package com.bot.game.service;

/**
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
public interface GameHandler {
    /**
     * 退出
     * @param token
     * @return
     */
    String exit(String token);

    /**
     * 正常触发，可能是初次进入也可能是流程调用
     * @param reqContent
     * @param token
     * @return
     */
    String play(String reqContent, String token);
}
