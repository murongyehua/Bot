package com.bot.base.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TencentChatReq {

    private String ques;

    private Integer isLongChat;

    private String appKey;

    private String uid;

}
