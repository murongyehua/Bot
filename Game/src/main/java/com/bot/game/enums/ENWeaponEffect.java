package com.bot.game.enums;

import com.bot.commom.exception.BotException;
import lombok.Getter;

/**
 * @author murongyehua
 * @version 1.0 2020/11/4
 */
@Getter
public enum ENWeaponEffect {
    //
    W01("W01", "翻天印", "参战幻灵的攻击提升百分之%s", new Integer[]{5,6,7,8,10}),
    W02("W02", "护灵甲", "参战幻灵的防御提升百分之%s", new Integer[]{5,6,7,8,10}),
    W03("W03", "玄王佩", "参战幻灵的血量提升百分之%s", new Integer[]{5,6,7,8,10}),
    W04("W04", "醒世符", "每次战斗参战幻灵可免疫伤害%s回合", new Integer[]{1,1,1,1,2}),
    W05("W05", "解灵囊", "每次战斗参战幻灵可免疫负面效果%s回合", new Integer[]{1,1,1,1,2}),
    W06("W06", "禁灵镜", "每次战斗禁止目标使用技能，持续%s回合", new Integer[]{1,1,2,2,3}),
    W07("W07", "极影斗篷", "参战幻灵的速度提升百分之%s", new Integer[]{5,7,9,11,13});

    private final String value;

    private final String label;

    private final String effectContent;

    private final Integer[] levelNumber;

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
