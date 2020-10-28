package com.bot.game.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/16
 */
@Getter
public enum  ENRarity {

    //
    NORMAL("0", "普通", 11, 10),
    GOOD("1", "精英", 13, 15),
    GREAT("2", "珍稀", 15, 20),
    BEST("3", "绝世", 18, 25);


    private String value;

    private String label;

    private Integer maxGrow;

    private Integer power;

    ENRarity(String value, String label, Integer maxGrow, Integer power) {
        this.value = value;
        this.label = label;
        this.maxGrow = maxGrow;
        this.power = power;
    }

    public static String getLabelByValue(String value) {
        ENRarity[] enRarities = ENRarity.values();
        for (ENRarity enRarity : enRarities) {
            if (enRarity.getValue().equals(value)) {
                return enRarity.getLabel();
            }
        }
        return StrUtil.EMPTY;
    }

    public static ENRarity getByValue(String value) {
        for (ENRarity enRarity : ENRarity.values()) {
            if (enRarity.getValue().equals(value)) {
                return enRarity;
            }
        }
        return null;
    }

}
