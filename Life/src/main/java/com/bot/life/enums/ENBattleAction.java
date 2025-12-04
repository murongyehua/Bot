package com.bot.life.enums;

/**
 * 战斗行动枚举
 * @author Assistant
 */
public enum ENBattleAction {
    NORMAL_ATTACK(1, "普通攻击"),
    USE_SKILL(2, "使用技能"),
    DEFEND(3, "防御"),
    USE_ITEM(4, "使用道具"),
    ESCAPE(5, "逃跑");

    private final int code;
    private final String desc;

    ENBattleAction(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ENBattleAction getByCode(int code) {
        for (ENBattleAction action : values()) {
            if (action.code == code) {
                return action;
            }
        }
        return NORMAL_ATTACK;
    }
}
