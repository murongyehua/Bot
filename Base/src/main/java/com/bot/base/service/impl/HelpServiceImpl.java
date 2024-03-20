package com.bot.base.service.impl;

import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("helpServiceImpl")
public class HelpServiceImpl implements BaseService {

    @Value("${help.url}")
    private String url;
    @Override
    public CommonResp doQueryReturn(String reqContent, String token) {
        return new CommonResp(url, ENRespType.IMG.getType());
    }
}
