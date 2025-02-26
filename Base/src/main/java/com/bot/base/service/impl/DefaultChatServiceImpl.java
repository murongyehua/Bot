package com.bot.base.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.CreatePicReq;
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
import java.util.HashMap;
import java.util.Map;

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

    @Value("${pic.create.url}")
    private String picUrl;

    @Value("${chat.key}")
    private String token;

    @Value("${manager.token}")
    private String managerToken;

    public final static Map<String, Integer> SLOW_CREATE_TOKEN_TIMES_MAP = new HashMap<>();

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        if (reqContent.startsWith("深思")) {
            reqContent = reqContent.replaceAll("深思", "").trim();
            return new CommonResp(this.deepChat(reqContent, "Pro/deepseek-ai/DeepSeek-R1"), ENRespType.TEXT.getType());
        }
        if (reqContent.startsWith("生图")) {
            reqContent = reqContent.replaceAll("生图", "").trim();
            String url = this.createPic("black-forest-labs/FLUX.1-schnell", reqContent);
            if (url == null) {
                return new CommonResp("生成失败，请联系管理员检查。", ENRespType.TEXT.getType());
            }
            return new CommonResp(url, ENRespType.IMG.getType());
        }
        if (reqContent.startsWith("慢生图")) {
            // 校验次数
            Integer times = SLOW_CREATE_TOKEN_TIMES_MAP.get(token);
            if (ObjectUtil.notEqual(token, managerToken)) {
                if (times != null && times >= 2) {
                    return new CommonResp("当前限制每人每天可使用2次慢生图，今日已达上限。\r\n（每日上午10:00刷新）", ENRespType.TEXT.getType());
                }
            }
            reqContent = reqContent.replaceAll("慢生图", "").trim();
            String url = this.createPic("black-forest-labs/FLUX.1-dev", reqContent);
            if (url == null) {
                return new CommonResp("生成失败，请联系管理员检查。", ENRespType.TEXT.getType());
            }
            // 记录次数
            if (times == null) {
                SLOW_CREATE_TOKEN_TIMES_MAP.put(token, 1);
            }else {
                SLOW_CREATE_TOKEN_TIMES_MAP.put(token, times + 1);
            }
            return new CommonResp(url, ENRespType.IMG.getType());
        }
        return new CommonResp(this.deepChat(reqContent, "Qwen/Qwen2.5-7B-Instruct"), ENRespType.TEXT.getType());
    }

    private String createPic(String model, String reqContent) {
        try {
            JSONObject json = JSONUtil.parseObj(HttpSenderUtil.postJsonDataWithToken(picUrl,
                    JSONUtil.toJsonStr(new CreatePicReq(model, reqContent, "768x1024", 14)),
                    token));
            JSONArray images = json.getJSONArray("images");
            if (images == null) {
                log.error("调用失败，返回内容：[{}]", JSONUtil.toJsonStr(json));
                return "生成失败，请联系管理员检查。";
            }
            JSONObject image = (JSONObject) images.get(0);
            return (String) image.get("url");
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
