package com.bot.base.dto.jx;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RandomShowReq {

    private String body;

    private String force;

    private String token;

}
