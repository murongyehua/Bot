package com.bot.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.dto.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendMsgUtil {

    private static final String THUMB_PATH = "https://img31.mtime.cn/pi/2016/09/12/104656.40455111_1000X1000.jpg";

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

    public static void sendFile(String userId, String url) {
        try {
            SendMsgDTO sendMsg = new SendMsgDTO();
            sendMsg.setPath(url);
            String[] arr = url.split("\\.");
            sendMsg.setFileName(DateUtil.now() + StrUtil.DOT + arr[arr.length - 1]);
            sendMsg.setWcId(userId);
            sendMsg.setWId(SystemConfigCache.wId);
            HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.SEND_FILE_URL, JSONUtil.toJsonStr(sendMsg));
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送消息失败");
        }
    }

    public static void sendVideo(String userId, String url) {
        try {
            SendMsgDTO sendMsg = new SendMsgDTO();
            sendMsg.setPath(url);
            sendMsg.setWcId(userId);
            sendMsg.setWId(SystemConfigCache.wId);
            sendMsg.setThumbPath(THUMB_PATH);
            HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.SEND_VIDEO_URL, JSONUtil.toJsonStr(sendMsg));
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送消息失败");
        }
    }

    public static void sendGroupVideo(String groupId, String url, String userId) {
        try{
            SendGroupDTO sendGroup = new SendGroupDTO();
            sendGroup.setWId(SystemConfigCache.wId);
            sendGroup.setAt(userId);
            sendGroup.setWcId(groupId);
            sendGroup.setPath(url);
            sendGroup.setThumbPath(THUMB_PATH);
            HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.SEND_VIDEO_URL, JSONUtil.toJsonStr(sendGroup));
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送消息失败");
        }
    }



    public static void sendGroupMsg(String groupId, String msg, String userId) {
        try{
            SendGroupDTO sendGroup = new SendGroupDTO();
            sendGroup.setWId(SystemConfigCache.wId);
            sendGroup.setAt(userId);
            if (StrUtil.isEmpty(userId)) {
                sendGroup.setContent(msg);
            }else {
                sendGroup.setContent(String.format("@%s\u2005", getGroupNickName(groupId, userId)) + StrUtil.CRLF + msg);
            }
            sendGroup.setWcId(groupId);
            HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.SEND_TEXT_URL, JSONUtil.toJsonStr(sendGroup));
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送消息失败");
        }

    }

    /**
     * 发送朋友圈
     * @param content 文字内容
     * @param urls 图片 多个用;分隔 单张图片最大3M以内,必须是png格式
     * @return
     */
    public static void snsSendImage(String content, String urls) {
        try {
            SendFriendDTO sendFriendDTO = new SendFriendDTO();
            sendFriendDTO.setContent(content);
            sendFriendDTO.setPaths(urls);
            sendFriendDTO.setWId(SystemConfigCache.wId);
            HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.SEND_TEXT_URL, JSONUtil.toJsonStr(sendFriendDTO));
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送朋友圈失败");
        }
    }

    private static String getGroupNickName(String groupId, String userId) {
        if (userId.equals("notify@all")) {
            return "所有人";
        }
        try {
            QueryGroupUserDTO queryGroupUser = new QueryGroupUserDTO();
            queryGroupUser.setWId(SystemConfigCache.wId);
            queryGroupUser.setChatRoomId(groupId);
            queryGroupUser.setUserList(userId);
            String jsonStr = HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.QUERY_GROUP_USER_URL, JSONUtil.toJsonStr(queryGroupUser));
            JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
            JSONObject data = (JSONObject) (((JSONArray)jsonObject.get("data")).get(0));
            if (data == null) {
                System.out.println("获取群成员昵称失败");
                return "";
            }
            return (String) data.get("nickName");
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送消息失败");
        }
        return "";
    }



}
