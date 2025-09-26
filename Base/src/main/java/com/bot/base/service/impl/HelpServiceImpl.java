package com.bot.base.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("helpServiceImpl")
public class HelpServiceImpl implements BaseService {

    @Value("${help.url}")
    private String url;
    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (ObjectUtil.equals(reqContent, "菜单") || ObjectUtil.equals(reqContent, "帮助")) {
            return new CommonResp(url, ENRespType.IMG.getType());
        }
        return null;
    }
}
