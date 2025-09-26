package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENJXCacheType {

    SHOW("show", "QQ秀"),
    MONEY("money", "金价"),
    NEWS("news", "资讯"),
    NOTICE("notice", "公告"),
    BATTLE("battle", "战绩"),
    TEAM_CD("teamCd", "副本"),
    ATTRIBUTE("attribute", "属性"),
    OPEN_SERVER("open", "开服");

    private String value;

    private String label;

}
