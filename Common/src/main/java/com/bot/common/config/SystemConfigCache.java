package com.bot.common.config;


import com.bot.common.enums.ENChatEngine;
import com.bot.common.enums.ENRegDay;

import java.util.*;

public class SystemConfigCache {

    public static String baseUrl = "";

    public static String token = "";

    public static String wId = "";

    /**
     * 试用邀请码-长期有效-每人仅能使用一次
     */
    public static String inviteCode = "";

    /**
     * 临时邀请码-使用后失效-用于成为正式用户
     */
    public static Map<String, ENRegDay> tempInviteCode = new HashMap<>();

    public static final String SEND_TEXT_URL = "/sendText";

    public static final String SEND_IMG_URL = "/sendImage2";

    public static final String SEND_FILE_URL = "/sendFile";

    public static final String SEND_VIDEO_URL = "/sendVideo";

    public static final String QUERY_GROUP_USER_URL = "/getChatRoomMemberInfo";

    public static final String OWNER_ID = "wxid_gt9s4k14zz7l22";

    /**
     * 用户到期时间
     */
    public static final Map<String, Date> userDateMap = new HashMap<>();

    /**
     * 用户聊天引擎
     */
    public static final Map<String, ENChatEngine> userChatEngine = new HashMap<>();
    /**
     * 晨报订阅情况
     */
    public static final Map<String, String> userMorningMap = new HashMap<>();

    /**
     * 日报发送情况
     */
    public static final Map<String, List<String>> morningSendMap = new HashMap<>();

}
