package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENSystemWord {

    CHRISTMAS(21L,"金勾拜", "圣诞节签到可得", 4, "2"),
    INVITE_ONE(22L, "小林推广大使", "邀请一位好友签到可得", 2, "1"),
    INVITE_THREE(23L, "摆渡人", "邀请三位好友签到可得", 4, "2"),
    INVITE_FIVE(24L, "松烟荐友人", "邀请五位好友签到可得", 9,"3"),
    INVITE_TEN(25L, "星夜引路人", "邀请十位好友签到可得", 16, "4");

    private Long id;

    private String word;

    private String desc;

    private Integer merit;

    private String rariy;

}
