package com.bot.base.dto.jx.attribute;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EquipInfo {

    /**
     * 装备名字
     */
    private String name;

    /**
     * 装备图标
     */
    private String icon;

    /**
     * 装备类型
     */
    private String kind;

    /**
     * 装备品质
     */
    private String quality;

    /**
     * 精炼等级
     */
    private String strengthLevel;

    /**
     * 最高精炼等级
     */
    private String maxStrengthLevel;

    /**
     * 装备来源
     */
    private String source;

    /**
     * 五行石镶嵌
     */
    private List<FiveStoneInfo> fiveStone;

    /**
     * 五彩石
     */
    private ColorStoneInfo colorStone;

    /**
     * 装备类型
     */
    private EquipType EquipType;

    /**
     * 属性
     */
    private List<ModifyType> modifyType;

    /**
     * 附魔
     */
    private List<PermanentEnchantInfo> permanentEnchant;
}
