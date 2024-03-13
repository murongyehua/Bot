package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.service.BaseService;
import com.bot.common.loader.CommonTextLoader;
import com.bot.common.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("girlVideoServiceImpl")
public class GirlVideoServiceImpl implements BaseService {

    @Value("${girl.url}")
    private String girlUrl;

    @Value("${girl.key}")
    private String girlKey;

    @Override
    public String doQueryReturn(String reqContent, String token) {
        JSONObject json = JSONUtil.parseObj(HttpSenderUtil.get(girlUrl + girlKey, null));
        Integer code = (Integer) json.get("code");
        if (code != 200) {
            return CommonTextLoader.defaultResponseMsg.get(RandomUtil.randomInt(0, CommonTextLoader.defaultResponseMsg.size()));
        }
        String url = (String) json.get("video");
        if (StrUtil.isEmpty(url)) {
            return CommonTextLoader.defaultResponseMsg.get(RandomUtil.randomInt(0, CommonTextLoader.defaultResponseMsg.size()));
        }
        return url;
    }

}
