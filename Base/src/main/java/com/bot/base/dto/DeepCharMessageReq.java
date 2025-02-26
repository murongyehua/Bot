package com.bot.base.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeepCharMessageReq {

    private String content;

    private String role;

}
