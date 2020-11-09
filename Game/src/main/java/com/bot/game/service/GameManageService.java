package com.bot.game.service;

import com.bot.game.dto.CompensateDTO;

/**
 * @author murongyehua
 * @version 1.0 2020/11/2
 */
public interface GameManageService {

    String compensate(CompensateDTO compensate);

    String compensateMoney(Integer money);

}
