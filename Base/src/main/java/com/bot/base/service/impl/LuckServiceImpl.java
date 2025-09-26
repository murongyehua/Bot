package com.bot.base.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.LuckDTO;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 运势占卜服务
 * @author murongyehua
 * @version 1.0 2020/9/27
 */
@Service("luckServiceImpl")
public class LuckServiceImpl implements BaseService {

    public static Map<String, LuckDTO> luckCacheMap = new HashMap<>();

    @Value("${luck.url}")
    private String url;

    @Override
    @Deprecated
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (ObjectUtil.equals("求签", reqContent)) {
            String key = token + StrUtil.UNDERLINE + DateUtil.today();
            LuckDTO resp = luckCacheMap.computeIfAbsent(key, k -> this.getLuck());
            return new CommonResp(resp.getPic(), ENRespType.IMG.getType());
        }else if (ObjectUtil.equals("解签", reqContent)) {
            String key = token + StrUtil.UNDERLINE + DateUtil.today();
            LuckDTO resp = luckCacheMap.get(key);
            if (resp == null) {
                return new CommonResp("你今天还未求签，要先求签才能解签哦~", ENRespType.TEXT.getType());
            }
            String contentFormat = "签名：%s\r\n内容：%s\r\n解签：%s\r\n(此为附带的解签结果，仅供参考，也可以自行解签或使用小林的ai能力解签哦OvO)";
            String content = String.format(contentFormat, resp.getTitle(), resp.getPoem(), resp.getContent());
            return new CommonResp(content, ENRespType.TEXT.getType());
        }
        return null;
    }

    private LuckDTO getLuck() {
        String response = HttpUtil.get(url);
        JSONObject jsonObject = JSONUtil.parseObj(response);
        JSONObject data = (JSONObject) jsonObject.get("data");
        return JSONUtil.toBean(data, LuckDTO.class);
    }

}
