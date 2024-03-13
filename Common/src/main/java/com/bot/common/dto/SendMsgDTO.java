package com.bot.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMsgDTO {

    private String wId;

    private String wcId;

    private String content;

    private String path;

    private String fileName;

    private String thumbPath;

}
