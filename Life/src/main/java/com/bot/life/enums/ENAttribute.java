package com.bot.life.enums;

/**
 * 属性枚举
 * @author Assistant
 */
public enum ENAttribute {
    NONE(0, "无属性"),
    METAL(1, "金"),
    WOOD(2, "木"),
    WATER(3, "水"),
    FIRE(4, "火"),
    EARTH(5, "土");

    private final int code;
    private final String desc;

    ENAttribute(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ENAttribute getByCode(int code) {
        for (ENAttribute attribute : values()) {
            if (attribute.code == code) {
                return attribute;
            }
        }
        return NONE;
    }

    /**
     * 获取克制关系
     * @param target 目标属性
     * @return true表示克制目标属性
     */
    public boolean restrains(ENAttribute target) {
        if (this == NONE || target == NONE) {
            return false;
        }
        return (this == METAL && target == WOOD) ||
               (this == WOOD && target == EARTH) ||
               (this == EARTH && target == WATER) ||
               (this == WATER && target == FIRE) ||
               (this == FIRE && target == METAL);
    }

    /**
     * 获取被克制关系
     * @param attacker 攻击者属性
     * @return true表示被攻击者属性克制
     */
    public boolean restrainedBy(ENAttribute attacker) {
        return attacker.restrains(this);
    }
}
