package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_goods_box
 * @author 
 */
@Data
public class GoodsBox implements Serializable {
    private String id;

    private String messageId;

    private String playerId;

    private String type;

    private String goodId;

    private Integer number;

    private String status;

    private static final long serialVersionUID = 1L;
}