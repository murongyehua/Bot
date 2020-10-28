package com.bot.game.dto;

import com.bot.game.dao.entity.PlayerPhantom;
import lombok.Data;

/**
 * @author murongyehua
 * @version 1.0 2020/10/16
 */
@Data
public class UseGoodsDTO{

    private static final long serialVersionUID = 4607332378877054905L;

    private String token;

    private String title;

    private String goodsId;

    private String effect;

    private String targetId;

    private PlayerPhantom playerPhantom;

    private Integer number;

    private String playerGoodsId;

}
