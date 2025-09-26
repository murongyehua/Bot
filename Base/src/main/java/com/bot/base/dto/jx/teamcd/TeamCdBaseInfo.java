package com.bot.base.dto.jx.teamcd;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeamCdBaseInfo {

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
     * 数据
     */
    private List<TeamCdDetailData> data;

    /**
     * 时间
     */
    private Long time;

}
