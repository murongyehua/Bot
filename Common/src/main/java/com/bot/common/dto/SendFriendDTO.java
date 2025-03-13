package com.bot.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendFriendDTO {

    private String wId;

    private String content;

    private String paths;
}
