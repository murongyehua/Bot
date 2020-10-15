package com.bot.commom.constant;

import cn.hutool.core.util.StrUtil;

/**
 * @author liul
 * @version 1.0 2020/10/14
 */
public interface GameConsts {

    interface CommonTip {
        String EXIT_SUCCESS = "退出成功";
        String REG_TIP = "检测到您是第一次进入游戏，请输入昵称完成注册：";
        String LOGIN_TIP = "欢迎回来,[%s]，当前活动：1234567" + StrUtil.CRLF + "回复数字【1】进入游戏";
        String REPEAT_REG = "昵称重复了，重新输入：";
        String UN_KNOW_POINT = "未知指令";
    }

    interface PlayerInfo {
        String MENU_NAME = "个人信息";
    }

    interface MyPhantom {
        String MENU_NAME = "我的幻灵";
    }

    interface MyKnapsack {
        String MENU_NAME = "我的背包";
    }

    interface GetPhantom {
        String MENU_NAME = "唤灵仪式";
    }

    interface Explore {
        String MENU_NAME = "外出探索";
    }

    interface MyFriends {
        String MENU_NAME = "我的好友";
    }

    interface FriendCompare {
        String MENU_NAME = "好友切磋";
    }

    interface RankList {
        String MENU_NAME = "排行榜";
    }

}
