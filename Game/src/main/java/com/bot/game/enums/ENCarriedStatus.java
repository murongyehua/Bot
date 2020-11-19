package com.bot.game.enums;

import com.bot.common.exception.BotException;
import lombok.Getter;

/**
 * @author murongyehua
 * @version 1.0 2020/11/12
 */
@Getter
public enum ENCarriedStatus {

    //
    NORMAL("0", "携带"),
    LOCK("1", "放置");

    private String value;

    private String label;

    ENCarriedStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public static ENCarriedStatus getInvertByValue(String value) {
        for (ENCarriedStatus enCarriedStatus : ENCarriedStatus.values()) {
            if (!enCarriedStatus.value.equals(value)) {
                return enCarriedStatus;
            }
        }
        throw new BotException("未知枚举值");
    }

}
