package com.bot.base.dto.jx;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BattleReq {

    private String server;

    private String name;

    private String ticket;

    private String token;

}
