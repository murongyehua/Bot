package com.bot.common.util;

import cn.hutool.json.JSONUtil;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.dto.SendCardDTO;
import com.bot.common.dto.SendGroupDTO;
import com.bot.common.dto.SendMsgDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendMsgUtil {

    public static void sendMsg(String userId, String msg) {
        try {
            SendMsgDTO sendMsg = new SendMsgDTO();
            sendMsg.setContent(msg);
            sendMsg.setWcId(userId);
            sendMsg.setWId(SystemConfigCache.wId);
             HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.SEND_TEXT_URL, JSONUtil.toJsonStr(sendMsg));
        }catch (Exception e) {
            System.out.println("发送消息失败");
        }
    }

    public static void sendCard(String userId) {
        try {
            SendCardDTO sendMsg = new SendCardDTO();
            sendMsg.setNameCardId(SystemConfigCache.OWNER_ID);
            sendMsg.setWcId(userId);
            sendMsg.setWId(SystemConfigCache.wId);
            HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.SEND_TEXT_URL, JSONUtil.toJsonStr(sendMsg));
        }catch (Exception e) {
            System.out.println("发送消息失败");
        }
    }

    public static void sendImg(String userId, String url) {
        try {
            SendMsgDTO sendMsg = new SendMsgDTO();
            sendMsg.setContent(url);
            sendMsg.setWcId(userId);
            sendMsg.setWId(SystemConfigCache.wId);
            HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.SEND_IMG_URL, JSONUtil.toJsonStr(sendMsg));
        }catch (Exception e) {
            System.out.println("发送消息失败");
        }
    }

    public static void sendGroupMsg(String groupId, String msg, String at) {
        try{
            SendGroupDTO sendGroup = new SendGroupDTO();
            sendGroup.setWId(SystemConfigCache.wId);
            sendGroup.setAt(at);
            sendGroup.setContent(msg);
            sendGroup.setWcId(groupId);
            HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.SEND_TEXT_URL, JSONUtil.toJsonStr(sendGroup));
        }catch (Exception e) {
            System.out.println("发送消息失败");
        }

    }

}
