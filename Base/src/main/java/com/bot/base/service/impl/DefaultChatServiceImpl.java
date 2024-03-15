package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.TencentChatReq;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import com.bot.common.loader.CommonTextLoader;
import com.bot.common.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 默认聊天服务
 * @author murongyehua
 * @version 1.0 2020/10/10
 */
@Service("defaultChatServiceImpl")
public class DefaultChatServiceImpl implements BaseService {

    @Value("${tencent.chat.url}")
    private String url;

    @Value("${tencent.appkey}")
    private String appKey;

    @Value(("${tencent.uid}"))
    private String uid;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token) {
        try {
            JSONObject json = JSONUtil.parseObj(HttpSenderUtil.postJsonData(url, JSONUtil.toJsonStr(new TencentChatReq(reqContent, 0, appKey, uid))));
            Integer code = (Integer) json.get("code");
            if (0 == code) {
                String content = (String) ((JSONObject)((JSONObject) json.get("data")).get("result")).get("Content");
                return new CommonResp(content, ENRespType.TEXT.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 到这里说明混元服务不可用 走免费聊天逻辑
        String finalUrl = url + reqContent;
        JSONObject json = JSONUtil.parseObj(HttpSenderUtil.get(finalUrl, null));
        Integer code = (Integer) json.get("result");
        if (0 == code) {
            String content = (String) json.get("content");
            return new CommonResp(this.dealResponse(content), ENRespType.TEXT.getType());
        }
        return new CommonResp(CommonTextLoader.defaultResponseMsg.get(RandomUtil.randomInt(0, CommonTextLoader.defaultResponseMsg.size())), ENRespType.TEXT.getType());
    }

    /**
     * 去除{}与()的内容，如果内容中包含src，当作没获取到处理
     * @param content
     * @return
     */
    private String dealResponse(String content) {
        content = content.replaceAll("\\{br}", StrUtil.CRLF);
        if (content.contains(BaseConsts.Chat.ILL_REX_1)) {
            return null;
        }
        if (content.contains(StrUtil.DELIM_START)) {
            return this.replaceSomethings(content, StrUtil.DELIM_START, StrUtil.DELIM_END);
        }
        if (content.contains(BaseConsts.Chat.ILL_REX_2)) {
            return this.replaceSomethings(content, BaseConsts.Chat.ILL_REX_2, BaseConsts.Chat.ILL_REX_3);
        }
        return content;
    }

    private String replaceSomethings(String content, String startStr, String endStr) {
        int startIndex = content.indexOf(startStr);
        int endIndex = content.indexOf(endStr);
        String replacement = content.substring(startIndex, endIndex + 1);
        return content.replace(replacement, StrUtil.EMPTY);
    }

}
