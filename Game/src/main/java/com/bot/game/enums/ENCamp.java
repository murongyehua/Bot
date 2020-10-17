package com.bot.game.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/10/17
 */
@Getter
public enum ENCamp {
    //
    EAST_SEA("S01", "东海");

    private String value;

    private String label;

    ENCamp(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public static String getLabelByValue(String value) {
        ENCamp[] enCamps = ENCamp.values();
        for (ENCamp enCamp : enCamps) {
            if (enCamp.value.equals(value)) {
                return enCamp.label;
            }
        }
        return StrUtil.EMPTY;
    }

}
