package com.bot.base.dto.jx.battle;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BattleDetailData {

    /**
     * 积分
     */
    private Integer mmr;

    /**
     * 段位
     */
    private Integer grade;

    /**
     * 排名
     */
    private String ranking;

    /**
     * 胜场
     */
    private Integer winCount;

    /**
     * 总场
     */
    private Integer totalCount;

    /**
     * mvp场次
     */
    private Integer mvpCount;

    /**
     * 胜率
     */
    private Integer winRate;

}
