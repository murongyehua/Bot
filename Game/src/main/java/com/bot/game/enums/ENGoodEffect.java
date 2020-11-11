package com.bot.game.enums;

import com.bot.commom.exception.BotException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Getter
public enum  ENGoodEffect {
    //
    GET_PHANTOM("0", "唤灵符", 1000),
    SKILL("1", "技能卡", 25),
    WAN_1("2", "避灵丹", 5),
    WAN_2("3", "大蕴丸", 10),
    WAN_3("4", "天命散", 20),
    WAN_4("5", "洗髓丹", 300),
    WAN_5("6", "溢灵散", 600),
    WAN_6("7", "小补丸", 100),
    WAN_7("8", "大还丹", 190);

    private final String value;
    private final String label;
    private final Integer money;

    ENGoodEffect(String value, String label, Integer money) {
        this.value = value;
        this.label = label;
        this.money = money;
    }

    public static ENGoodEffect getByValue(String value) {
        for (ENGoodEffect enGoodEffect : ENGoodEffect.values()) {
            if (enGoodEffect.value.equals(value)) {
                return enGoodEffect;
            }
        }
        throw new BotException("未知枚举值");
    }

    public static List<ENGoodEffect> getCanSaleGoods() {
        return Arrays.asList(GET_PHANTOM,WAN_1,WAN_2,WAN_3,WAN_4,WAN_5);
    }

}
