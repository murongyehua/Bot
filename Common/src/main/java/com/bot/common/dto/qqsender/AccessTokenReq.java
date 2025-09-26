package com.bot.common.dto.qqsender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessTokenReq {

    private String appId;

    private String clientSecret;

}
