package com.bot.game.service.impl;

import com.bot.commom.exception.BotException;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.service.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * @author liul
 * @version 1.0 2020/10/15
 */
@Service
public class CommonPlayer implements Player {

    public String title;

    public static Map<String, Object> mapperMap;

    @Override
    public String doPlay(String token) {
        throw new BotException("子类实现");
    }

}
