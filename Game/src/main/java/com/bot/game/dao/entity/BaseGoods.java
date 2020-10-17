package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_base_goods
 * @author 
 */
@Data
public class BaseGoods implements Serializable {
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

    /**
     * 产地(阵营、活动、区域)
     */
    private String origin;

    /**
     * 产出权重，同一产地权重越大，掉落的几率越大，1-10，为0时不掉落
     */
    private String weight;

    private static final long serialVersionUID = 1L;
}