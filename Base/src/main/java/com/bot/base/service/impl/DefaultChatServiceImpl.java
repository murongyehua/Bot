package com.bot.base.service.impl;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.*;
import com.bot.base.service.BaseService;
import com.bot.base.util.SiliconflowUtil;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    @Value("${audio.chat.key}")
    private String audioChatKey;

    @Value("${group.chat.key}")
    private String groupChatKey;

    /**
     * 聊天id记录
     */
    public final static Map<String, ChatIdDTO> TOKEN_2_BASE_CHAT_ID_MAP = new HashMap<>();

    /**
     * 语音聊天id和步数记录
     */
    public final static Map<String, SpeechIdDTO> TOKEN_2_SPEECH_ID_MAP = new HashMap<>();

    /**
     * 群组id记录
     */
    public final static Map<String, String> TOKEN_2_GROUP_ID_MAP = new HashMap<>();

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (StrUtil.isEmpty(reqContent.trim())) {
            return new CommonResp("你好，我在。", ENRespType.TEXT.getType());
        }
        if (reqContent.equals("开启语音")) {
            TOKEN_2_SPEECH_ID_MAP.put(groupId == null ? token : groupId, new SpeechIdDTO(null, 0));
            return new CommonResp("开启成功，接下来的5条消息我将发送语音回复，也可以提前关闭", ENRespType.TEXT.getType());
        }
        if (reqContent.equals("关闭语音")) {
            TOKEN_2_SPEECH_ID_MAP.remove(groupId == null ? token : groupId);
            return new CommonResp("关闭成功", ENRespType.TEXT.getType());
        }
        if (reqContent.startsWith("读一下")) {
            return new CommonResp(SiliconflowUtil.speech(reqContent.replaceAll("读一下", "").trim()), ENRespType.AUDIO.getType());
        }
        if (reqContent.equals("删除会话")) {
            TOKEN_2_BASE_CHAT_ID_MAP.remove(groupId == null ? token : groupId);
            TOKEN_2_SPEECH_ID_MAP.remove(groupId == null ? token : groupId);
            TOKEN_2_GROUP_ID_MAP.remove(groupId == null ? token : groupId);
            return new CommonResp("已清除所有版本会话，可以重新开始聊天了。", ENRespType.TEXT.getType());
        }
        if (reqContent.startsWith("生图")) {
            reqContent = reqContent.replaceAll("生图", "").trim();
            String url = this.createPic("Kwai-Kolors/Kolors", reqContent);
            if (url == null) {
                return new CommonResp("生成失败，请联系管理员检查。", ENRespType.TEXT.getType());
            }
            return new CommonResp(url, ENRespType.IMG.getType());
        }
        // 如果是语音聊天，要进语音会话
        if (TOKEN_2_SPEECH_ID_MAP.get(groupId == null ? token : groupId) != null) {
            return new CommonResp(this.getAudioChatFileName(reqContent,  groupId == null ? token : groupId), ENRespType.AUDIO.getType());
        }
        // 现在群聊闲聊在外面处理，这里返回null
        if (groupId != null) {
            return null;
        }
        // 私聊正常返回
        return new CommonResp(this.deepChat(reqContent, "base", token), ENRespType.TEXT.getType());
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

    private String getAudioChatFileName(String reqContent, String token) {
        // 生成文本
        String inputContent = this.deepChat(reqContent, "audio", token);
        return SiliconflowUtil.speech(inputContent);
    }

    public String deepChat(String reqContent, String model, String token) {
        log.info("调用ai的消息为[{}]", reqContent);
        try {
            String conversationId = "";
            String key = "";
            String responseMode = "streaming";
            if ("base".equals(model)) {
                conversationId = TOKEN_2_BASE_CHAT_ID_MAP.get(token) == null ? "" : TOKEN_2_BASE_CHAT_ID_MAP.get(token).getId();
                key = groupChatKey;
                responseMode = "blocking";
            }else if ("audio".equals(model)) {
                conversationId = TOKEN_2_SPEECH_ID_MAP.get(token) == null ? "" : TOKEN_2_SPEECH_ID_MAP.get(token).getId();
                key = audioChatKey;
                responseMode = "blocking";
            }else if ("group".equals(model)) {
                conversationId = TOKEN_2_GROUP_ID_MAP.get(token) == null ? "" : TOKEN_2_GROUP_ID_MAP.get(token);
                key = groupChatKey;
                responseMode = "blocking";
            }
            String response = HttpSenderUtil.postJsonDataWithToken(defaultUrl,
                    JSONUtil.toJsonStr(new DeepChatReq(new JSONObject(), reqContent, responseMode, conversationId, token)),
                    key);
            String conversation_id = null;
            StringBuilder answer = new StringBuilder();
            if (response.startsWith("{")) {
                // 阻塞模式
                JSONObject json = JSONUtil.parseObj(response);
                conversation_id = json.getStr("conversation_id");
                answer = new StringBuilder(json.getStr("answer"));
            }else {
                // 流式模式
                String[] datas = response.split("data: ");
                for (String data : datas) {
                    if (StrUtil.isNotEmpty(data)) {
                        JSONObject dataObject = JSONUtil.parseObj(data);
                        String event = dataObject.getStr("event");
                        switch (event) {
                            case "agent_message":
                                String answerPart = dataObject.getStr("answer");
                                answer.append(answerPart);
                                break;
                            case "message_end":
                                conversation_id = dataObject.getStr("conversation_id");
                                break;
                        }
                    }
                }
            }
            if (conversation_id == null) {
                log.error("调用失败，返回内容：[{}]", JSONUtil.toJsonStr(response));
                return "分析失败，请联系管理员检查。";
            }
            if ("base".equals(model)) {
                TOKEN_2_BASE_CHAT_ID_MAP.put(token, new ChatIdDTO(conversation_id, new Date()));
            }else if ("audio".equals(model)) {
                SpeechIdDTO speechIdDTO = TOKEN_2_SPEECH_ID_MAP.get(token);
                if (speechIdDTO.getId() == null) {
                    speechIdDTO.setId(conversationId);
                }
                if (speechIdDTO.getStep() == 4) {
                    TOKEN_2_SPEECH_ID_MAP.remove(token);
                }else {
                    speechIdDTO.setStep(speechIdDTO.getStep() + 1);
                    TOKEN_2_SPEECH_ID_MAP.put(token, speechIdDTO);
                }
            }else if ("group".equals(model)) {
                TOKEN_2_GROUP_ID_MAP.put(token, conversation_id);
            }
            return UnicodeUtil.toString(answer.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return "分析失败，请联系管理员检查。";
    }

}
