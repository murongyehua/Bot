package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENMorningType {

    MORNING("morning", "早", "早上"),
    AFTERNOON("afternoon", "午", "中午"),
    EVENING("evening", "晚", "晚上"),
    ALL("morning,afternoon,evening", "全部", "全部");

    private final String value;

    private final String label;

    private final String full;

    public static String getValueByLabel(String label) {
        for (ENMorningType morningType : ENMorningType.values()) {
            if (morningType.label.equals(label)) {
                return morningType.value;
            }
        }
        return null;
    }

}
