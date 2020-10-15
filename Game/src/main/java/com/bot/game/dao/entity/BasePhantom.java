package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_base_phantom
 * @author 
 */
@Data
public class BasePhantom implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 称号
     */
    private String appellation;

    /**
     * 稀有度
     */
    private String rarity;

    /**
     * 属性
     */
    private String attribute;

    /**
     * 阵营
     */
    private String camp;

    /**
     * 地区
     */
    private String area;

    /**
     * 速度
     */
    private Integer speed;

    /**
     * 攻击
     */
    private Integer attack;

    /**
     * 体质
     */
    private Integer physique;

    /**
     * 成长
     */
    private Integer grow;

    /**
     * 技能
     */
    private String skills;

    /**
     * 描述
     */
    private String describe;

    /**
     * 台词
     */
    private String line;

    private static final long serialVersionUID = 1L;
}