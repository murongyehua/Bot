package com.bot.life.enums;

/**
 * 道具类型枚举
 * @author Assistant
 */
public enum ENItemType {
    CULTIVATION(1, "修为类"),
    ATTRIBUTE(2, "属性类"),
    STAMINA(3, "体力类"),
    TREASURE_UPGRADE(4, "升级法宝类"),
    RECOVERY(5, "恢复类"),
    SKILL_BOOK(6, "技能书");

    private final int code;
    private final String desc;

    ENItemType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ENItemType getByCode(int code) {
        for (ENItemType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
