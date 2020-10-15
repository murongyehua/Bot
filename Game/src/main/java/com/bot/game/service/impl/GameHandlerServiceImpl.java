package com.bot.game.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.commom.enums.ENStatus;
import com.bot.game.chain.Collector;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.service.GameCommonHolder;
import com.bot.game.service.GameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

    private final static List<String> WAIT_REG = new LinkedList<>();

    private final static List<String> WAIT_LOGIN = new LinkedList<>();

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
        // 已处于待登录状态
        if (WAIT_LOGIN.contains(token)) {
            if (BaseConsts.Menu.ONE.equals(reqContent)) {
                WAIT_LOGIN.remove(token);
                return collector.buildCollector(token);
            }else {
                return GameConsts.CommonTip.UN_KNOW_POINT;
            }
        }
        // 已处于待注册状态
        if (WAIT_REG.contains(token)) {
            if (this.isExsitName(reqContent)) {
                return GameConsts.CommonTip.REPEAT_REG;
            }
            gamePlayerMapper.insert(this.getGamePlayer(token, reqContent));
            WAIT_REG.remove(token);
            return String.format(GameConsts.CommonTip.LOGIN_TIP, reqContent);
        }
        // 判断是注册还是登录
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        if (gamePlayer == null) {
            WAIT_REG.add(token);
            return GameConsts.CommonTip.REG_TIP;
        }else {
            WAIT_LOGIN.add(token);
            return String.format(GameConsts.CommonTip.LOGIN_TIP, gamePlayer.getNickname());
        }
    }

    private GamePlayer getGamePlayer(String token, String nickName) {
        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setId(token);
        gamePlayer.setGameId(GameCommonHolder.GAMES.get(0).getId());
        gamePlayer.setNickname(nickName);
        gamePlayer.setRegTime(new Date());
        gamePlayer.setSoulPower(1);
        gamePlayer.setStatus(ENStatus.NORMAL.getValue());
        return gamePlayer;
    }

    private boolean isExsitName(String nickName) {
        GamePlayer param = new GamePlayer();
        param.setNickname(nickName);
        List<GamePlayer> list = gamePlayerMapper.selectBySelective(param);
        return CollectionUtil.isNotEmpty(list);
    }
}
