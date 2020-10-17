package com.bot.game.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/10/17
 */
@Getter
public enum ENSkillEffect {
    //
    DOUBLE_ATTACK("A01", "双倍攻击");

    private String value;

    private String label;

    ENSkillEffect(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public static String getLabelByValue(String value) {
        ENSkillEffect[] enSkillEffects = ENSkillEffect.values();
        for (ENSkillEffect enSkillEffect : enSkillEffects) {
            if (enSkillEffect.value.equals(value)) {
                return enSkillEffect.label;
            }
        }
        return StrUtil.EMPTY;
    }

}
