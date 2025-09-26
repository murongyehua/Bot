package com.bot.base.dto.jx.teamcd;

import lombok.Data;

import java.util.List;

@Data
public class TeamCdDetailData {

    /**
     * 副本名称
     */
    private String mapName;

    /**
     * 副本类型
     */
    private String mapType;

    /**
     * Boss击败情况
     */
    private List<TeamCdBossInfo> bossProgress;

}
