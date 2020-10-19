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
        String UN_KNOW_POINT = "未知指令，回复【0】返回";
        String EMPTY = "无";
        String APPELLATION_EMPTY = "您还没有称号";
        String APPELLATION_CHANGE = "输入称号前的编号以完成更换";
        String PLAY_SUCCESS = "操作成功，目前菜单仍停留在上一层，可输入【0】返回，或继续操作";
        String GOODS_EMPTY = "啊哦，这里什么也没有哦~";
        String GOODS_UES = "输入物品前的编号查看详情或使用";
        String PHANTOM_LOOK = "输入幻灵前的编号查看详细信息";
        String PHANTOM_EMPTY = "您还没有幻灵，赶紧去唤灵仪式吧~";
        String SKILL_DESCRIBE = "【%s】";
        String SKILL_WAIT_ROUND = "冷却时间%s回合";
        String SKILL_ROUNT = "效果持续%s回合";
        String SEE_BATTLE_DETAIL = "Q";
    }

    interface PlayerInfo {
        String MENU_NAME = "个人信息";
        String DESCRIBE = "昵称：[%s]" + StrUtil.CRLF
                + "称号：[%s]" + StrUtil.CRLF
                + "幻灵数：[%s]" + StrUtil.CRLF
                + "战灵力：[%s]" + StrUtil.CRLF;
    }

    interface GoodsDetail {
        String DESCRIBE = "名称：%s" + StrUtil.CRLF + "数量：%s" + StrUtil.CRLF + "用途：%s";
        String USE = "使用";
    }

    interface PhantomDetail {
        String MENU_NAME = "[%s]%s(%s级)";
        String DESCRIBE = "名称：%s" + StrUtil.CRLF + "等级：%s" + "稀有度：%s" + StrUtil.CRLF
                + "称号：%s" + StrUtil.CRLF + "属性：%s" + StrUtil.CRLF +"阵营：%s" + StrUtil.CRLF
                +"家乡：%s" +  StrUtil.CRLF +"速度：%s" + StrUtil.CRLF + "攻击：%s" + StrUtil.CRLF
                + "体质：%s" + StrUtil.CRLF + "成长：%s" + StrUtil.CRLF + "简介：%s" + StrUtil.CRLF
                + "技能：%s";
    }

    interface MyPhantom {
        String MENU_NAME = "我的幻灵";
    }

    interface MyKnapsack {
        String MENU_NAME = "我的背包";
    }

    interface GetPhantom {
        String MENU_NAME = "唤灵仪式";
        String WAIT_GET_1 = "灵符燃尽，有四位幻灵表示了兴趣，却不愿露面";
        String WAIT_GET_2 = "请跟随您的直觉，输入代号前面的数字";
        String SUCCESS = "唤灵成功，当前唤灵符数量: %s，此时返回不会消耗唤灵符!";
        String GET_1 = "【%s】幻灵，[%s]%s，觐见!";
        String GET_2 = "详细资料可前往[我的幻灵]菜单查看";
        String REPEAT = "该幻灵已拥有，自动转为成长值+1，若已达到该稀有度所能承受的成长上限，则不会再增加!";
        String WAIT_1 = "天";
        String WAIT_2 = "地";
        String WAIT_3 = "玄";
        String WAIT_4 = "黄";
        String CAN_GET_TIME = "当前拥有唤灵符数量为0，无法唤灵";
    }

    interface Explore {
        String MENU_NAME = "外出探索";
        String TIP = "输入所要探索的地区前的编号：";
        String MEET = "你遭遇了%s[%s][%s级]，请选择你要出战的幻灵";
        String RUN = "逃跑";
    }

    interface MyFriends {
        String MENU_NAME = "我的好友";
        String EMPTY = "暂无好友";
        String FIND_MENU_NAME = "添加好友";
        String ADD_SUCCESS = "添加成功!回复数字【0】返回";
        String NOT_FOUND = "目标玩家不存在，请确认名称正确后再次输入，或者回复数字【0】返回";
        String ADD_TIP = "输入玩家名称进行添加：";
        String REPEAT = "该玩家已经是你的好友，请勿重复添加，回复数字【0】返回";
    }

    interface FriendCompare {
        String MENU_NAME = "好友切磋";
    }

    interface RankList {
        String MENU_NAME = "排行榜";
        String MY_POSITION = "我的名次：%s";
        String ELEMENT_WITH_APPELLATION = "%s.[%s]%s 战灵力：%s";
        String ELEMENT_APPELLATION = "%s.%s 战灵力：%s";
        Integer SHOW_NUMBER = 10;
    }

    interface MapperName {
        String BASE_GOODS = "baseGoodsMapper";
        String BASE_PHANTOM = "basePhantomMapper";
        String BASE_SKILL = "baseSkillMapper";
        String GAME = "gameMapper";
        String GAME_PLAYER = "gamePlayerMapper";
        String PLAYER_APPELLATION = "playerAppellationMapper";
        String PLAYER_FRIENDS = "playerFriendsMapper";
        String PLAYER_GOODS = "playerGoodsMapper";
        String PLAYER_PHANTOM = "playerPhantomMapper";
        String BASE_MONSTER = "baseMonsterMapper";
    }

    interface LittlePrinter {
        String CHANGE_APPELLATION = "更换称号";
    }

    interface BaseFigure {
        Integer SPEED_FOR_EVERY_LEVEL = 2;
        Integer ATTACK_FOR_EVERY_LEVEL = 10;
        Integer HP_FOR_EVERY_LEVEL = 100;
        Integer DEFENSE_FOR_EVERY_LEVEL = 1;
        Integer SPEED_POINT = 1;
        Integer ATTACK_POINT = 3;
        Integer HP_POINT = 10;
        Integer DEFENSE_POINT = 2;
        Integer UP_LEVEL_NEED_EXP = 150;
        Double BASE_BUFF_FIGURE = 1.2;
        Double BASE_DE_BUFF_FIGURE = 0.8;
        Double BASE_ONE_NUMBER = 1.0;


        Integer POWER_ATTACK = 10;
        Integer POWER_SPEED = 3;
        Integer POWER_PHYSIQUE = 8;
    }

    interface Battle {
        String TITLE = "[%s]%s[%s]级";
        String FAIL = "战败了...赶紧去提升自己吧!";
        String BUFF = "buff";
        String BATTLE_RECORD_FORMAT = "[战斗详情]" + StrUtil.CRLF;
        String BATTLE_RECORD_START = "战斗开始，%s血量%s，%s血量%s" + StrUtil.CRLF;
        String BATTLE_RECORD_ROUND = "第%s回合" + StrUtil.CRLF;
        String BATTLE_RECORD_PHANTOM = "%s使用了%s" + StrUtil.CRLF;
        String BATTLE_RECORD_ROUND_RESULT = "回合结束，%s剩余血量%s，%s剩余血量%s" + StrUtil.CRLF;
        String ATTACK = "普通攻击";
        String SUCCESS = "恭喜你，战斗胜利，输入【Q】可查看战斗详情";
        String END = "战斗结束";
    }
}
