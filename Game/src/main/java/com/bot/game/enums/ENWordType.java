package com.bot.game.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENWordType {

    NORMAL("1", "普通词组", "⭕"),
    LIMIT("2", "限定词组", "🔰");

    private String type;

    private String label;
    
    private String icon;

    /**
     * 根据类型值获取标签
     */
    public static String getLabelByValue(String type) {
        if (type == null) {
            return NORMAL.label;
        }
        for (ENWordType wordType : ENWordType.values()) {
            if (wordType.type.equals(type)) {
                return wordType.label;
            }
        }
        return NORMAL.label;
    }

    /**
     * 根据类型值获取图标
     */
    public static String getIconByValue(String type) {
        if (type == null) {
            return NORMAL.icon;
        }
        for (ENWordType wordType : ENWordType.values()) {
            if (wordType.type.equals(type)) {
                return wordType.icon;
            }
        }
        return NORMAL.icon;
    }

}
