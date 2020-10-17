package com.bot.game.enums;

import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/10/15
 */
@Getter
public enum  ENGoodEffect {
    //
    GET_PHANTOM("0", "唤灵"),
    SKILL("1", "技能卡");

    private String value;
    private String label;

    ENGoodEffect(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
