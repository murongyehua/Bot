package com.bot.common.constant;

import cn.hutool.core.util.StrUtil;

/**
 * @author murongyehua
 * @version 1.0 2020/9/22
 */
public interface BaseConsts {

    interface ClassBeanName {
        String ANSWER_BOOK = "answerBookServiceImpl";
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
        String GAME = "游戏";
        String ANSWER_BOOK = "答案之书";
        String LUCK_NAME = "运势占卜";
        String SWEET = "情话Boy";
        String MUSIC = "听音乐";

        String GAME_TIP = "目前内置游戏为【山海见闻】，向小林发送游戏名即可进入游戏模式。";


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
        String ILL_CODE = "指令格式有误";
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
        String INVITE_CODE_ERROR = "你的邀请码不正确，或已被使用，请仔细核对后再试哦~";
        String REG_SUCCESS = "恭喜成为小林的新玩伴！截止日期为%s，在此之前小林都会一直在哦~";
        String OVER_TIME_TIP = "你的服务未开通或者已到期，请先进行开通哦~";
        String CONTINUE_TIPS = "您的服务时间即将到期，如果还有需求请及时续期，感谢您的使用。";
        String QUERY_DEADLINE_DATE = "到期时间";
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
        String CHAT = "闲聊";
        String TIP = "无聊的时候，就来找小林聊天吧。";
    }

    interface Picture {
        String SUFFIX = "头像";
        String PICTURE = "头像推荐";
        String TIP = "可以通过发送【头像】【男头】【女头】，来让小林给你推荐头像。";
        String FAIL_QUERY = "哎呀，我也不知道了，你要不换个方式问问";
    }

    interface Constellation {
        String CONSTELLATION = "星座";
        String TIP = "小林可以帮你查询星座运势，快来询问试试吧。";
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

    interface Work {
        String ENTRY = "开始核算";
        String ENTRY_TIPS = "成功进入核算工作模式，从现在起您的所有消息都会被认为是核算信息，当核算信息发送完毕之后请发送【结束】来获取当日核算文件，确认无误后可以发送【提取】来提取当日结果信息。\r\n" +
                "在这个过程中您可以随时发送【退出】来离开核算工作模式，您提交的数据不会因退出而消失。每日0点自动退出核算工作模式并清空提交数据。";
        String FINISH = "结束";
        String FETCH = "提取";
        String EXIT = "退出";
        String EXIT_SUCCESS = "退出成功，感谢您的使用";
        String ERROR = "未检测到工作文件，请退出后重新进入工作模式再尝试~";
        String ILL_INFO = "数据格式不正确，请检查后重试，若未找到问题，请联系我主人~";
        String GET_INFO_SUCCESS = "数据收集成功，请继续发送或发送【结束】提取当前文件，若数据有误，修正后重新发送即可实现覆盖";
        String FILE_NOT_EXIST = "出问题啦，请退出工作模式重新进入尝试，若问题仍然存在，请联系我主人~";
        String EXCEPTION = "出现未知异常，请及时联系我主人";
        String START_DEAL = "处理";
        String START_DEAL_TIP = "好的，现在开始处理数据，处理完成后我会通知您，请耐心等待...";
        String SAVE_DATA_TIP = "收集数据成功，当前已收集%s条数据，如已收集完毕请发送【处理】进行下一步操作。";
        String DEAL_SUCCESS_RESULT = "数据处理完毕，累计处理%s条数据，全部处理成功，请发送【结束】提取当前文件，您也可以继续发送数据处理。";
        String DEAL_FAIL_RESULT = "数据处理完毕，累计处理%s条数据，成功%s条，失败%s条，处理失败的数据将在此条提示发送后单独发送给您，您可以核对后重新发送处理，也可以直接发送【结束】提取当前文件。";
    }

    interface GirlVideo {
        String SUCCESS = "好好好，视频发得慢，请不要一直发，耐心等待哈。(如果很久都没等到，再重新尝试~)";
    }

    interface Dog {
        String NAME = "舔狗日记";
    }

    interface Change {
        String CHANGE = "切换";
        String NO_ENGINE = "未能识别需要切换的引擎，已为您切换至默认聊天引擎。（群聊内会统一切换）";
        String CHANGE_SUCCESS_FORMAT = "已为您切换至%s引擎。（群聊内会统一切换）";
    }

}
