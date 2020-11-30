package com.bot.common.constant;

import cn.hutool.core.util.StrUtil;

/**
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
public interface GameConsts {

    interface CommonTip {
        String EXIT_SUCCESS = "退出成功";
        String REG_TIP = "检测到您是第一次进入游戏，请输入昵称完成注册：";
        String LOGIN_TIP = "欢迎回来,[%s],回复数字【1】进入游戏,当前公告："+ StrUtil.CRLF + "新用户注册登录即送3张唤灵符" + StrUtil.CRLF + "2.9.0版本更新内容\n" +
                "【基础变更】\n" +
                "1. 所有百分比伤害设置了伤害上限，最多造成3.5倍伤害(反弹伤害除外)\n" +
                "2. 世界boos伤害削弱\n" +
                "3. 调整了各类幻灵召唤概率，绝世:珍稀:精英:普通=1:3:7:9\n" +
                "【新增内容】\n" +
                "1. 昆仑仙境新剧情版本预热开启\n" +
                "2. 商店新增昆仑道具[九转灵泉水]，使用后可以给幻灵直接增加一点属性\n" +
                "【更新补偿】\n" +
                "此次更新后全服玩家获得 唤灵符 * 2\n" +
                "更新补偿将通过邮件形式发放\n";
        String REPEAT_REG = "昵称重复了，重新输入：";
        String UN_KNOW_POINT = "未知指令，回复【0】返回上级菜单，【00】返回主菜单";
        String ERROR_POINT = "指令错误，请输入正确的指令";
        String EMPTY = "无";
        String APPELLATION_EMPTY = "您还没有称号";
        String APPELLATION_CHANGE = "输入称号前的编号以完成更换";
        String PLAY_SUCCESS = "操作成功，目前菜单仍停留在上一层，可输入【0】返回，或继续操作";
        String GOODS_EMPTY = "啊哦，这里什么也没有哦~";
        String GOODS_UES = "输入物品前的编号可查看详情、使用或出售";
        String PHANTOM_LOOK = "输入幻灵前的编号查看详细信息";
        String PHANTOM_EMPTY = "您还没有幻灵，赶紧去唤灵仪式吧~";
        String SKILL_DESCRIBE = "【%s】";
        String SKILL_WAIT_ROUND = "冷却时间%s回合";
        String SKILL_ROUND = "效果持续%s回合";
        String SEE_BATTLE_DETAIL = "Q";
        String SEE_VERSION_HISTORY = "A";
        String TURN_BACK = "0.返回";
        String TURN_BACK_ORCONTINU = "输入【0】返回或者输入数字1-4继续召唤";
        String LOCK = "当前游戏维护中，请输入【退出】以回到聊天模式，稍后再来尝试哦~";
        String GET_APPELLATION = "获得称号[%s],若已获得过请无视这条提示";
        String MENU_TIP = "回复菜单前的数字，进入相应功能";
        String NOW_VERSION = "当前版本：【盛世凡音】";
        String SEE_VERSION_TIP = "输入[A]可查看历史版本详情";
        Integer MAX_LEVEL = 25;
    }

    interface PlayerInfo {
        String MENU_NAME = "个人信息";
        String DESCRIBE = "昵称：[%s]" + StrUtil.CRLF
                + "称号：[%s]" + StrUtil.CRLF
                + "幻灵数：[%s]" + StrUtil.CRLF
                + "战灵力：[%s]" + StrUtil.CRLF
                + "灵石：[%s]" + StrUtil.CRLF
                + "体力：[%s]" + StrUtil.CRLF
                + "法宝：[%s]";
    }

    interface GoodsDetail {
        String DESCRIBE = "名称：%s" + StrUtil.CRLF + "数量：%s" + StrUtil.CRLF + "出售单价：%s灵石" +StrUtil.CRLF+ "用途：%s";
        String USE = "使用";
    }

    interface PhantomDetail {
        String MENU_NAME = "【%s】[%s]%s,%s级[%s][%s]";
        String DESCRIBE = "名称：%s" + StrUtil.CRLF + "等级：%s" + StrUtil.CRLF + "稀有度：%s" + StrUtil.CRLF
                + "称号：%s" + StrUtil.CRLF + "属性：%s" + StrUtil.CRLF +"阵营：%s" + StrUtil.CRLF
                +"家乡：%s" +  StrUtil.CRLF +"速度：%s" + StrUtil.CRLF + "攻击：%s" + StrUtil.CRLF
                + "体质：%s" + StrUtil.CRLF + "成长：%s" + StrUtil.CRLF + "简介：%s" + StrUtil.CRLF
                + "血量：%s" + StrUtil.CRLF + "经验值：%s/%s" + StrUtil.CRLF + "技能：%s";
        String MENU_NAME_NO_CARRIED = "【%s】[%s]%s,%s级[%s]";
    }

    interface MyPhantom {
        String MENU_NAME = "我的幻灵";
        String SUCCESS = "操作成功，赶紧带着幻灵出去探索吧~";
    }

    interface MyKnapsack {
        String MENU_NAME = "我的背包";
        String USE_GOODS_MENU = "使用道具";
        String SALE_GOODS_MENU = "出售道具(目前仅支持全部卖出，请谨慎选择)";
        String GET_PHANTOM = "请前往【唤灵仪式】使用唤灵符";
        String USE_SKILL_CARD = "请选择需要使用的幻灵" + StrUtil.CRLF + "已自动过滤掉与此技能属性不符的幻灵、学满3个技能的幻灵和已学会该技能的幻灵";
        String USE_SKILL_NO_PHANTOM = "没有满足使用条件的幻灵";
        String BUFF_REPEAT = "已有类似功能的道具生效中，请勿重复使用";
        String BUFF_USE = "使用成功!快去试试效果吧~";
        String EMPTY = "该道具数量为0，无法使用";
        String CHOOSE_RESET = "选择需要使用洗髓丹的幻灵";
        String CHOOSE_ADD = "选择需要使用九转灵泉水的幻灵";
        String CHOOSE_FORGET = "选择需要使用溢灵散的幻灵";
        String CHOOSE_ATTRIBUTE = "选择要洗去的属性" + StrUtil.CRLF + "所选属性将洗去1点，并自动随机加到其他属性上面" + StrUtil.CRLF + "属性最低为1，已自动隐去不可洗的属性";
        String CHOOSE_ATTRIBUTE_2 = "选择要增加的属性";
        String CHOOSE_SKILL = "选择要遗忘的技能";
        String NO_SKILL = "该幻灵没有技能";
        String TITLE = "%s: %s";
        String SALE_RESULT = "共计出售%s个%s，获得%s灵石";
        String SKILL_FORGET_SUCCESS = "使用成功，所选技能已遗忘";
        String ACTION_POINT_FULL = "目前体力充沛，不要浪费哦~";
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
        String MEET = "你消耗了1点体力，遭遇了%s[%s][%s级]，请选择你要出战的幻灵";
        String RUN = "逃跑";
        String NO_ACTION_POINT = "体力不足，无法探索，恢复恢复了再来试试吧~";
        String NO_MONSTER = "你使用了道具，而此地没有等级符合限制条件的怪物，请换个地图试试~";
        String EXPLORER_ALL_AREA = "游历四海(打怪升级)";
    }

    interface MyFriends {
        String MENU_NAME = "我的好友";
        String EMPTY = "暂无好友";
        String FIND_MENU_NAME = "添加好友";
        String ADD_SUCCESS = "添加成功!回复数字【0】返回";
        String NOT_FOUND = "目标玩家不存在，请确认名称正确后再次输入，或者回复数字【0】返回";
        String ADD_TIP = "输入玩家名称进行添加：";
        String REPEAT = "该玩家已经是你的好友，请勿重复添加，回复数字【0】返回";
        String CHOOSE_TIP = "输入好友前的编号，进行更多操作";
    }

    interface FriendCompare {
        String MENU_NAME = "切磋";
        String DESCRIBE = "选择要切磋的好友";
        String PICK = "选择要出战的幻灵";
        String EMPTY = "你没有好友，赶紧去添加吧!";
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
        String BASE_WEAPON = "baseWeaponMapper";
        String PLAYER_WEAPON = "playerWeaponMapper";
        String MESSAGE = "messageMapper";
        String GOODS_BOX = "goodsBoxMapper";
    }

    interface LittlePrinter {
        String CHANGE_APPELLATION = "更换称号";
        String CHANGE_WEAPON = "更换法宝";

    }

    interface Weapon {
        String NO_WEAPON = "你还没有法宝，快去获取吧!";
        String WAIT_WEAPON = "选择法宝查看详情";
        String WEAPON_DETAIL = "效果：%s" + StrUtil.CRLF + "灵气：%s 级" + StrUtil.CRLF + "介绍：" + StrUtil.CRLF + "%s";
        String CHANGE = "装备";
        String SUCCESS = "装备成功，0返回";
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

        Integer WEAPON_ONE = 500;
        Integer WEAPON_LEVEL = 200;
        Integer WEAPON_MAX = 400;
        Integer HALF = 50;
        // 人生赢家
        Integer HAS_PHANTOM = 60;
        // 小资生活
        Integer HAS_MONEY_1 = 1000;
        // 家缠万贯
        Integer HAS_MONEY_2 = 10000;
        // 富可敌国
        Integer HAS_MONEY_3 = 100000;
        // 珠光宝气
        Integer HAS_WEAPON = 5;
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
        String BOOS_RESULT = "共造成%s点伤害" + StrUtil.CRLF + "获得%s：%s，灵石*%s可前往背包查看详情" + StrUtil.CRLF + "Q.查看战斗详情" +StrUtil.CRLF + "0.返回";
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
        String TITLE = "诛神法坛(世界Boos)";
        String NOT_IN_TIME = "当前不在开放时间内，法坛开放时间为每日11:00-13:00以及17:00-19:00";
        String BOOS = "当前神兽[%s],等级[%s],属性[%s],血量[%s/%s]";
        String PICK = "请选择要出战的幻灵";
        String FINISH = "当前神兽已被击退，下次早点来吧!";
        String OVER_TIMES = "当前神兽你已经挑战得够多了，休息休息吧!";
        Integer INIT_HP = 150000;
        Integer MAX_TIME = 2;
    }

    interface Sign {
        String MENU_NAME = "签到";
        String SIGN_SUCCESS = "签到成功，获得了[%s]";
        String SIGN_REPEAT = "今天已经签到过了，明天再来吧~";
        String SIGN_NOTHING = "很遗憾，什么都没有得到...";
    }

    interface Manage {
        String COMPENSATE_MONEY = "补偿灵石";
        String COMPENSATE = "补偿";
    }

    interface Dungeon {
        String MENU_TITLE = "时光溯流(副本挑战)";
        String DESCRIBE = "时光之门已开启，请选择要进入的场景";
        String DUNGEON_NAME = "[%s]建议最低等级%s";
        String NO_GROUP = "此场景当前没有还在等待的探索队伍，你可以选择创建队伍等待其他玩家加入，也可以选择返回去其他地方看看";
        String WAIT_TIP = "已找到想要探索当前场景的队伍，你可以选择一个加入，也可以自行创建队伍，组满两人将会自动开始探索";
        String GROUP_FULL = "就在这片刻之间，该队伍已经满了，请返回加入其他队伍吧";
        String PICK_PHANTOM = "请选择你要出战的第%s个幻灵";
        String JOIN_SUCCESS = "操作成功，请稍候再进入该场景查看探索结果领取探索奖励，输入[00]返回主菜单";
        String GROUP_WAIT = "你已经有队伍了，请稍候再来看看，或者你可以退出去加入其他玩家的队伍";
        String GROUP_WAIT_FULL = "你已经有队伍了，即将自动进行探索，请稍候来领取奖励哦";
        String DUNGEON_FINISH = "战斗已结束，奖励已发放" + StrUtil.CRLF;
        String REPEAT = "你今日已挑战过该副本，先歇一歇吧~";
        String CREATE_GROUP = "创建队伍";
        String QUIT_GROUP = "退出队伍";
        String QUIT_FULL = "队伍满员，即将开始探索，不能退出哦";
        String QUIT_SUCCESS = "退出成功，输入[00]返回主菜单";
        String FAIL = "很遗憾!副本挑战失败!"+ StrUtil.CRLF +"获得50灵石已到账，努力提升自己吧，再接再厉!";
        String VICTORY = "副本挑战成功，获得";
        Integer GET_DUNGEON = 30;
        String PHANTOM_NOT = "你的参战幻灵不足2个，进入此地实在不太安全，请先去携带幻灵或去唤灵";
    }

    interface  Money {
        Integer WORLD_BOOS_1 = 20;
        Integer WORLD_BOOS_2 = 50;
        Integer WORLD_BOOS_3 = 100;
        Integer WORLD_BOOS_4 = 200;

        Integer DUNGEON_SUCCESS = 200;
        Integer DUNGEON_FAIL = 50;

        Integer EXPLORE_MIN = 0;
        Integer EXPLORE_MAX = 2;
    }

    interface Shop {
        String MENU_NAME = "忘川鬼市(神秘商店)";
        String DESCRIBE = "当前折扣：%s折(每天凌晨刷新)" + StrUtil.CRLF + "我的灵石：%s";
        String NO_MONEY = "你的灵石不足，无法购买";
        String BUY_SUCCESS = "购买成功，消耗灵石：%s，剩余灵石：%s";
    }

    interface Message {
        String MENU_NAME = "信使";
        String DETAIL_TITLE = "来自%s的消息";
        String FRIEND = "好友[%s]";
        String SYSTEM = "系统";
        String MESSAGE_CONTENT = "%s" + StrUtil.CRLF + "投送时间：%s" + StrUtil.CRLF;
        String GET_ATTACH = "接收附件";
        String ATTACH = "附件："  + StrUtil.CRLF;
        String EMPTY = "暂时没有你的未读信件";
        String READ_TIP = "输入编号查看信件内容" + StrUtil.CRLF + "注意：含有附件的信件读取后需要手动收取附件，否则附件将会消失";
        String ATTACH_REPEAT = "该附件已查收，请勿重复操作哦~";
        String WRITE_MESSAGE_DESCRIBE = "请输入所要发送的内容，你也可以输入[0]返回";
        String WRITE_MESSAGE_MENU = "写信";

        String ASK_NEED_ATTACH = "是否需要继续添加附件，请回答[是]或[否]";
        String YES = "是";
        String NO = "否";
        String NOT_YES_NO = "请输入[是]或[否]";
        String WAIT_ATTACH = "请输入要邮寄的物品与数量，按照格式[唤灵符*10]，物品名称不要输错哦";
        String ERROR_ATTACH = "格式错误，请按照[唤灵符*10]的格式来输入物品与数量";
        String ERROR_GOODS_NAME = "物品不存在，请检查名称是否正确后重新输入";
        String NUMBER_NOT = "你也没这么多了，少寄点吧(请重新输入)";
        String NUMBER_FORMAT_ERROR = "你又调皮了，数量要是数字格式的哦，重新输入吧~";
        String NUMBER_ZERO = "邮寄的物品数量不能为0，请重新输入";
        String NOT_HAVE = "你也没有这件物品哦，请重新输入";
        String HAS_ADD_ATTACH = "该物品在附件中已经有了，不要重复添加同一物品，请重新输入";
        String SEND_SUCCESS = "发送成功!!";
    }
}
