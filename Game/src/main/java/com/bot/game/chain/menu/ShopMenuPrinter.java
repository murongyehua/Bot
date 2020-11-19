package com.bot.game.chain.menu;

import com.bot.common.constant.GameConsts;
import com.bot.common.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.enums.ENGoodEffect;
import com.bot.game.service.impl.BuyGoodsServiceImpl;
import com.bot.game.service.impl.CommonPlayer;

/**
 * @author murongyehua
 * @version 1.0 2020/11/8
 */
public class ShopMenuPrinter extends Menu {

    ShopMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.Shop.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        this.describe = String.format(GameConsts.Shop.DESCRIBE, CommonPlayer.nowSale, CommonPlayer.getMoney(token));
        for (int index = 0; index < ENGoodEffect.getCanSaleGoods().size(); index ++) {
            this.playServiceMap.put(IndexUtil.getIndex(index + 1), new BuyGoodsServiceImpl(ENGoodEffect.getCanSaleGoods().get(index)));
        }
    }

}
