package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENGridKeyWord {

    BIND_ACCOUNT("绑定大唐工号", "/bossAccount/bindDtAccount"),
    CHECK_ACCOUNT("检查工号", "/bossAccount/checkAccount"),
    REFUND("退款", "/order/refund"),
    FETCH_BOSS_LOG("boss日志", "/log/fetchBossLog"),
    QUERY_ORDER("查订单", "/order/queryOrder"),
    QUERY_SMS("验证码", "/sms/querySms")
    ;


    private String prefix;

    private String url;

}
