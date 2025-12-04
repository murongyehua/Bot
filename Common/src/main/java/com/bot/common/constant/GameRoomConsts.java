package com.bot.common.constant;

/**
 * 游戏房间相关常量
 * @author Assistant
 */
public class GameRoomConsts {

    /**
     * 时间格式
     */
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 房间超时时间(分钟)
     */
    public static final int ROOM_TIMEOUT_MINUTES = 10;

    /**
     * 房间编号长度
     */
    public static final int ROOM_CODE_LENGTH = 5;

    /**
     * 房间编号范围
     */
    public static final int ROOM_CODE_MIN = 10000;
    public static final int ROOM_CODE_MAX = 100000;

    /**
     * 房间密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 16;

    /**
     * 游戏列表查询上下文超时时间(毫秒) - 5分钟
     */
    public static final long GAME_LIST_CONTEXT_TIMEOUT = 5 * 60 * 1000L;

    /**
     * 指令关键字
     */
    public static class Command {
        public static final String GAME_HALL = "小林游戏大厅";
        public static final String CREATE_ROOM = "创建房间";
        public static final String JOIN_ROOM = "加入房间";
        public static final String LEAVE_ROOM = "离开房间";
        public static final String START_GAME = "开始游戏";
        public static final String SWITCH_GAME = "切换游戏";
        public static final String GAME_LIST = "小林游戏";
    }

    /**
     * 友好提示信息
     */
    public static class Tips {
        public static final String ALREADY_IN_ROOM = "您当前已在房间[%s]中，请先离开当前房间再加入新房间哦~";
        public static final String ROOM_NOT_FOUND = "房间号不存在，请检查后重试~";
        public static final String GAME_STARTED = "该房间游戏已开始，暂时无法加入，请稍后再试~";
        public static final String WRONG_PASSWORD = "房间密码错误，无法加入~";
        public static final String ROOM_FULL = "房间已满(%d/%d)，无法加入~";
        public static final String JOIN_SUCCESS = "成功加入房间[%s]，当前人数：%d/%d";
        public static final String LEAVE_SUCCESS = "已离开房间[%s]";
        public static final String NOT_IN_ROOM = "您当前不在任何房间中~";
        public static final String NOT_ROOM_MASTER = "只有房主才能执行此操作~";
        public static final String ROOM_NOT_WAITING = "房间必须处于等待状态才能执行此操作~";
        public static final String PLAYER_NOT_ENOUGH = "当前人数不足，至少需要%d人才能开始游戏~";
        public static final String PLAYER_NOT_FIT = "当前房间人数(%d)不符合目标游戏的人数要求(%d-%d人)~";
        public static final String CREATE_ROOM_SUCCESS = "房间创建成功！\n房间号：%s\n游戏：%s\n人数限制：%d-%d人\n房间类型：%s";
        public static final String START_GAME_SUCCESS = "游戏即将开始，请所有玩家准备！";
        public static final String SWITCH_GAME_SUCCESS = "成功切换到游戏：%s";
        public static final String GAME_NOT_FOUND = "游戏不存在，请输入【小林游戏】查看支持的游戏列表~";
        public static final String PASSWORD_TOO_LONG = "房间密码不能超过16个字符~";
        public static final String HALL_EMPTY = "当前游戏大厅空空如也，快创建一个房间吧~";
        public static final String GAME_LIST_HEADER = "=== 小林游戏列表 ===\n";
        public static final String GAME_LIST_ITEM = "%d. %s (%d-%d人)\n";
        public static final String GAME_LIST_FOOTER = "\n发送游戏编号查看详情";
        public static final String INVALID_GAME_NO = "无效的游戏编号~";
        public static final String GAME_LIST_EXPIRED = "游戏列表已过期，请重新查询~";
    }
}
