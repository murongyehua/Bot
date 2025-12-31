package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENGameInfo {

    SEVEN_PICK(1,"七连翻", "SEVEN", 2, 12, "SevenPickGamePlay", "【七连翻】游戏规则\n" +
            "游戏牌堆：\n" +
            "基础牌：12张12,11张11,10张10……1张1\n" +
            "记分牌：x2，+2，+4，+6，+8，+10\n" +
            "行动牌：4张【再翻三张】、4张【冻结】、4张【二次机会】 \n" +
            "1. 每局多个轮次，每轮积分，当有玩家达到200分时即为最后一轮，最后一轮结束时如果存在多个玩家分数>=200，则积分最多的玩家获胜；\n" +
            "2. 每轮每个玩家轮流翻牌，每次轮到都可以选择翻一张牌或者结束（如果翻完牌想结束，需等下次轮到自己才能结束）\n" +
            "3. 翻到基础牌即获得对应的分数，但如果翻到已经拥有的基础牌则本轮强行结束，本轮积分清零\n" +
            "4. 翻到记分牌则积累对应的分数，获得x2时仅针对基础牌x2，记分牌不参与计算\n" +
            "5. 当有人本轮翻了七张基础牌还未结束，则本轮强行结束，并额外获得15分\n" +
            "6. 行动牌【再翻三张】：翻到时选择给到任意一个玩家，让他立马再连续翻三张\n" +
            "7. 行动牌【冻结】：翻到时选择给到任意一个玩家，让他立刻结束这一轮次结算积分\n" +
            "8. 行动牌【二次机会】：翻到时默认给自己，如果自己已经拥有该牌则自动给到下家，拥有这张牌时如果翻到同样已拥有的基础牌不会强行结束，而是失去翻到的基础牌和二次机会，能够继续参与游戏\n" +
            "游戏过程中可随时使用【积分】指令查看当前所有玩家的积分情况\n"+
            "游戏过程中可随时使用【牌堆】指令查看当前剩余牌堆详情"),

    BLACKJACK(2,"二十一点", "BLACKJACK", 2, 10, "BlackjackGamePlay", "【二十一点（小林版）】游戏规则\n" +
            "游戏牌堆：\n" +
            "两副完整扑克牌（A-K圴8张），A=1分，2-10=牌面分，J/Q/K=0.5分\n" +
            "功能牌：4张【冻结】、4张【速翻】\n" +
            "─────────────\n" +
            "【游戏流程】\n" +
            "1. 每局4轮，每轮所有人轮流翻牌\n" +
            "2. 每回合可选择【翻牌】或【结束】\n" +
            "3. 选择【结束】保存当前分数作为本轮最终结果\n" +
            "4. 选择【翻牌】抽取一张牌累加分数\n" +
            "   • 超过21分 → 强制结束，本轮得0分\n" +
            "   • 未超过21分 → 继续下一位玩家回合\n" +
            "─────────────\n" +
            "【计分规则】\n" +
            "• 单轮排名：第1名得3分，第2名得2分，第3名得1分\n" +
            "• 同分视为同排名，均可得分\n" +
            "• 4轮结束后总分排名：第1名得5积分，第2名3积分，第3名2积分，其他1积分\n" +
            "─────────────\n" +
            "【特殊规则】\n" +
            "• 五小龙：翻5张牌（功能牌不算）分数≤ 5 → 视为21分且优先级最高\n" +
            "• 功能牌【冻结】：令目标玩家立即结束本轮\n" +
            "• 功能牌【速翻】：令目标玩家立即翻一张牌\n" +
            "• 功能牌可弃用\n" +
            "─────────────\n" +
            "游戏过程中可随时使用【积分】查看各玩家分数\n" +
            "游戏过程中可随时使用【牌堆】查看剩余牌堆详情\n" +
            "游戏过程中可随时使用【退出游戏】退出房间");

    private Integer no;

    private String name;

    private String code;

    private Integer minPeople;

    private Integer maxPeople;

    private String playServiceName;

    private String desc;

    /**
     * 根据编号获取游戏
     */
    public static ENGameInfo getByNo(Integer no) {
        for (ENGameInfo game : values()) {
            if (game.getNo().equals(no)) {
                return game;
            }
        }
        return null;
    }

    /**
     * 根据名称获取游戏
     */
    public static ENGameInfo getByName(String name) {
        for (ENGameInfo game : values()) {
            if (game.getName().equals(name)) {
                return game;
            }
        }
        return null;
    }

    /**
     * 根据Code获取游戏
     */
    public static ENGameInfo getByCode(String code) {
        for (ENGameInfo game : values()) {
            if (game.getCode().equals(code)) {
                return game;
            }
        }
        return null;
    }

}
