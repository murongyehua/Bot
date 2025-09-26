package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENUserGoodType {

    MONEY("money", "碎玉"),
    PERSONAL("personal", "个人资格"),
    GROUP("group", "群聊资格");

    private String value;

    private String label;

    public static String getLabelByValue(String value) {
        for (ENUserGoodType enUserGoodType : ENUserGoodType.values()) {
            if (enUserGoodType.value.equals(value)) {
                return enUserGoodType.label;
            }
        }
        return "未知";
    }
}
