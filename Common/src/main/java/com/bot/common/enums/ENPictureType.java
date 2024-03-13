package com.bot.common.enums;

import com.bot.common.exception.BotException;
import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2021/2/19
 */
@Getter
public enum  ENPictureType {

    //
    MAN("a1", "男头"),
    WOMAN("b1", "女头"),
    DM("c1", "动漫"),
    DM_WOMAN("c3", "动漫男头"),
    DM_WAN("c2", "动漫女头")
    ;

    private String value;

    private String label;

    ENPictureType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public static String getValueByContainLabel(String label) {
        if (isContain(label, DM_WOMAN.label)) {
            return DM_WOMAN.value;
        }
        if (isContain(label, DM_WAN.label)) {
            return DM_WAN.value;
        }
        if (isContain(label, MAN.label)) {
            return MAN.value;
        }
        if (isContain(label, WOMAN.label)) {
            return WOMAN.value;
        }
        return DM.value;
    }

    private static boolean isContain(String label, String content) {
        String[] contents = content.split("\\|\\|");
        for (String str : contents) {
            if (label.contains(str)) {
                return true;
            }
        }
        return false;
    }

}
