package com.bot.game.enums;

import lombok.Getter;

/**
 * @author murongyehua
 * @version 1.0 2020/10/20
 */
@Getter
public enum ENAppellation {
    //
    A01("初出茅庐"),
    A02("幸运天使"),
    A03("山海乾坤");

    private final String appellation;

    ENAppellation(String appellation) {
        this.appellation = appellation;
    }
}
