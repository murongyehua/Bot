package com.bot.game.enums;

import com.bot.commom.exception.BotException;
import lombok.Getter;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Getter
public enum  ENGoodEffect {
    //
    GET_PHANTOM("0", "唤灵"),
    SKILL("1", "技能卡"),
    WAN_1("2", "避灵丹"),
    WAN_2("3", "大蕴丸"),
    WAN_3("4", "天命散"),
    WAN_4("5", "洗髓丹");

    private String value;
    private String label;

    ENGoodEffect(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public static ENGoodEffect getByValue(String value) {
        for (ENGoodEffect enGoodEffect : ENGoodEffect.values()) {
            if (enGoodEffect.value.equals(value)) {
                return enGoodEffect;
            }
        }
        throw new BotException("未知枚举值");
    }

}
