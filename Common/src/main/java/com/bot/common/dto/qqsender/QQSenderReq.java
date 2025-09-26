package com.bot.common.dto.qqsender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QQSenderReq {

    private String content;

    private Integer msg_type;

    private QQMediaSenderReq media;

    private String event_id;

    private String msg_id;

}
