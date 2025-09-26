package com.bot.base.dto.jx.battle;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BattleBaseInfo {

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
     * 表现
     */
    private String performance;

    /**
     * 历史
     */
    private String history;

    /**
     * 时间
     */
    private Long time;

}
