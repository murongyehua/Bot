package com.bot.game.service.impl;

import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.service.CheckReg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liul
 * @version 1.0 2020/10/26
 */
@Service
public class CheckRegServiceImpl implements CheckReg {

    @Autowired
    private GamePlayerMapper gamePlayerMapper;

    @Override
    public boolean checkReg(String token) {
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        return gamePlayer == null;
    }

}
