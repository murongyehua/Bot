package com.bot.common.constant;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

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
        String MANAGE_OUT_TIME = "未发现有效操作，已自动退出管理模式";
        String GAME = "山海见闻";
        String JOIN_GAME_WARN = "即将进入游戏模式" + StrUtil.CRLF
                + "一旦进入后，您所有的消息都会默认作为游戏指令" + StrUtil.CRLF
                + "只有当您发送【退出】时才会退出并回到正常的聊天模式" + StrUtil.CRLF
                + "回复数字【1】进入，数字【0】返回";
        String EXIT_GAME = "退出";
        String GET_TOKEN = "我的专属token";
        String CREATE_INVITE_CODE = "生成邀请码";
        String SEND_DAILY = "发送日报";
        String TEMP_REG_PREFIX = "#试用";
        String REG_PREFIX = "#开通";
        String USER_CREATE_INVITE_CODE = "生成邀请码";
        String REPEAT_TEMP_REG_TIP = "你已经用过小林了，不能再参加试用哦~";
        String INVITE_CODE_ERROR = "你的邀请码不正确，或已被使用，请仔细核对后再试哦~";
        String REG_SUCCESS = "恭喜成为小林的新玩伴！截止日期为%s，在此之前小林都会一直在哦~";
        String OVER_TIME_TIP = "你的服务未开通或者已到期，请先兑换并使用资格哦~";
        String CONTINUE_TIPS = "您的服务时间即将到期，如果还有需求请及时续期，感谢您的使用。";
        String QUERY_DEADLINE_DATE = "到期时间";
        String SEND_NOTICE_FORMAT = "公告";
        String PIC_SEND_NOTICE_FORMAT = "图片公告";
        String PUSH_FRIEND_FORMAT = "发朋友圈";
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
        String SUCCESS = "正在发送视频，视频发得慢，不要一直尝试，耐心等待即可。";
    }

    interface Dog {
        String NAME = "舔狗日记";
    }

    interface Change {
        String CHANGE = "切换";
        String NO_ENGINE = "未能识别需要切换的引擎，已为您切换至默认聊天引擎。（群聊内会统一切换）";
        String CHANGE_SUCCESS_FORMAT = "现在不需要切换引擎即可正常使用小林的聊天能力~";
    }

    interface Morning {
        String MORNING = "日报";
        String PICK = "订阅日报";
        String CANCEL = "取消订阅日报";
        String PICK_SUCCESS = "订阅成功，每天8:30-9:00推送早报，14:00-14:30推送午报，18:00-18:30推送晚报，可以通过发送【取消订阅日报】来暂停推送，" +
                "默认订阅全天，共三次推送。如果您只想接收部分时段的新闻，可以发送【日报】+【早/午/晚】来进行更改，支持选择多个，比如发送【日报早晚】就可以去掉午报的推送。";
        String MORNING_FORMAT = "★小林日报" + StrUtil.CRLF +
                "%s好!" + StrUtil.CRLF +
                "今天是%s月%s日 星期%s" + StrUtil.CRLF +
                "历史上的今天%s" + StrUtil.CRLF +
                "以下是当前热点：" + StrUtil.CRLF +
                "%s" + StrUtil.CRLF + StrUtil.CRLF +
                "%s";
        String CANCEL_SUCCESS = "好的，我将暂停日报推送，若再有需要可以发送【订阅日报】恢复。";
        String CHANGE_SUCCESS_FORMAT = "设置成功日报为%s（若之前未订阅日报，此时已自动为您订阅）";
        String CHANGE_FAIL = "未找到目标类型，设置日报失败";
    }

    interface WorkDaily {
        String ACTIVE_WORK_DAILY = "激活打工日历";
        String DAILY_ACTIVE_SUCCESS = "激活成功，如果需要关闭，请发送【关闭打工日历】";
        String CLOSE_WORK_DAILY = "关闭打工日历";
        String DAILY_CLOSE_SUCCESS = "关闭成功，如果需要激活，请发送【激活打工日历】";
        String ILL_CONTENT = "指令有误，请检查";
    }

    interface Activity {
        String ACTIVITY_JX3 = "剑三";
        String ACTIVITY_BIND = "绑定";
        String ACTIVITY_GET_AWARD = "抽奖";
        String ACTIVITY_MY_AWARD = "中奖";
        String ACTIVITY_ALL_AWARD = "中奖汇总";
        String ACTIVITY_START = "抽奖开始";
        String ACTIVITY_FINISH = "抽奖结束";
        String UN_KNOW = "未知指令，请检查指令内容和格式是否正确";
        String REPEAT = "您当轮抽奖次数已达最大值，请不要重复操作。";
        String TODAY_DAILY = "今天日常";
        String TOMORROW_DAILY = "明天日常";
        String TOMORROW_TOMORROW_DAILY = "后天日常";
        String OPEN_SERVER = "开服";
        String BIND_SERVER = "绑定区服";
        String DAILY_RETURN_FORMAT = "【%s】" + StrUtil.CRLF +
                "日期：%s" + StrUtil.CRLF +
                "大战：%s" + StrUtil.CRLF +
                "战场：%s" + StrUtil.CRLF +
                "阵营任务：%s" + StrUtil.CRLF +
                "门派事件：%s" + StrUtil.CRLF +
                "驰援任务：%s" + StrUtil.CRLF +
                "美人画像：%s" + StrUtil.CRLF +
                "福缘宠物：%s，%s，%s" + StrUtil.CRLF +
                "武林通鉴·公共任务：%s" + StrUtil.CRLF +
                "武林通鉴·秘境任务：%s" + StrUtil.CRLF +
                "武林通鉴·团队秘境：%s";
        String DAILY_RETURN_FORMAT_WITHOUT_DRAW = "【%s】" + StrUtil.CRLF +
                "日期：%s" + StrUtil.CRLF +
                "大战：%s" + StrUtil.CRLF +
                "战场：%s" + StrUtil.CRLF +
                "阵营任务：%s" + StrUtil.CRLF +
                "门派事件：%s" + StrUtil.CRLF +
                "驰援任务：%s" + StrUtil.CRLF +
                "福缘宠物：%s，%s，%s" + StrUtil.CRLF +
                "武林通鉴·公共任务：%s" + StrUtil.CRLF +
                "武林通鉴·秘境任务：%s" + StrUtil.CRLF +
                "武林通鉴·团队秘境：%s";
        List<String> QQ_SHOW = new ArrayList<String>(){{add("qq秀");add("QQ秀");add("名片秀");}};
        String RANDOM_SHOW = "随机秀";
        String MONEY_PRICE = "金价\r\n更新时间：%s\r\n" +
                "服务器：%s\r\n" +
                "万宝楼：%s\r\n" +
                "贴吧：%s\r\n" +
                "dd373：%s\r\n" +
                "uu898：%s\r\n";
        String MONEY = "金价";
        String NEWS = "资讯";
        String NOTICE = "公告";
        String BATTLE = "战绩";
        String TEAM_CD = "副本";
        String ATTRIBUTE = "属性";
        String ALL_SHOWS = "全部名片";
        String OPEN_SERVER_NOTICE = "开服提醒";
    }

    interface Drink {
        String ACTIVE = "开启喝水记录";
        String CLOSE = "关闭喝水记录";
        String RECORD = "喝水";
        String ACTIVE_SUCCESS = "开启成功。";
        String CLOSE_SUCCESS = "关闭成功。";
        String RECORD_SUCCESS = "记录成功。";
        String ACTIVE_TIP = "当前会话未开启该功能，请先开启喝水记录。（私聊和群聊不共享开关）";
        String QUERY_RECORD = "%s 喝了%sml";
        String NO_DATA_TIP = "你今天还未记录喝水，赶紧喝一口记录下来吧！";
        String QUERY_ALL = "今天共喝水%s次，累计摄入%sL水量。";
        String ALL_TITLE = "【喝水提醒】";
    }

    interface Sign {
        String SIGN_TIP = "签到成功！\r\n连续签到%s天，碎玉 + %s";
        String SIGN_FAIL = "你今天已经签到过了~";
    }

}
