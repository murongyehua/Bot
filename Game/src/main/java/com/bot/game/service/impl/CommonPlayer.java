package com.bot.game.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.commom.exception.BotException;
import com.bot.game.dao.entity.BaseGoods;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.mapper.BaseGoodsMapper;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.enums.ENGoodEffect;
import com.bot.game.service.Player;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public static PlayerGoods isCanGetPhantom(String token) {
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        BaseGoodsMapper baseGoodsMapper = (BaseGoodsMapper) mapperMap.get(GameConsts.MapperName.BASE_GOODS);
        PlayerGoods playerGoods = new PlayerGoods();
        BaseGoods baseGoods = new BaseGoods();
        baseGoods.setEffect(ENGoodEffect.GET_PHANTOM.getValue());
        playerGoods.setPlayerId(token);
        playerGoods.setGoodId(baseGoodsMapper.selectBySelective(baseGoods).get(0).getId());
        List<PlayerGoods> list = playerGoodsMapper.selectBySelective(playerGoods);
        if (CollectionUtil.isEmpty(list)) {
            // 没有唤灵符
            return null;
        }
        return list.get(0);
    }

}
