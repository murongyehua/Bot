package com.bot.game.chain.menu;

import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dto.GoodsDetailDTO;
import com.bot.game.enums.ENGoodEffect;
import com.bot.game.service.impl.CommonPlayer;

/**
 * @author murongyehua
 * @version 1.0 2020/11/8
 */
public class SaleGoodsPrinter extends Menu {

    private final GoodsDetailDTO goodsDetailDTO;

    SaleGoodsPrinter(GoodsDetailDTO goodsDetailDTO) {
        this.goodsDetailDTO = goodsDetailDTO;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.MyKnapsack.SALE_GOODS_MENU;
    }

    @Override
    public void getDescribe(String token) {
        // 要增加的灵石数
        int money = (ENGoodEffect.getByValue(goodsDetailDTO.getEffect()).getMoney() / 5) * goodsDetailDTO.getNumber();
        CommonPlayer.afterSaleGoods(goodsDetailDTO.getPlayerGoodsId());
        CommonPlayer.addOrSubMoney(token, money);
        this.describe = String.format(GameConsts.MyKnapsack.SALE_RESULT, goodsDetailDTO.getNumber(), goodsDetailDTO.getName(), money);
    }

}
