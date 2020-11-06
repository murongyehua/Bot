package com.bot.game.enums;

import com.bot.commom.exception.BotException;
import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/11/5
 */
@Getter
public enum ENDungeon {
    //
    H01("H01", "混沌海", 25);

    String value;

    String label;

    Integer suggestLevel;

    ENDungeon(String value, String label, Integer suggestLevel) {
        this.value = value;
        this.label = label;
        this.suggestLevel = suggestLevel;
    }

    public ENDungeon getByValue(String value) {
        for (ENDungeon enDungeon : ENDungeon.values()) {
            if (enDungeon.value.equals(value)) {
                return enDungeon;
            }
        }
        throw new BotException("未知枚举值");
    }
}
