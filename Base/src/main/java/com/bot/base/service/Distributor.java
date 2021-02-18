package com.bot.base.service;

import com.bot.common.enums.ENFileType;

import javax.servlet.http.HttpServletResponse;

/**
 * @author murongyehua
 * @version 1.0 2020/9/23
 */
public interface Distributor {

    /**
     * 分发请求 进行响应
     * @param response
     * @param reqContent
     * @param token
     */
    void doDistribute(HttpServletResponse response, String reqContent, String token);

    /**
     * 微信分发请求 进行响应
     * @param reqContent
     * @param token
     */
    String doDistributeWithString(String reqContent, String token);

    String doDistributeWithFilePath(ENFileType enFileType);

}
