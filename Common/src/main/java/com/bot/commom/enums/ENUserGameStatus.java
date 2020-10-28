package com.bot.commom.enums;

import lombok.Getter;

/**
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
@Getter
public enum  ENUserGameStatus {
    /**
     * 用户游戏状态
     */
    WAIT_JOIN("0", " 等待加入"),
    JOINED("1", "已加入")
    ;

    private String value;

    private String label;

    ENUserGameStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
