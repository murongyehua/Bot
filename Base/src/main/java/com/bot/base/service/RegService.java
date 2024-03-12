package com.bot.base.service;

import com.bot.common.enums.ENRegType;

public interface RegService {

    /**
     * 尝试试用
     * @param inviteCode 邀请码
     * @return 返回消息
     */
    String tryTempReg(String activeId, String inviteCode, ENRegType regType);

    /**
     * 尝试开通服务
     * @param inviteCode 邀请码
     * @return 返回消息
     */
    String tryReg(String activeId, String inviteCode, ENRegType regType);

    /**
     * 查询到期时间
     * @param token
     * @return
     */
    String queryDeadLineDate(String token);

}
