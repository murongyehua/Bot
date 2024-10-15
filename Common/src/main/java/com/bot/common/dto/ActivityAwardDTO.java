package com.bot.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityAwardDTO {

    private String id;

    private String activityId;

    private String awardName;

    private String type;

    private String percent;

    private String prefix;

    private Integer number;

}
