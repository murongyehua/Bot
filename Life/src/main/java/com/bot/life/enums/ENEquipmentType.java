package com.bot.life.enums;

/**
 * 装备类型枚举
 * @author Assistant
 */
public enum ENEquipmentType {
    CULTIVATION_METHOD(1, "功法"),
    MENTAL_METHOD(2, "心法"),
    DIVINE_POWER(3, "神通"),
    TREASURE(4, "法宝");

    private final int code;
    private final String desc;

    ENEquipmentType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ENEquipmentType getByCode(int code) {
        for (ENEquipmentType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
