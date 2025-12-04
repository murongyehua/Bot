package com.bot.common.config;


import com.bot.common.dto.ActivityAwardDTO;
import com.bot.common.enums.ENChatEngine;
import com.bot.common.enums.ENRegDay;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SystemConfigCache {

    public static String baseUrl = "";

    public static String token = "";

    public static String wId = "";

    /**
     * 试用邀请码-长期有效-每人仅能使用一次
     */
    public static String inviteCode = "";

    public static List<String> topToken;

    public static List<String> signToken;

    public static String tuilanToken;

    public static String isMaintenance = "";

    /**
     * 临时邀请码-使用后失效-用于成为正式用户
     */
    public static Map<String, ENRegDay> tempInviteCode = new HashMap<>();

    public static final String SEND_TEXT_URL = "/sendText";

    public static final String SEND_IMG_URL = "/sendImage2";

    public static final String SEND_FILE_URL = "/sendFile";

    public static final String SEND_VIDEO_URL = "/sendVideo";

    public static final String SEND_EMOJI_URL = "/sendEmoji";

    public static final String QUERY_GROUP_USER_URL = "/getChatRoomMemberInfo";

    public static final String PUSH_FRIEND_URL = "/snsSendImage";

    public static final String SEND_AUDIO_URL = "/sendVoice";

    public static final String FETCH_PICTURE_URL = "/getMsgImg";

    public static final String FETCH_CHAT_ROOM_USERS = "/getChatRoomMember";

    public static final String ACCEPT_URL = "/acceptUrl";

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
     * 日报订阅情况
     */
    public static final Map<String, String> userMorningMap = new HashMap<>();

    /**
     * 日报发送情况
     */
    public static final Map<String, List<String>> morningSendMap = new HashMap<>();

    /**
     * 激活了打工日历的用户
     */
    public static final List<String> userWorkDaily = new ArrayList<>();

    /**
     * 聊天频率
     */
    public static final Map<String, String> chatFrequency = new HashMap<>();

    /**
     * 开了开服提示的 用户
     */
    public static final List<String> openServer = new ArrayList<>();

    /**
     * 开启了表情的用户
     */
    public static final List<String> emojiUser = new ArrayList<>();

    /**
     * 开启了自动推送漂流瓶的用户
     */
    public static final List<String> bottleUser = new ArrayList<>();

    /**
     * 设置了欢迎语的用户
     */
    public static final Map<String, String> welcomeMap = new HashMap<>();

    /**
     * 当前开启的抽奖活动奖品
     */
    public static final CopyOnWriteArrayList<ActivityAwardDTO> activityAwardList = new CopyOnWriteArrayList<>();

}
