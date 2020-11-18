package com.bot.game.enums;

import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/11/17
 */
@Getter
public enum ENWriteMessageStatus {

    //
    WAIT_CONTENT("0", "等待输入内容"),
    WAIT_ASK_NEED_ATTACH("1", "询问是否需要附件"),
    WAIT_ATTACH("2", "等待选择附件");

    private String value;

    private String label;

    ENWriteMessageStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
