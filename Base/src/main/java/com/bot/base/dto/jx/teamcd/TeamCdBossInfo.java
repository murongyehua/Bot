package com.bot.base.dto.jx.teamcd;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamCdBossInfo {

    /**
     * 是否击败
     */
    private Boolean finished;

    /**
     * 名称
     */
    private String name;

    /**
     * 副本内排序
     */
    private String progressId;

}
