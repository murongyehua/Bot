package com.bot.base.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DeepChatReq {

    private Object inputs;

    private String query;

    private String response_mode;

    private String conversation_id;

    private String user;

}
