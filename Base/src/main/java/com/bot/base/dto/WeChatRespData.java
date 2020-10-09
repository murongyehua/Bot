package com.bot.base.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author murongyehua
 * @version 1.0 2020/10/9
 */
@Getter
@Setter
@ToString
public class WeChatRespData {

    private String at_someone;
    private Integer cl;
    private String msg;

}
