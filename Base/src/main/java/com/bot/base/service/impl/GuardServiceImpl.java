package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import com.bot.common.loader.CommonTextLoader;
import com.bot.common.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("guardServiceImpl")
public class GuardServiceImpl implements BaseService {

    @Value("${guard.url}")
    private String url;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token) {
        JSONObject json = JSONUtil.parseObj(HttpSenderUtil.get(url, null));
        int code = (Integer)json.get("code");
        if (code == 200) {
            return new CommonResp((String) json.get("body"), ENRespType.TEXT.getType());
        }
        return new CommonResp(CommonTextLoader.defaultResponseMsg.get(RandomUtil.randomInt(0, CommonTextLoader.defaultResponseMsg.size())), ENRespType.TEXT.getType());
    }


}
