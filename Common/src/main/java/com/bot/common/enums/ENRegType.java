package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENRegType {

    PERSONNEL("1", "个人"),
    GROUP("2", "群");

    private final String value;

    private final String label;

}
