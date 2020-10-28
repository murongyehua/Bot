package com.bot.game.enums;

import lombok.Getter;

/**
 * @author murongyehua
 * @version 1.0 2020/10/19
 */
@Getter
public enum  ENEffectType {
    //
    PRE("0", "前置"),
    END("1", "后置"),
    ATTACK("2","攻击时判断"),
    DEFENSE("3","受击时判断");


    private String value;

    private String label;

    ENEffectType(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
