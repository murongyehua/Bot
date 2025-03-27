package com.bot.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SendAudioDTO {

    private String wId;

    private String wcId;

    private String content;

    private Integer length;

}
