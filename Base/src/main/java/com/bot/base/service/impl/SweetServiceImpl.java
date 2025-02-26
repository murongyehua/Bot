package com.bot.base.service.impl;

import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
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
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        return new CommonResp(HttpSenderUtil.get(url, null), ENRespType.TEXT.getType());
    }

}
