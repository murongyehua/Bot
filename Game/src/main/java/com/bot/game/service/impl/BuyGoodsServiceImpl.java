package com.bot.game.service.impl;

import com.bot.commom.constant.GameConsts;
import com.bot.game.dao.entity.BaseGoods;
import com.bot.game.dao.mapper.BaseGoodsMapper;
import com.bot.game.enums.ENGoodEffect;

import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/11/8
 */
public class BuyGoodsServiceImpl extends CommonPlayer{

    private final ENGoodEffect enGoodEffect;

    public BuyGoodsServiceImpl(ENGoodEffect goodEffect) {
        this.enGoodEffect = goodEffect;
        this.title = String.format("%s,原价%s,现价%s", enGoodEffect.getLabel(), enGoodEffect.getMoney(), ((Double) (enGoodEffect.getMoney() * nowSale * 0.1)).intValue());
    }

    @Override
    public String doPlay(String token) {
        int hasMoney = getMoney(token);
        if (hasMoney < ((Double) (enGoodEffect.getMoney() * nowSale * 0.1)).intValue()) {
            return GameConsts.Shop.NO_MONEY;
        }
        BaseGoodsMapper baseGoodsMapper = (BaseGoodsMapper) mapperMap.get(GameConsts.MapperName.BASE_GOODS);
        BaseGoods param = new BaseGoods();
        param.setEffect(enGoodEffect.getValue());
        List<BaseGoods> list = baseGoodsMapper.selectBySelective(param);
        CommonPlayer.addPlayerGoods(list.get(0).getId(), token, 1);
        CommonPlayer.addOrSubMoney(token, -(((Double) (enGoodEffect.getMoney() * nowSale * 0.1)).intValue()));
        return String.format(GameConsts.Shop.BUY_SUCCESS, ((Double) (enGoodEffect.getMoney() * nowSale * 0.1)).intValue(), CommonPlayer.getMoney(token));
    }

}
