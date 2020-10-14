package com.bot.game.dao.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * bot_player_friends
 * @author 
 */
@Data
public class PlayerFriends implements Serializable {
    private String id;

    private String playerId;

    private String friendId;

    private Date getTime;

    private static final long serialVersionUID = 1L;
}