package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.DeepCharMessageReq;
import com.bot.base.dto.DeepChatReq;
import com.bot.base.service.BaseService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENChatEngine;
import com.bot.common.enums.ENRespType;
import com.bot.common.loader.CommonTextLoader;
import com.bot.common.util.HttpSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 默认聊天服务
 * @author murongyehua
 * @version 1.0 2020/10/10
 */
@Slf4j
@Service("defaultChatServiceImpl")
public class DefaultChatServiceImpl implements BaseService {

    @Value("${chat.url}")
    private String defaultUrl;

    @Value("${chat.key}")
    private String token;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        log.info("进入聊天服务的消息为[{}]", reqContent);
        if (reqContent.startsWith("深思")) {
            reqContent = reqContent.replaceAll("深思", "").trim();
            return new CommonResp(this.deepChat(reqContent, "Pro/deepseek-ai/DeepSeek-R1"), ENRespType.TEXT.getType());
        }
        return new CommonResp(this.deepChat(reqContent, "Qwen/Qwen2.5-7B-Instruct"), ENRespType.TEXT.getType());
    }

    private String deepChat(String reqContent, String model) {
        log.info("调用ai的消息为[{}]", reqContent);
        try {
            JSONObject json = JSONUtil.parseObj(HttpSenderUtil.postJsonDataWithToken(defaultUrl,
                    JSONUtil.toJsonStr(new DeepChatReq(model, 0.0, 4096, new ArrayList<DeepCharMessageReq>(){{add(new DeepCharMessageReq(reqContent, "user"));}})),
                    token));
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null) {
                log.error("调用失败，返回内容：[{}]", JSONUtil.toJsonStr(json));
                return "分析失败，请联系管理员检查。";
            }
            JSONObject choice = (JSONObject) choices.get(0);
            JSONObject message = choice.getJSONObject("message");
            return message.getStr("content");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "分析失败，请联系管理员检查。";
    }

}
