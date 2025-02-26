package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import com.bot.common.loader.CommonTextLoader;
import com.bot.common.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("girlVideoServiceImpl")
public class GirlVideoServiceImpl implements BaseService {

    @Value("${girl.url}")
    private String girlUrl;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        JSONObject json = JSONUtil.parseObj(HttpSenderUtil.get(girlUrl, null));
        Integer code = (Integer) json.get("code");
        if (code != 200) {
            return new CommonResp(CommonTextLoader.defaultResponseMsg.get(RandomUtil.randomInt(0, CommonTextLoader.defaultResponseMsg.size())), ENRespType.TEXT.getType());
        }
        String url = (String) json.get("data");
        if (StrUtil.isEmpty(url)) {
            return new CommonResp(CommonTextLoader.defaultResponseMsg.get(RandomUtil.randomInt(0, CommonTextLoader.defaultResponseMsg.size())), ENRespType.TEXT.getType());
        }
        return new CommonResp(url, ENRespType.VIDEO.getType());
    }

}
