package com.bot.common.enums;

import lombok.Getter;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Getter
public enum ENStatus {

    //
    NORMAL("0", "正常"),
    LOCK("1", "锁定");

    private String value;

    private String label;

    ENStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
