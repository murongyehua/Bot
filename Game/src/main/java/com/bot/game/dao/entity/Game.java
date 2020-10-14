package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_game
 * @author 
 */
@Data
public class Game implements Serializable {
    private String id;

    private String gameName;

    private String keywords;

    private String status;

    private static final long serialVersionUID = 1L;
}