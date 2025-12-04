package com.bot.life.enums;

/**
 * 游戏模式枚举
 * @author Assistant
 */
public enum ENGameMode {
    NOT_ENTERED(0, "未进入"),
    PREPARE(1, "预备状态"),
    IN_GAME(2, "正式游戏"),
    GHOST_MARKET(3, "鬼市"),
    BATTLE(4, "战斗中");

    private final int code;
    private final String desc;

    ENGameMode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ENGameMode getByCode(int code) {
        for (ENGameMode mode : values()) {
            if (mode.code == code) {
                return mode;
            }
        }
        return NOT_ENTERED;
    }
}
