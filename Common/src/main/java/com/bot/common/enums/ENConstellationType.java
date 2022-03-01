package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENConstellationType {

    TODAY("today", "日"),
    WEEK("week", "周"),
    MONTH("month", "月"),
    YEAR("year", "年");

    private final String value;

    private final String label;

    public static String getValueByLabel(String label) {
        for (ENConstellationType constellationType : ENConstellationType.values()) {
            if (constellationType.label.equals(label)) {
                return constellationType.value;
            }
        }
        return null;
    }

}
