package com.bot.game.service.impl;

import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Collector;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.service.GameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liul
 * @version 1.0 2020/10/14
 */
@Service
public class GameHandlerServiceImpl implements GameHandler {

    @Autowired
    private Collector collector;

    @Autowired
    private GamePlayerMapper gamePlayerMapper;

    @Override
    public String exit(String token) {
        collector.removeToken(token);
        return GameConsts.CommonTip.EXIT_SUCCESS;
    }

    @Override
    public String play(String reqContent, String token) {
        // 先查是否已处于在线状态
        if (collector.isOnLine(token)) {
            return collector.toNextOrPrevious(token, reqContent.trim());
        }
        // 当前不在线，检查是登录还是注册
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        if (gamePlayer == null) {
            return "注册!";
        }else {
            return "登录!";
        }
    }
}
