package com.bot.game.dao.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * bot_player_appellation
 * @author 
 */
@Data
public class PlayerAppellation implements Serializable {
    private String id;

    private String playerId;

    private String appellation;

    private Date getTime;

    private static final long serialVersionUID = 1L;
}