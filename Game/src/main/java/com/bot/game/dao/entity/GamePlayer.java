package com.bot.game.dao.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * bot_game_player
 * @author 
 */
@Data
public class GamePlayer implements Serializable {
    private String id;

    private String gameId;

    private String nickname;

    private Date regTime;

    private String status;

    private String appellation;

    private Integer soulPower;

    private String playerWeaponId;

    private Integer money;

    private static final long serialVersionUID = 1L;
}