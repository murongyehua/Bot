package com.bot.game.service;

import com.bot.game.dto.ResultContext;

/**
 * @author murongyehua
 * @version 1.0 2020/10/26
 */
public interface CheckReg {
    /**
     * 是否注册
     * @param token
     * @return
     */
    boolean checkReg(String token);

    /**
     * 注册
     * @param nickName
     * @return
     */
    ResultContext reg(String nickName);

}
