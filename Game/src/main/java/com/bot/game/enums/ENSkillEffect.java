package com.bot.game.enums;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.exception.BotException;
import lombok.Getter;

/**
 * @author liul
 * @version 1.0 2020/10/17
 */
@Getter
public enum ENSkillEffect {
    //A 纯攻击类、B 苦肉类、C deBuff类、D 驱散类
    A01("A01", "双倍", null, null),
    A02("A02", "1.5倍", null, null),
    A03("A03", "3倍，但defense下调30%",null,null),

    B01("B01", "扣除当前hp5%，并将其转化为等额伤害给对方，此伤害无视防御", null, null),
    B02("B02", "扣除当前hp10%，并将其转化为等额伤害给对方，此伤害无视防御", null, null),

    C01("C01", "降低目标10%攻击", "deBuff", ENEffectType.PRE),
    C02("C02", "降低目标20%攻击","deBuff", ENEffectType.PRE),
    C03("C03", "降低目标10%防御","deBuff", ENEffectType.PRE),
    C04("C04", "降低目标20%防御","deBuff", ENEffectType.PRE),
    C05("C05", "停止行动1回合", "deBuff", ENEffectType.PRE),
    C06("C06", "每回合自动扣除2%最大生命值", "deBuff", ENEffectType.PRE),
    C07("C07", "每回合自动扣除当前5%生命值", "deBuff", ENEffectType.PRE),
    C08("C08", "反弹所受伤害的30%，真实伤害无法反弹","buff", ENEffectType.DEFENSE),
    C09("C09", "每回合结束时，回复已损失生命的10%","buff", ENEffectType.END),
    C10("C10", "降低目标速度5%", "deBuff", ENEffectType.PRE),

    D01("D01", "盗取对方一个buff", null, null),
    D02("D02", "取消一个deBuff", null, null);


    private String value;

    private String label;

    private ENEffectType enEffectType;

    private String buffStatus;

    ENSkillEffect(String value, String label, String buffStatus, ENEffectType enEffectType) {
        this.value = value;
        this.label = label;
        this.buffStatus = buffStatus;
        this.enEffectType = enEffectType;
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

    public static ENSkillEffect getByValue(String value) {
        for (ENSkillEffect enSkillEffect : ENSkillEffect.values()) {
            if (enSkillEffect.value.equals(value)) {
                return enSkillEffect;
            }
        }
        throw new BotException("未知枚举值");
    }

}
