package com.bot.common.dto.qqsender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QQGroupMessage {

    private QQAuthor author;

    private String content;

    private String group_openid;

    private String id;

    private String timestamp;

}
