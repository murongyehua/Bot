package com.bot.game.chain.menu;

import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dto.GoodsDetailDTO;

/**
 * @author liul
 * @version 1.0 2020/10/16
 */
public class GoodsDetailMenuPrinter extends Menu {

    private GoodsDetailDTO goodsDetailDTO;

    GoodsDetailMenuPrinter(GoodsDetailDTO goodsDetailDTO) {
        this.goodsDetailDTO = goodsDetailDTO;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = String.format("%s[%s]", goodsDetailDTO.getName(), goodsDetailDTO.getNumber());
    }

    @Override
    public void getDescribe(String token) {
        this.describe = String.format(GameConsts.GoodsDetail.DESCRIBE,
                goodsDetailDTO.getName(), goodsDetailDTO.getNumber(), goodsDetailDTO.getDescribe());
        this.menuChildrenMap.put(BaseConsts.Menu.ONE, new UseGoodsPrinter(goodsDetailDTO));
    }
}
