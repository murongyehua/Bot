package com.bot.base.dto.jx.attribute;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttributeDetailInfo {

    /**
     * 属性名称
     */
    private String name;

    /**
     * 是否百分比
     */
    private Boolean percent;

    /**
     * 数值
     */
    private Double value;

}
