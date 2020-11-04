package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_player_weapon
 * @author 
 */
@Data
public class PlayerWeapon implements Serializable {
    private String id;

    private String playerId;

    private String weaponId;

    private Integer level;

    private static final long serialVersionUID = 1L;
}