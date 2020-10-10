package com.bot.base.service.impl;

import com.bot.base.service.BaseService;
import com.bot.commom.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 土味情话服务
 * @author murongyehua
 * @version 1.0 2020/10/9
 */
@Service("sweetServiceImpl")
public class SweetServiceImpl implements BaseService {

    @Value("${sweet.url}")
    private String url;

    @Override
    public String doQueryReturn(String reqContent, String token) {
        return HttpSenderUtil.get(url, null);
    }

}
