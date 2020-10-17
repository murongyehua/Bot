package com.bot.game.service.impl;

import com.bot.game.dto.UseGoodsDTO;

/**
 * @author liul
 * @version 1.0 2020/10/16
 */
public class UseGoodsServiceImpl extends CommonPlayer {

    private UseGoodsDTO useGoodsDTO;

    public UseGoodsServiceImpl(UseGoodsDTO useGoodsDTO) {
        this.useGoodsDTO = useGoodsDTO;
        this.title = useGoodsDTO.getTitle();
    }

    @Override
    public String doPlay(String token) {
        return null;
    }

}
