package com.bot.game.service;

/**
 * @author liul
 * @version 1.0 2020/10/15
 */
public interface Player {

    /**
     * 真正执行的操作 各子类实现
     * @param token
     * @return
     */
    String doPlay(String token);

}
