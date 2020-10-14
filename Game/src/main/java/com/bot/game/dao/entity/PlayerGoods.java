package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_player_goods
 * @author 
 */
@Data
public class PlayerGoods implements Serializable {
    private String id;

    private String playerId;

    private String goodId;

    private Integer number;

    private static final long serialVersionUID = 1L;
}