package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_base_skill
 * @author 
 */
@Data
public class BaseSkill implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String describe;

    /**
     * 效果
     */
    private String effect;

    /**
     * debuff
     */
    private String debuff;

    /**
     * 有效回合
     */
    private Integer round;

    /**
     * 数值
     */
    private String figure;

    /**
     * 属性
     */
    private String attribute;

    /**
     * 冷却时间
     */
    private Integer waitRound;

    private static final long serialVersionUID = 1L;
}