package com.bot.commom.constant;

import cn.hutool.core.util.StrUtil;

/**
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
public interface GameConsts {

    interface CommonTip {
        String EXIT_SUCCESS = "退出成功";
        String REG_TIP = "检测到您是第一次进入游戏，请输入昵称完成注册：";
        String LOGIN_TIP = "欢迎回来,[%s],当前活动："+ StrUtil.CRLF + "1.新用户注册登录即送3张唤灵符" + StrUtil.CRLF + "2.金系技能卡掉落几率翻倍，唤灵符掉落几率翻倍" + StrUtil.CRLF + "3.每日11:00-13:00、17:00-19:00将会有强大的异兽来袭，可在探索菜单进入挑战，海量唤灵符掉落哦~" + StrUtil.CRLF + "回复数字【1】进入游戏";
        String REPEAT_REG = "昵称重复了，重新输入：";
        String UN_KNOW_POINT = "未知指令，回复【0】返回";
        String ERROR_POINT = "指令错误，请输入正确的指令";
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
        String SKILL_ROUND = "效果持续%s回合";
        String SEE_BATTLE_DETAIL = "Q";
        String TURN_BACK = "0.返回";
        String TURN_BACK_ORCONTINU = "输入【0】返回或者输入数字1-4继续召唤";
        String LOCK = "当前游戏维护中，请输入【退出】以回到聊天模式，稍后再来尝试哦~";
        String GET_APPELLATION = "获得称号[%s],若已获得过请无视这条提示";
        String MENU_TIP = "回复菜单前的数字，进入相应功能";
        Integer MAX_LEVEL = 20;
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
        String DESCRIBE = "名称：%s" + StrUtil.CRLF + "等级：%s" + StrUtil.CRLF + "稀有度：%s" + StrUtil.CRLF
                + "称号：%s" + StrUtil.CRLF + "属性：%s" + StrUtil.CRLF +"阵营：%s" + StrUtil.CRLF
                +"家乡：%s" +  StrUtil.CRLF +"速度：%s" + StrUtil.CRLF + "攻击：%s" + StrUtil.CRLF
                + "体质：%s" + StrUtil.CRLF + "成长：%s" + StrUtil.CRLF + "简介：%s" + StrUtil.CRLF
                + "血量：%s" + StrUtil.CRLF + "经验值：%s/%s" + StrUtil.CRLF + "技能：%s";
    }

    interface MyPhantom {
        String MENU_NAME = "我的幻灵";
    }

    interface MyKnapsack {
        String MENU_NAME = "我的背包";
        String USE_GOODS_MENU = "使用道具";
        String GET_PHANTOM = "请前往【唤灵仪式】使用唤灵符";
        String USE_SKILL_CARD = "请选择需要使用的幻灵" + StrUtil.CRLF + "已自动过滤掉与此技能属性不符的幻灵、学满3个技能的幻灵和已学会该技能的幻灵";
        String USE_SKILL_NO_PHANTOM = "没有满足使用条件的幻灵";
        String BUFF_REPEAT = "已有类似功能的道具生效中，请勿重复使用";
        String BUFF_USE = "使用成功!快去试试效果吧~";
        String EMPTY = "该道具数量为0，无法使用";
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
        String DESCRIBE = "选择要切磋的好友";
        String PICK = "选择要出战的幻灵";
        String EMPTY = "你没有好友，赶紧去添加吧!";
        String TITLE_1 = "[%s]%s,战灵力: %s";
        String TITLE_2 = "%s,战灵力: %s";
        String FRIEND_EMPTY = "对方没有可出战的幻灵，换个好友试试吧";
        String PLAYER_EMPTY = "你没有可出战的幻灵，快去唤灵吧";
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
        Integer UP_LEVEL_NEED_EXP = 35;
        Integer MAX_EXP_GROW = 10;
        Double BASE_BUFF_FIGURE = 1.2;
        Double BASE_DE_BUFF_FIGURE = 0.8;
        Double BASE_ONE_NUMBER = 1.0;


        Integer POWER_ATTACK = 10;
        Integer POWER_SPEED = 3;
        Integer POWER_PHYSIQUE = 8;
    }

    interface Battle {
        String TITLE = "[%s]%s[%s]级";
        String FAIL = "战败了...赶紧去提升自己吧!"+ StrUtil.CRLF + "输入【Q】可查看战斗详情";
        String BUFF = "buff";
        String BATTLE_RECORD_FORMAT = "[战斗详情]" + StrUtil.CRLF;
        String BATTLE_RECORD_START = "战斗开始，%s血量%s，%s血量%s" + StrUtil.CRLF;
        String BATTLE_RECORD_ROUND = "第%s回合" + StrUtil.CRLF;
        String BATTLE_RECORD_PHANTOM = "%s使用了%s" + StrUtil.CRLF;
        String BATTLE_RECORD_ROUND_RESULT = "回合结束，%s剩余血量%s，%s剩余血量%s" + StrUtil.CRLF;
        String ATTACK = "普通攻击";
        String SUCCESS = "恭喜你，战斗胜利，输入【Q】可查看战斗详情";
        String END = "战斗结束";


        Integer LEVEL_NUMBER_1 = 3;
        String GET_RESULT_EXP = "出战幻灵经验值：+%s";
        String EXP_MAX = "出战幻灵已达到最高等级，不增加经验值";
        String GET_RESULT_GOOD = "获得%s：%s，可前往背包查看详情";
        String GET_RESULT_GOOD_EMTPY = "此次战斗无物品掉落";
        String BOOS_RESULT = "共造成%s点伤害" + StrUtil.CRLF + "获得%s：%s，数量[%s]可前往背包查看详情" + StrUtil.CRLF + "Q.查看战斗详情" +StrUtil.CRLF + "0.返回";
    }

    interface Help {
        String MENU_NAME = "帮助手册";
        String HELP = "[山海见闻]是一个集卡式养成游戏，它最大的特点就是几乎所有的可玩点都是看运气的，比如："+ StrUtil.CRLF
                + "抽卡是看运气的，幻灵的稀有度由低到高分为普通、精英、珍稀、绝世。" + StrUtil.CRLF
                + "打怪是看运气的，去不同的地方探索，会遇到不一样的怪物，你可能在一级的时候就遇到十几级的大怪，这个时候，我由衷的建议你选择逃跑。" + StrUtil.CRLF
                + "掉落是看运气的，击败怪物后有不小的几率可以掉落物品，其中包括但不限于唤灵符、技能卡等道具。" + StrUtil.CRLF
                + "养成是看运气的，一模一样的幻灵，随着等级的增长各项属性会完全不一样。" + StrUtil.CRLF
                + "什么，全部都靠运气，可玩性在哪啊？" + StrUtil.CRLF
                + "问得好。" + StrUtil.CRLF
                + "属性是相克的，当幻灵属性被克制的时候自身的攻击力会降低，对方的攻击力会增加，所以请谨慎选择出战幻灵，克制关系为金>木>土>水>火>金。" + StrUtil.CRLF
                + "技能是可以学习的，打怪掉落的技能卡，可以拿来让与其属性相同的幻灵学会上面的技能，这样一来我们可以看到很多一模一样的幻灵，却有着截然不同的属性和技能，这样不是很有趣吗？" + StrUtil.CRLF
                + "当然了，技能的学习是有上限的，每个幻灵最多只能学会3个技能，所以要谨慎选择哦。" + StrUtil.CRLF
                + "对了，不同的技能卡可能藏在不同的怪物身上，要记得多去探索几个地方。" + StrUtil.CRLF
                + "不过，这个世界很危险，当你还是新手，还比较弱小的时候，我由衷的建议你多去东海走走，那里比较宜居。";
    }

    interface WorldBoss {
        String TITLE = "世界Boss";
        String NOT_IN_TIME = "当前不在开放时间内，世界Boos开放时间为每日11:00-13:00以及17:00-19:00";
        String BOOS = "当前Boos[%s],等级[%s],属性[%s],血量[%s/%s]";
        String PICK = "请选择要出战的幻灵";
        String FINISH = "当前Boos已被击退，下次早点来吧!";
        String OVER_TIMES = "当前Boos你已经挑战得够多了，休息休息吧!";
        Integer INIT_HP = 50000;
        Integer MAX_TIME = 2;
    }

    interface Sign{
        String MENU_NAME = "签到";
        String SIGN_SUCCESS = "签到成功，获得了[%s]";
        String SIGN_REPEAT = "今天已经签到过了，明天再来吧~";
        String SIGN_NOTHING = "很遗憾，什么都没有得到...";
    }
}
