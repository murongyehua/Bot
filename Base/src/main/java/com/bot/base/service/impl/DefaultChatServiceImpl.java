package com.bot.base.service.impl;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.CreatePicReq;
import com.bot.base.dto.DeepChatReq;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${base.chat.key}")
    private String baseChatKey;

    @Value("${ds.chat.key}")
    private String dsChatKey;

    /**
     * 基础聊天id记录
     */
    public final static Map<String, String> TOKEN_2_BASE_CHAT_ID_MAP = new HashMap<>();

    /**
     * 深思聊天id记录
     */
    public final static Map<String, String> TOKEN_2_DS_CHAT_ID_MAP = new HashMap<>();

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        if (reqContent.equals("删除会话")) {
            TOKEN_2_BASE_CHAT_ID_MAP.remove(groupId == null ? token : groupId);
            TOKEN_2_DS_CHAT_ID_MAP.remove(groupId == null ? token : groupId);
            return new CommonResp("已清除所有版本会话，可以重新开始聊天了。", ENRespType.TEXT.getType());
        }
        if (reqContent.startsWith("深思")) {
            reqContent = reqContent.replaceAll("深思", "").trim();
            return new CommonResp(this.deepChat(reqContent, "ds", groupId == null ? token : groupId), ENRespType.TEXT.getType());
        }
        if (reqContent.startsWith("生图")) {
            reqContent = reqContent.replaceAll("生图", "").trim();
            String url = this.createPic("Kwai-Kolors/Kolors", reqContent);
            if (url == null) {
                return new CommonResp("生成失败，请联系管理员检查。", ENRespType.TEXT.getType());
            }
            return new CommonResp(url, ENRespType.IMG.getType());
        }
        return new CommonResp(this.deepChat(reqContent, "base", groupId == null ? token : groupId), ENRespType.TEXT.getType());
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

    private String deepChat(String reqContent, String model, String token) {
        log.info("调用ai的消息为[{}]", reqContent);
        try {
            String conversationId = "";
            String key = "";
            if ("base".equals(model)) {
                conversationId = TOKEN_2_BASE_CHAT_ID_MAP.get(token) == null ? "" : TOKEN_2_BASE_CHAT_ID_MAP.get(token);
                key = baseChatKey;
            }else {
                conversationId = TOKEN_2_DS_CHAT_ID_MAP.get(token) == null ? "" : TOKEN_2_DS_CHAT_ID_MAP.get(token);
                key = dsChatKey;
            }
            JSONObject json = JSONUtil.parseObj(HttpSenderUtil.postJsonDataWithToken(defaultUrl,
                    JSONUtil.toJsonStr(new DeepChatReq(new JSONObject(), reqContent, "blocking", conversationId, token)),
                    key));
            String conversation_id = json.getStr("conversation_id");
            if (conversation_id == null) {
                log.error("调用失败，返回内容：[{}]", JSONUtil.toJsonStr(json));
                return "分析失败，请联系管理员检查。";
            }
            if ("base".equals(model)) {
                TOKEN_2_BASE_CHAT_ID_MAP.put(token, conversation_id);
            }else {
                TOKEN_2_DS_CHAT_ID_MAP.put(token, conversation_id);
            }
            String answer = json.getStr("answer");
            return UnicodeUtil.toString(answer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "分析失败，请联系管理员检查。";
    }

}
