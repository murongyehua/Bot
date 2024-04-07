package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENChineseNumber {

    ONE("1", "一"),
    TWO("2", "二"),
    THREE("3", "三"),
    FOUR("4", "四"),
    FIVE("5", "五"),
    SIX("6", "六"),
    SEVEN("0", "日");

    private final String value;

    private final String label;

    public static String getLabelByValue(String value) {
        for (ENChineseNumber chineseNumber : ENChineseNumber.values()) {
            if (chineseNumber.value.equals(value)) {
                return chineseNumber.label;
            }
        }
        return null;
    }

}
