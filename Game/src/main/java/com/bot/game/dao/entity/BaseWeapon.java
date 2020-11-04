package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_base_weapon
 * @author 
 */
@Data
public class BaseWeapon implements Serializable {
    private String id;

    private String name;

    private String effect;

    private String describe;

    private static final long serialVersionUID = 1L;
}