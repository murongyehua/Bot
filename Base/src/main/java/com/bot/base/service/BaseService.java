package com.bot.base.service;

import com.bot.base.dto.CommonResp;

/**
 * @author murongyehua
 * @version 1.0 2020/9/22
 */
public interface BaseService {

    /**
     * 通用查询方法
     * @param reqContent 请求内容
     * @return
     */
    CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel);

}
