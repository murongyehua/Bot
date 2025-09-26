package com.bot.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.dto.*;
import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.MultimediaObject;

import java.io.File;

@Slf4j
public class SendMsgUtil {

    private static final String THUMB_PATH = "https://img31.mtime.cn/pi/2016/09/12/104656.40455111_1000X1000.jpg";

    private static final String AUDIO_URL = "http://113.45.63.97//file/audio/";

    private static final String AUDIO_PATH = "/data/files/audio/";

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

    public static void sendAudio(String userId, String fileName) {
        try {
            SendAudioDTO sendAudio = new SendAudioDTO();
            sendAudio.setWcId(userId);
            sendAudio.setWId(SystemConfigCache.wId);
            String transFileName = "trans_" + fileName + ".silk";
            File source = new File(AUDIO_PATH + fileName);
            File target = new File(AUDIO_PATH + transFileName);
            log.info("-=----1");
            AudioTransUtil.mp3ToSilkUtil(source, target);
            log.info("-=----2");
            sendAudio.setLength((int) AudioTransUtil.getPCMDurationMilliSecond(source));
            log.info("-=----6");
            sendAudio.setContent(AUDIO_URL + transFileName);
            log.info("语音发送参数：" + sendAudio);
            String response = HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.SEND_AUDIO_URL, JSONUtil.toJsonStr(sendAudio));
            log.info("语音发送结果：" + response);
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送语音失败");
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
     * 获取聊天图片，返回本地图片名称
     * @param content
     * @param msgId
     * @return
     */
    public static String fetchPicture(String content, Long msgId) {
        try {
            FetchPictureDTO fetchPictureDTO = new FetchPictureDTO();
            fetchPictureDTO.setContent(content);
            fetchPictureDTO.setWId(SystemConfigCache.wId);
            fetchPictureDTO.setMsgId(msgId);
            String response = HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.FETCH_PICTURE_URL, JSONUtil.toJsonStr(fetchPictureDTO));
            log.info(response);
            JSONObject data = (JSONObject) JSONUtil.parseObj(response).get("data");
            File img = new File("/data/files/pic/" + System.currentTimeMillis() + ".png");
            HttpUtil.downloadFile((String) data.get("url"), img);
            return img.getName();
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取图片失败");
        }
        return null;
    }

    /**
     * 获取群聊人数
     * @param groupId
     * @return
     */
    public static int getChatRoomUserCount(String groupId) {
        try {

            FetchGroupUsersDTO fetchGroupUsersDTO = new FetchGroupUsersDTO();
            fetchGroupUsersDTO.setWId(SystemConfigCache.wId);
            fetchGroupUsersDTO.setChatRoomId(groupId);
            String response = HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.FETCH_CHAT_ROOM_USERS, JSONUtil.toJsonStr(fetchGroupUsersDTO));
            JSONArray userList = (JSONArray) JSONUtil.parseObj(response).get("data");
            return userList.size();
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取群人数失败");
        }
        // 失败的时候按0人
        return 0;
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
            String response = HttpSenderUtil.postJsonData(SystemConfigCache.baseUrl + SystemConfigCache.PUSH_FRIEND_URL, JSONUtil.toJsonStr(sendFriendDTO));
            System.out.println(response);
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
