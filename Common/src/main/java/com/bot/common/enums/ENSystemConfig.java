package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENSystemConfig {

    BASE_URL("baseUrl", "基础url"),
    WID("wid", "wid"),
    TOKEN("token", "token");

    private final String value;

    private final String label;

}
