package com.bot.base.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author liul
 * @version 1.0 2020/10/9
 */
@Getter
@Setter
@ToString
public class WeChatResp {

    private String to_user;

    private WeChatRespData[] data;

}
