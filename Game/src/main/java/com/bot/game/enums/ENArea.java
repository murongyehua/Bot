package com.bot.game.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/10/17
 */
@Getter
public enum ENArea {

    //
    EAST_SEA("S01", "东海"),
    ZY_M("M01", "招摇山"),
    JYZZ_Z("Z01", "即翼之泽"),
    FY_M("M02", "浮玉山");

    private String value;

    private String label;

    ENArea(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public static String getLabelByValue(String value) {
        ENArea[] enAreas = ENArea.values();
        for (ENArea enArea : enAreas) {
            if (enArea.value.equals(value)) {
                return enArea.label;
            }
        }
        return StrUtil.EMPTY;
    }

    public static ENArea getByValue(String value) {
        for (ENArea enArea : ENArea.values()) {
            if (enArea.value.equals(value)) {
                return enArea;
            }
        }
        return null;
    }

}
