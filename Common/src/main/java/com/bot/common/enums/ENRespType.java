package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENRespType {

    TEXT("0", "文本"),
    IMG("1", "图片"),
    VIDEO("2", "视频"),
    FILE("3", "文件");

    private final String type;

    private final String label;

}
