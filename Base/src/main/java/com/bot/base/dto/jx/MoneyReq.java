package com.bot.base.dto.jx;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoneyReq {

    private String server;

    private Integer limit;

    private String token;

}
