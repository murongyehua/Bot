package com.bot.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.common.dto.qqsender.AccessTokenReq;
import com.bot.common.dto.qqsender.QQMediaInfo;
import com.bot.common.dto.qqsender.QQMediaSenderReq;
import com.bot.common.dto.qqsender.QQSenderReq;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QQSender {

    public static final String GET_ACCESS_TOKEN = "https://bots.qq.com/app/getAppAccessToken";

    public static final String BASE_URL = "https://api.sgroup.qq.com";

    public static final String SEND_GROUP_MESSAGE = "/v2/groups/%s/messages";

    public static final String SEND_GROUP_MEDIA = "/v2/groups/%s/files";

    public static Map<String, Date> accessTokenExpiresMap = new HashMap<>();

    public static final String appId = "102791624";

    public static final String clientSecret = "93xrlfZUPKFA50vrnjfbXTQNKHEB8531";

    public static String initToken() {
        AccessTokenReq accessTokenReq = new AccessTokenReq();
        accessTokenReq.setAppId(appId);
        accessTokenReq.setClientSecret(clientSecret);
        try {
            String response = HttpSenderUtil.postJsonDataWithFullToken(GET_ACCESS_TOKEN, JSONUtil.toJsonStr(accessTokenReq), null);
            JSONObject data = JSONUtil.parseObj(response);
            String token = (String) data.get("access_token");
            String expires = (String) data.get("expires_in");
            // 按提前30s过期处理
            int expiresSecond = Integer.parseInt(expires) - 30;
            accessTokenExpiresMap.clear();
            accessTokenExpiresMap.put(token, DateUtil.offsetSecond(new Date(), expiresSecond));
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getToken() {
        if (accessTokenExpiresMap.size() == 0) {
            return initToken();
        }
        // 如果过期了就重新获取
        for (String token : accessTokenExpiresMap.keySet()) {
            Date date = accessTokenExpiresMap.get(token);
            if (new Date().after(date)) {
                return initToken();
            }
            // 没过期 直接返
            return token;
        }
        return null;
    }

    public static void sendGroupMessageTxt(String groupId, String content, String msgId) {
        QQSenderReq qqSenderReq = new QQSenderReq();
        qqSenderReq.setContent(content);
        qqSenderReq.setMsg_id(msgId);
        qqSenderReq.setMsg_type(0);
        try {
            HttpSenderUtil.postJsonDataWithFullToken(BASE_URL + String.format(SEND_GROUP_MESSAGE, groupId), JSONUtil.toJsonStr(qqSenderReq), "QQBot " + getToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendGroupMessageMedia(String groupId, String media, String content, String msgId) {
        // 先把文件上传
        QQMediaInfo qqMediaInfo = new QQMediaInfo();
        qqMediaInfo.setUrl(media);
        qqMediaInfo.setFile_type(1);
        try {
            String mediaResp = HttpSenderUtil.postJsonDataWithFullToken(BASE_URL + String.format(SEND_GROUP_MEDIA, groupId), JSONUtil.toJsonStr(qqMediaInfo), "QQBot " + getToken());
            log.info("media: " + mediaResp);
            QQMediaSenderReq mediaData = JSONUtil.toBean(mediaResp, QQMediaSenderReq.class);
            QQSenderReq qqSenderReq = new QQSenderReq();
            qqSenderReq.setContent(content);
            qqSenderReq.setMedia(mediaData);
            qqSenderReq.setMsg_id(msgId);
            qqSenderReq.setMsg_type(7);
            String response = HttpSenderUtil.postJsonDataWithFullToken(BASE_URL + String.format(SEND_GROUP_MESSAGE, groupId), JSONUtil.toJsonStr(qqSenderReq), "QQBot " + getToken());
            log.info(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
