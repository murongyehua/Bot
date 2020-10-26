package com.bot.game.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.bot.commom.enums.ENStatus;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.dto.ResultContext;
import com.bot.game.service.CheckReg;
import com.bot.game.service.GameCommonHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/10/26
 */
@Service
public class CheckRegServiceImpl implements CheckReg {

    @Autowired
    private GamePlayerMapper gamePlayerMapper;

    @Autowired
    private PlayerGoodsMapper playerGoodsMapper;

    @Override
    public boolean checkReg(String token) {
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        return gamePlayer == null;
    }

    @Override
    public ResultContext reg(String nickName) {
        ResultContext resultContext = new ResultContext();
        GamePlayer param = new GamePlayer();
        param.setNickname(nickName);
        List<GamePlayer> list = gamePlayerMapper.selectBySelective(param);
        if (CollectionUtil.isNotEmpty(list)) {
            resultContext.setCode(ENStatus.LOCK.getValue());
            resultContext.setInfo("昵称重复，请重新输入");
            return resultContext;
        }
        String id = IdUtil.simpleUUID();
        param.setSoulPower(1);
        param.setId(id);
        param.setStatus(ENStatus.NORMAL.getValue());
        param.setRegTime(new Date());
        param.setGameId(GameCommonHolder.GAMES.get(0).getId());
        gamePlayerMapper.insert(param);
        PlayerGoods playerGoods = new PlayerGoods();
        playerGoods.setId(IdUtil.simpleUUID());
        playerGoods.setNumber(3);
        playerGoods.setPlayerId(id);
        playerGoods.setGoodId("1");
        playerGoodsMapper.insert(playerGoods);
        resultContext.setCode(ENStatus.NORMAL.getValue());
        resultContext.setData(id);
        return resultContext;
    }

}
