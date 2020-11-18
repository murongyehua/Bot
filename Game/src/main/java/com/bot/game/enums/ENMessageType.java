package com.bot.game.enums;

import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/11/14
 */
@Getter
public enum ENMessageType {
    //
    SYSTEM("1",  "系统消息"),
    MESSAGE("2", "好友信息"),
    GOODS("3", "物品"),
    MONEY("4", "灵石")
    ;

    private final String value;

    private final String label;

    ENMessageType(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
