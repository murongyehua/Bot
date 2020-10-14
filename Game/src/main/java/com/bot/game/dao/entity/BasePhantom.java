package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_base_goods
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
     * 效果
     */
    private String effect;

    /**
     * 数值
     */
    private String figure;

    /**
     * 描述
     */
    private String describe;

    /**
     * 有效期
     */
    private String termOfValidity;

    /**
     * 是否可用
     */
    private String used;

    private static final long serialVersionUID = 1L;
}