package com.bot.common.dto.qqsender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QQMediaInfo {

    private Integer file_type;

    private String url;

    private Boolean srv_send_msg = false;

}
