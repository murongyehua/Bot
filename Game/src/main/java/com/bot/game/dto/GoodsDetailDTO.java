package com.bot.game.dto;

import lombok.Data;

/**
 * @author liul
 * @version 1.0 2020/10/16
 */
@Data
public class GoodsDetailDTO {

    private String playerGoodsId;

    private String token;

    private String goodsId;

    private String name;

    private Integer number;

    private String describe;

    private String effect;

    private String targetId;


}
