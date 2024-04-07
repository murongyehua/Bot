package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.GptChatReq;
import com.bot.base.dto.TencentChatReq;
import com.bot.base.service.BaseService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENChatEngine;
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
    private String tencentUrl;

    @Value("${chatgpt.url}")
    private String gptUrl;

    @Value("${chatgpt.key}")
    private String gptApiKey;

    @Value("${tencent.appkey}")
    private String tencentAppKey;

    @Value(("${tencent.uid}"))
    private String uid;

    @Value("${chat.url}")
    private String defaultUrl;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token) {
        ENChatEngine engine = SystemConfigCache.userChatEngine.get(token);
        if (engine == null) {
            engine = ENChatEngine.DEFAULT;
        }
        String msg = null;
        switch (engine) {
            case TENCENT:
                msg = tencentChat(reqContent);
                break;
            case CHATGPT:
                msg = gptChat(reqContent);
                break;
            case DEFAULT:
                msg = defaultChat(reqContent);
                break;
        }
        if (msg == null) {
            msg = CommonTextLoader.defaultResponseMsg.get(RandomUtil.randomInt(0, CommonTextLoader.defaultResponseMsg.size()));
        }
        return new CommonResp(msg, ENRespType.TEXT.getType());
    }

    private String tencentChat(String reqContent) {
        try {
            JSONObject json = JSONUtil.parseObj(HttpSenderUtil.postJsonData(tencentUrl, JSONUtil.toJsonStr(new TencentChatReq(reqContent, 0, tencentAppKey, uid))));
            Integer code = (Integer) json.get("code");
            if (0 == code) {
                return (String) ((JSONObject)((JSONObject) json.get("data")).get("result")).get("Content");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String gptChat(String reqContent) {
        try {
            return HttpSenderUtil.postJsonData(gptUrl, JSONUtil.toJsonStr(new GptChatReq(reqContent, gptApiKey)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String defaultChat(String reqContent) {
        String finalUrl = defaultUrl + reqContent;
        JSONObject json = JSONUtil.parseObj(HttpSenderUtil.get(finalUrl, null));
        Integer code = (Integer) json.get("result");
        if (0 == code) {
            String content = (String) json.get("content");
            return this.dealResponse(content);
        }
        return null;
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
