package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 道具实体
 * @author Assistant
 */
@Data
public class LifeItem {
    private Long id;
    private String name;
    private Integer type; // 道具类型：1修为类2属性类3体力类4升级法宝类5恢复类6技能书
    private Integer effectValue; // 效果值（修为类、体力类、恢复类使用）
    private String effectAttribute; // 影响的属性（属性类道具使用：speed/constitution/spirit_power/strength）
    private Long skillId; // 技能书对应的技能ID
    private Integer maxUseCount; // 最大使用次数（-1表示无限制，仅属性类道具使用）
    private Integer canUseInBattle; // 是否可战斗中使用：0否1是
    private String description;
    private Date createTime;
}
