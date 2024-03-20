package com.bot.base.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GptChatReq {

    private String text;

    private String apiKey;

}
