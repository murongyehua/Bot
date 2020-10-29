package com.bot.game.enums;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/29
 */
@Getter
public enum  ENPhantomAttribute {
    //
    ATTACK("attack", "攻击"),
    SPEED("speed", "速度"),
    PHYSIQUE("physique", "体质");

    private String value;

    private String label;

    ENPhantomAttribute(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public static List<ENPhantomAttribute> getWithOutOne(ENPhantomAttribute enPhantomAttribute) {
        List<ENPhantomAttribute> result = new LinkedList<>();
        for (ENPhantomAttribute phantomAttribute : ENPhantomAttribute.values()) {
            if (!phantomAttribute.getValue().equals(enPhantomAttribute.getValue())) {
                result.add(phantomAttribute);
            }
        }
        return result;
    }
}
