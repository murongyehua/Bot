package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENRegDay {

    DAY("day", 7),
    MONTH("month", 30),
    SEASON("season", 90);

    private final String dayType;

    private final Integer datNumber;

    public static ENRegDay getRegDayByType(String type) {
        for (ENRegDay enRegDay : ENRegDay.values()) {
            if (enRegDay.dayType.equals(type)) {
                return enRegDay;
            }
        }
        return null;
    }

}
