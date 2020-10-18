package com.bot.commom.enums;

import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/10/14
 */
@Getter
public enum ENYesOrNo {

    //
    YES("1", "是"),
    NO("0", "否"),;


    private String value;
    private String label;

    ENYesOrNo(String value, String label) {
        this.value = value;
        this.label = label;
    }
}