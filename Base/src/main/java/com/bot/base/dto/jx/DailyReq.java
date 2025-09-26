package com.bot.base.dto.jx;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyReq {

    private String server;

    /**
     * 0当天 1明天 2后天
     */
    private Integer num;

}
