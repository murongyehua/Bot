package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 装备基础实体
 * @author Assistant
 */
@Data
public class LifeEquipment {
    private Long id;
    private String name;
    private Integer type; // 装备类型：1功法2心法3神通4法宝
    private Integer attribute; // 装备属性：0无属性1金2木3水4火5土
    private Integer rarity; // 稀有度：1普通2精良3稀有4史诗5传说
    private String description;
    private Date createTime;
}
