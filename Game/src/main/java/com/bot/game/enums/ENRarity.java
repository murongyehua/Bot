package com.bot.game.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/10/16
 */
@Getter
public enum  ENRarity {

    //
    NORMAL("0", "普通"),
    GOOD("1", "精英"),
    GREAT("2", "珍稀"),
    BEST("3", "绝世");


    private String value;

    private String label;

    ENRarity(String value, String label) {
        this.value = value;
        this.label = label;
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

}
