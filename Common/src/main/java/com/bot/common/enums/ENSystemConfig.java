package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENSystemConfig {

    BASE_URL("baseUrl", "基础url"),
    WID("wid", "wid"),
    TOKEN("token", "token"),
    INVITE_CODE("inviteCode", "邀请码");

    private final String value;

    private final String label;

}
