package com.bot.base.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DeepChatReq {

    private String model;

    private Double frequency_penalty;

    private Integer max_tokens;

    private List<DeepCharMessageReq> messages;

}
