package com.bot.base.dto.jx.attribute;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PanelInfo {

    /**
     * 装备分数
     */
    private Integer score;

    /**
     * 详细属性
     */
    private List<AttributeDetailInfo> panel;

}
