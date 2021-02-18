package com.bot.common.enums;

import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2021/2/18
 */
@Getter
public enum ENFileType {

    //
    HELP_IMG("0", "功能大全"),
    GAME_FILE("1", "游戏文件下载");

    private String value;

    private String label;

    ENFileType(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
