package com.bot.game.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.dto.CompensateDTO;
import com.bot.game.service.GameManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/11/2
 */
@Service
public class GameManagerServiceImpl implements GameManageService {

    @Autowired
    private PlayerGoodsMapper playerGoodsMapper;

    @Autowired
    private GamePlayerMapper gamePlayerMapper;

    @Override
    public String compensate(CompensateDTO compensate) {
        List<GamePlayer> players = gamePlayerMapper.selectBySoulPower(compensate.getSoulPowerStart(), compensate.getSoulPowerEnd());
        for (GamePlayer gamePlayer : players) {
            PlayerGoods param = new PlayerGoods();
            param.setPlayerId(gamePlayer.getId());
            param.setGoodId(compensate.getGoodsId());
            List<PlayerGoods> list = playerGoodsMapper.selectBySelective(param);
            PlayerGoods good = new PlayerGoods();
            good.setGoodId(compensate.getGoodsId());
            good.setPlayerId(gamePlayer.getId());
            good.setNumber(compensate.getNumber());
            if (CollectionUtil.isNotEmpty(list)) {
                good.setId(list.get(0).getId());
                good.setNumber(list.get(0).getNumber() + compensate.getNumber());
                playerGoodsMapper.updateByPrimaryKey(good);
                return BaseConsts.SystemManager.SUCCESS;
            }
            good.setId(IdUtil.simpleUUID());
            playerGoodsMapper.insert(good);
        }
        return BaseConsts.SystemManager.SUCCESS;
    }

    @Override
    public String compensateMoney(Integer money) {
        List<GamePlayer> list = gamePlayerMapper.getBySoulPowerDesc();
        list.forEach(x -> CommonPlayer.addOrSubMoney(x.getId(), money));
        return BaseConsts.SystemManager.SUCCESS;
    }
}
