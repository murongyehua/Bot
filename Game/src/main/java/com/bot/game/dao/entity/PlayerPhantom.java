package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_player_phantom
 * @author 
 */
@Data
public class PlayerPhantom implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 玩家id
     */
    private String playerId;

    /**
     * 幻灵名称
     */
    private String name;

    /**
     * 幻灵等级
     */
    private Integer level;

    /**
     * 幻灵称号
     */
    private String appellation;

    /**
     * 幻灵稀有度
     */
    private String rarity;

    /**
     * 幻灵属性
     */
    private String attribute;

    /**
     * 幻灵阵营
     */
    private String camp;

    /**
     * 幻灵地区
     */
    private String area;

    /**
     * 幻灵速度
     */
    private Integer speed;

    /**
     * 幻灵攻击
     */
    private Integer attack;

    /**
     * 幻灵体质
     */
    private Integer physique;

    /**
     * 幻灵成长
     */
    private Integer grow;

    /**
     * 幻灵技能
     */
    private String skills;

    /**
     * 幻灵描述
     */
    private String describe;

    /**
     * 幻灵台词
     */
    private String lines;

    private static final long serialVersionUID = 1L;
}