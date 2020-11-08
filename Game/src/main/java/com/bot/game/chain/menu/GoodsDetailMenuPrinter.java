package com.bot.game.chain.menu;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dto.GoodsDetailDTO;
import com.bot.game.enums.ENGoodEffect;

/**
 * @author murongyehua
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
        this.menuName = StrUtil.isEmpty(goodsDetailDTO.getAttribute()) ? String.format("%s[%s]", goodsDetailDTO.getName(), goodsDetailDTO.getNumber())
        : String.format("[%s]%s[%s]", goodsDetailDTO.getAttribute(), goodsDetailDTO.getName(), goodsDetailDTO.getNumber());
    }

    @Override
    public void getDescribe(String token) {
        this.describe = String.format(GameConsts.GoodsDetail.DESCRIBE,
                goodsDetailDTO.getName(), goodsDetailDTO.getNumber(), ENGoodEffect.getByValue(goodsDetailDTO.getEffect()).getMoney() / 5, goodsDetailDTO.getDescribe());
        this.menuChildrenMap.put(BaseConsts.Menu.ONE, new UseGoodsPrinter(goodsDetailDTO));
        this.menuChildrenMap.put(BaseConsts.Menu.TWO, new SaleGoodsPrinter(goodsDetailDTO));
    }
}
