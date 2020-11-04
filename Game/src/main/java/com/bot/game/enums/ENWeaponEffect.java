package com.bot.game.enums;

import com.bot.commom.exception.BotException;
import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/11/4
 */
@Getter
public enum ENWeaponEffect {
    //
    A01("A01", "", "参战幻灵的攻击提升%s%", new Integer[]{5,6,7,8,10});

    private String value;

    private String label;

    private String effectContent;

    private Integer[] levelNumber;

    ENWeaponEffect(String value, String label, String effectContent, Integer[] levelNumber) {
        this.value = value;
        this.label = label;
        this.effectContent = effectContent;
        this.levelNumber = levelNumber;
    }

    public static ENWeaponEffect getByValue(String value) {
        for (ENWeaponEffect enWeaponEffect : ENWeaponEffect.values()) {
            if (enWeaponEffect.value.equals(value)) {
                return enWeaponEffect;
            }
        }
        throw new BotException("未知枚举值");
    }

}
