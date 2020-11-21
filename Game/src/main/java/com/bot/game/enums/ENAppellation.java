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
    A03("山海乾坤"),
    A04("人生赢家"),
    A05("小资生活"),
    A06("家缠万贯"),
    A07("富可敌国"),
    A08("登坛先锋"),
    A09("救世主"),
    A10("珠光宝气"),
    A11("友情至上");

    private final String appellation;

    ENAppellation(String appellation) {
        this.appellation = appellation;
    }
}
