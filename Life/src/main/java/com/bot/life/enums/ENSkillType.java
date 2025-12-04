package com.bot.life.enums;

/**
 * 技能类型枚举
 * @author Assistant
 */
public enum ENSkillType {
    DIRECT_DAMAGE(1, "直接伤害类"),
    BUFF(2, "增益类"),
    DEBUFF(3, "减益类");

    private final int code;
    private final String desc;

    ENSkillType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ENSkillType getByCode(int code) {
        for (ENSkillType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
