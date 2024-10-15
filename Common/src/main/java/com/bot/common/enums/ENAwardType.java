package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENAwardType {

    NORNAL("1", "普通"),
    ONLY("2", "唯一"),
    DI_BAO("3", "低保");

    private String value;

    private String label;

}
