package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENRegStatus {

    TEMP("0", "试用"),
    FOREVER("1", "正式");

    private final String value;

    private final String label;

}
