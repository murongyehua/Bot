package com.bot.common.constant;

import cn.hutool.core.util.StrUtil;

/**
 * @author murongyehua
 * @version 1.0 2020/9/22
 */
public interface BaseConsts {

    interface ClassBeanName {
        String WEATHER = "weatherServiceImpl";
        String ANSWER_BOOK = "answerBookServiceImpl";
    }

    interface Weather {
        String CITY_NAME = "city";
        String KEY_NAME = "appkey";
        String QUERY_STATUS = "ok";

        String KEYWORD_NOW = "现在";
        String KEYWORD_TODAY = "今天";
        String KEYWORD_TOMORROW = "明天";
        String KEYWORD_AFTER_TOMORROW = "后天";
        String SUGGEST = "建议";

        String LAST_UPDATE_TIME = "最后更新时间: ";
        String DAY_COND = "白天: ";
        String NIGHT_COND = "夜晚: ";
        String DEG = "温度: ";
        String WIND = "风力: ";
        String DATE = "日期: ";
        String NOW_TEXT = "现在外面[%s],温度[%s],[%s]";

        String AIR = "空气: ";
        String COMF = "舒适: ";
        String CW = "洗车: ";
        String DRSG = "穿衣: ";
        String FLU = "流感: ";
        String SPORT = "运动: ";
        String TRAV = "旅游: ";
        String UV = "日晒: ";



        String DESCRIBE = "你可以发送"+ StrUtil.CRLF +
                "【武汉今天天气怎么样】" + StrUtil.CRLF +
                "【武汉明天天气怎么样】" + StrUtil.CRLF +
                "【武汉后天天气怎么样】" + StrUtil.CRLF +
                "【武汉天气建议】" + StrUtil.CRLF +
                "等类似指令来获取天气相关的信息" + StrUtil.CRLF +
                "不过，我只能给你提供最多近3天的天气信息";

        String FAIL_QUERY = "哎呀，我也不知道了，你要不换个方式问问";
    }

    interface Menu {
        String ONE = "1";
        String TWO = "2";
        String THREE = "3";
        String FOUR = "4";
        String FIVE = "5";
        String SIX = "6";
        String SEVEN = "7";
        String EIGHT = "8";
        String NINE  = "9";
        String TEN = "10";
        String ZERO = "0";
        String DOUBLE_ZERO = "00";

        String MAIN_MENU_NAME = "菜单";
        String WEATHER_MENU_NAME = "天气";
        String ANSWER_BOOK = "答案之书";
        String LUCK_NAME = "运势占卜";
        String SWEET = "情话Boy";
        String MUSIC = "听音乐";

        String TURN_BACK = "返回";
        String TURN_BACK_MAIN = "返回主菜单";
        String MENU_TIP = "回复选项前的数字，即可查看更多";
        String VERSION_TIP = "[小林当前版本]: [%s]";
    }

    interface Distributor {
        String SPLIT_REG = "=";
        String AND_REG = "&";
    }

    interface AnswerBook {
        String DESCRIBE = "为了不与正常聊天冲突，现在只有包含【请问】才会触发答案之书的回答，如：" + StrUtil.CRLF +
                "【请问我应该吃包子吗？】" + StrUtil.CRLF +
                "【请问为什么我这么好看呢？】";
    }

    interface Luck {
        String TEXT_COMMON = "今日运势: ";
        String TEXT_BEST = "极好";
        String TEXT_GOOD = "较好";
        String TEXT_RIGHT = "不错";
        String TEXT_NORMAL = "一般";
        String TEXT_BAD = "较差";
        String TEXT_TERRIBLE = "极差";

        String DESCRIBE = "你可以通过【占卜】、【运势】等关键字来触发运势占卜" + StrUtil.CRLF
                + "此功能纯属娱乐，不可作为任何行为、事件的凭证";
    }

    interface SystemManager {
        String TRY_INTO_MANAGER_SUCCESS = "请输入管理员密码：";
        String TRY_INTO_MANAGER_FAIL = "当前有其他人处于管理模式中，进入管理模式【失败】";
        String TRY_INTO_MANAGER_INFO = "进入管理模式";
        String TRY_OUT_MANAGER_INFO = "退出管理模式";
        String TRY_INTO_MANAGER_REPEAT = "当前已处于管理模式";
        String MANAGER_PASSWORD_ERROR = "密码错误，自动退出管理模式";
        String MANAGER_PASSWORD_RIGHT = "密码正确，成功进入管理模式";

        String MANAGER_CODE_RELOAD_TEXT = "重载文本";
        String CLEAR_VOTE = "清空投票";
        String LOOK_VOTE = "投票结果";
        String GAME_MANAGER = "游戏";
        String UN_KNOW_MANAGER_CODE = "未知管理指令";
        String SUCCESS = "操作成功";
        String MANAGE_OUT_TIME = "超过1分钟未发现有效操作，已自动退出管理模式";
        String GAME = "山海见闻";
        String JOIN_GAME_WARN = "即将进入游戏模式" + StrUtil.CRLF
                + "一旦进入后，您所有的消息都会默认作为游戏指令" + StrUtil.CRLF
                + "只有当您发送【退出】时才会退出并回到正常的聊天模式" + StrUtil.CRLF
                + "回复数字【1】进入，数字【0】返回";
        String EXIT_GAME = "退出";
        String GET_TOKEN = "我的专属token";
        String CREATE_INVITE_CODE = "生成邀请码";
        String TEMP_REG_PREFIX = "#试用";
        String REG_PREFIX = "#开通";
        String REPEAT_TEMP_REG_TIP = "你已经用过小林了，不能再参加试用哦~";
        String INVITE_CODE_ERROR = "你的邀请码不对，请仔细核对后再试哦~";
        String REG_SUCCESS = "恭喜成为小林的新玩伴！截止日期为%s，在此之前小林都会一直在哦~";
        String OVER_TIME_TIP = "你的服务未开通或者已到期，请联系我的主人进行开通哦~";
    }

    interface Sweet {
        String DESCRIBE = "发送的内容中包含【情话】，即可让小林化身情话Boy";
    }

    interface Music {
        String ERROR = "现在嗓子不太舒服，过会儿再唱吧";
        String TIP = "希望你能喜欢！";
        String[] SORT = {"热歌榜", "新歌榜", "飙升榜", "抖音榜", "电音榜"};
        String DESCRIBE = "想听音乐又不知道听什么？随时来找小林听歌吧！" + StrUtil.CRLF +"我会随机从网易云音乐的排行榜上给你发一首歌哦~";
    }

    interface Chat {
        String ILL_REX_1 = "src";
        String ILL_REX_2 = "（";
        String ILL_REX_3 = "）";
    }

    interface Picture {
        String SUFFIX = "头像";
    }

    interface Constellation {
        String ERROR = "你的问法有问题，换个方式问问吧~";
        String TODAY_FORMAT = "%s" + StrUtil.CRLF + "%s" + StrUtil.CRLF +
                "亲近星座：%s" + StrUtil.CRLF +
                "幸运色：%s" + StrUtil.CRLF +
                "健康：%s" + StrUtil.CRLF +
                "爱情：%s" + StrUtil.CRLF +
                "工作：%s" + StrUtil.CRLF +
                "金钱：%s" + StrUtil.CRLF +
                "综合运势：%s" + StrUtil.CRLF +
                "幸运数字：%s" + StrUtil.CRLF +
                "综述：%s";
    }

}
