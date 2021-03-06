package com.bot.game.enums;

import cn.hutool.core.util.StrUtil;
import com.bot.common.exception.BotException;
import lombok.Getter;

/**
 * @author murongyehua
 * @version 1.0 2020/10/17
 */
@Getter
public enum ENSkillEffect {
    //A 纯攻击类、B 苦肉类、C deBuff类、D 驱散类
    A01("A01", "给目标造成双倍伤害", null, null),
    A02("A02", "给目标造成1.5倍伤害", null, null),
    A03("A03", "给目标造成3倍伤害，但本回合内防御降低30%",null,null),
    A04("A04", "50%的几率给目标造成3倍伤害",null,null),

    B01("B01", "扣除自己当前血量的5%，并将其转化为等额伤害给目标，此伤害无视防御", null, null),
    B02("B02", "扣除自己当前血量的10%，并将其转化为等额伤害给对方，此伤害无视防御", null, null),

    C01("C01", "降低目标10%攻击", "deBuff", ENEffectType.PRE),
    C02("C02", "降低目标20%攻击","deBuff", ENEffectType.PRE),
    C03("C03", "降低目标10%防御","deBuff", ENEffectType.PRE),
    C04("C04", "降低目标20%防御","deBuff", ENEffectType.PRE),
    C05("C05", "使目标停止行动1回合", "deBuff", ENEffectType.PRE),
    C06("C06", "使目标每回合自动扣除2%最大生命值", "deBuff", ENEffectType.PRE),
    C07("C07", "使目标每回合自动扣除当前5%生命值", "deBuff", ENEffectType.PRE),
    C08("C08", "反弹所受伤害的30%，真实伤害无法反弹","buff", ENEffectType.DEFENSE),
    C09("C09", "每回合结束时，回复自己已损失生命的10%","buff", ENEffectType.END),
    C10("C10", "本次战斗中，永久降低目标速度5%", "deBuff", ENEffectType.PRE),

    D01("D01", "盗取对方一个buff", null, null),
    D02("D02", "取消一个deBuff", null, null),

    U01("U01", "提升自己%10的攻击力", "buff", ENEffectType.PRE),
    U02("U02", "提升自己%100的速度", "buff", ENEffectType.PRE),
    U03("U03", "提升自己%30的防御", "buff", ENEffectType.PRE),

    W01("W01","翻天印","buff", ENEffectType.END),
    W02("W02","护灵甲","buff", ENEffectType.END),
    W03("W03","玄王佩","buff", ENEffectType.END),
    W04("W04","醒世符","buff", ENEffectType.DEFENSE),
    W05("W05","解灵囊","buff", ENEffectType.DEFENSE),
    W06("W06","禁灵镜","deBuff", ENEffectType.PRE),
    W07("W07","极影斗篷","buff", ENEffectType.END);


    private final String value;

    private final String label;

    private final ENEffectType enEffectType;

    private final String buffStatus;

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
