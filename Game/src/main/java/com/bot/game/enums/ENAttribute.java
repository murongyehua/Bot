package com.bot.game.enums;

import lombok.Getter;

/**
 * @author murongyehua
 * @version 1.0 2020/10/18
 */
@Getter
public enum ENAttribute {

    //
    JIN("金", "木", "火"),
    MU("木", "土", "金"),
    SHUI("水", "火", "土"),
    HUO("火", "金", "水"),
    TU("土", "水", "木");

    private String value;

    private String buff;

    private String deBuff;

    ENAttribute(String value, String buff, String deBuff) {
        this.value = value;
        this.buff = buff;
        this.deBuff = deBuff;
    }

    public static boolean isBuff(String myValue, String targetValue) {
        ENAttribute attribute = getByValue(myValue);
        return targetValue.equals(attribute != null ? attribute.buff : null);
    }

    public static boolean isDeBuff(String myValue, String targetValue) {
        ENAttribute attribute = getByValue(myValue);
        return targetValue.equals(attribute != null ? attribute.deBuff : null);
    }

    public static ENAttribute getByValue(String value) {
        for (ENAttribute attribute : ENAttribute.values()) {
            if (attribute.value.equals(value)) {
                return attribute;
            }
        }
        return null;
    }
}
