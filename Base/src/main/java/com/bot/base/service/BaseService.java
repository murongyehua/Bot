package com.bot.base.service;

/**
 * @author liul
 * @version 1.0 2020/9/22
 */
public interface BaseService {

    /**
     * 通用查询方法
     * @param reqContent 请求内容
     * @return
     */
    String doQueryReturn(String reqContent,String token);

}
