package com.bot.game.enums;

import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/11/14
 */
@Getter
public enum  ENMessageStatus {
    //
    NOT_READ("0", "未查收"),
    READ("1", "已查收");

    private final String value;

    private final String label;

    ENMessageStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
