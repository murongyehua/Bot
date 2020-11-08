package com.bot.game.enums;

/**
 * @author murongyehua
 * @version 1.0 2020/11/5
 */
public enum  ENDungeonResult {
    //
    SUCCESS("0", "成功"),
    FAIL("1", "失败"),
    WAIT("2","等待中");

    private String value;

    private String label;

    ENDungeonResult(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
