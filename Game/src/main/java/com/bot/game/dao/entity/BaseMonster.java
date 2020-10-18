package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_base_monster
 * @author 
 */
@Data
public class BaseMonster implements Serializable {
    private String id;

    private String name;

    private String attribute;

    private String area;

    private Integer attack;

    private Integer speed;

    private Integer physique;

    private Integer grow;

    private Integer level;

    private String skills;

    private String describe;

    private static final long serialVersionUID = 1L;
}