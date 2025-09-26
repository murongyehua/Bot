package com.bot.base.dto.jx.attribute;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeBaseInfo {

    /**
     * 大区
     */
    private String zoneName;

    /**
     * 区服
     */
    private String serverName;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 门派名称
     */
    private String forceName;

    /**
     * 体型名称
     */
    private String bodyName;

    /**
     * 帮会名称
     */
    private String tongName;

    /**
     * 阵营名称
     */
    private String campName;

    /**
     * 推栏头像
     */
    private String personAvatar;

    /**
     * 心法名称
     */
    private String kungfuName;

    /**
     * 心法类型
     */
    private String kungfuType;

    /**
     * 装备
     */
    private List<EquipInfo> equipList;

    /**
     * 奇穴
     */
    private List<QIXUEInfo> qixueList;

    /**
     * 详细属性
     */
    private PanelInfo panelList;

    /**
     * 时间
     */
    private Long time;

}
