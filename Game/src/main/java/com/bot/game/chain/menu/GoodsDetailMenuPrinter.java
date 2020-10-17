package com.bot.game.chain.menu;

import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dto.GoodsDetailDTO;
import com.bot.game.dto.UseGoodsDTO;
import com.bot.game.service.impl.UseGoodsServiceImpl;

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
        this.playServiceMap.put(BaseConsts.Menu.ONE, new UseGoodsServiceImpl(this.getUseGoodsDTO(token, goodsDetailDTO.getGoodsId())));
    }

    private UseGoodsDTO getUseGoodsDTO(String token, String goodsId) {
        UseGoodsDTO useGoodsDTO = new UseGoodsDTO();
        useGoodsDTO.setToken(token);
        useGoodsDTO.setGoodsId(goodsId);
        useGoodsDTO.setTitle(GameConsts.GoodsDetail.USE);
        return useGoodsDTO;
    }
}
