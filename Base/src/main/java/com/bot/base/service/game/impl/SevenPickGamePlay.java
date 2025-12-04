package com.bot.base.service.game.impl;

import com.bot.base.service.game.BaseGamePlay;
import com.bot.common.util.SendMsgUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 七连翻游戏完整实现
 * 注意:不使用@Service注解,由GameRoomManager动态创建实例
 * @author Assistant
 */
@Slf4j
public class SevenPickGamePlay extends BaseGamePlay {

    // ========== 卡牌定义 ==========
    private enum CardType { BASIC, SCORE, ACTION }

    private static class Card {
        CardType type;
        String name;
        int value; // 基础牌的数值,或计分牌的加分值

        Card(CardType type, String name, int value) {
            this.type = type;
            this.name = name;
            this.value = value;
        }
    }

    // ========== 功能牌选择状态 ==========
    private enum PendingEffectType { NONE, FREEZE, RE_DRAW_3 }

    private PendingEffectType pendingEffectType = PendingEffectType.NONE;
    private String pendingOperatorUserId;
    private List<String> pendingTargets = new ArrayList<>();
    
    // 待处理的功能牌队列（用于处理再翻三张过程中触发的功能牌）
    private Deque<PendingAction> pendingActionQueue = new ArrayDeque<>();
    
    private static class PendingAction {
        PendingEffectType type;
        String operatorUserId;
        
        PendingAction(PendingEffectType type, String operatorUserId) {
            this.type = type;
            this.operatorUserId = operatorUserId;
        }
    }

    // ========== 游戏状态 ==========
    private Deque<Card> deck = new ArrayDeque<>();
    private int roundIndex = 1; // 当前轮次(盘数)
    private int currentSeatIndex = 0; // 当前座位索引
    private boolean hasPlayerReached200 = false; // 是否有玩家达到200分
    private String playerReached200 = null; // 达到200分的玩家

    // ========== 玩家总分 ==========
    private Map<String, Integer> totalScore = new HashMap<>();

    // ========== 本轮次玩家状态 ==========
    private Map<String, Boolean> endedThisRound = new HashMap<>(); // 是否已结束本轮
    private Map<String, Integer> roundBaseSum = new HashMap<>(); // 本轮基础牌总分
    private Map<String, Integer> roundExtraSum = new HashMap<>(); // 本轮额外积分
    private Map<String, Boolean> roundHasX2 = new HashMap<>(); // 本轮是否有x2
    private Map<String, Boolean> roundHasSecondChance = new HashMap<>(); // 本轮是否有二次机会
    private Map<String, Set<Integer>> roundOwnedBasic = new HashMap<>(); // 本轮已拥有的基础牌
    private Map<String, List<String>> roundScoreCards = new HashMap<>(); // 本轮拥有的计分牌
    private Map<String, List<String>> roundActionCards = new HashMap<>(); // 本轮拥有的功能牌
    private Map<String, Boolean> frozenThisRound = new HashMap<>(); // 本轮是否被冻结

    public SevenPickGamePlay(String roomCode, String gameCode, String gameName, List<String> playerIds) {
        super(roomCode, gameCode, gameName, playerIds);
    }

    @Override
    protected void initGame() {
        // 随机打乱座位
        Collections.shuffle(playerIds);

        // 初始化玩家状态
        for (String playerId : playerIds) {
            totalScore.put(playerId, 0);
            endedThisRound.put(playerId, false);
            roundBaseSum.put(playerId, 0);
            roundExtraSum.put(playerId, 0);
            roundHasX2.put(playerId, false);
            roundHasSecondChance.put(playerId, false);
            roundOwnedBasic.put(playerId, new HashSet<>());
            roundScoreCards.put(playerId, new ArrayList<>());
            roundActionCards.put(playerId, new ArrayList<>());
            frozenThisRound.put(playerId, false);
        }

        // 初始化并洗牌
        initDeck();
        currentSeatIndex = 0;
        roundIndex = 1;

        log.info("房间[{}]七连翻游戏初始化完成,玩家数:{}", roomCode, playerIds.size());
    }

    /**
     * 初始化牌堆
     */
    private void initDeck() {
        deck.clear();
        List<Card> allCards = new ArrayList<>();

        // 基础牌:12张12,11张11,...,1张1
        for (int i = 1; i <= 12; i++) {
            for (int j = 0; j < i; j++) {
                allCards.add(new Card(CardType.BASIC, String.valueOf(i), i));
            }
        }

        // 计分牌 各四张
        for (int i = 0; i < 4; i++) {
            allCards.add(new Card(CardType.SCORE, "x2", 0));
            allCards.add(new Card(CardType.SCORE, "+2", 2));
            allCards.add(new Card(CardType.SCORE, "+4", 4));
            allCards.add(new Card(CardType.SCORE, "+6", 6));
            allCards.add(new Card(CardType.SCORE, "+8", 8));
            allCards.add(new Card(CardType.SCORE, "+10", 10));
        }

        // 行动牌:各4张
        for (int i = 0; i < 4; i++) {
            allCards.add(new Card(CardType.ACTION, "再翻三张", 0));
            allCards.add(new Card(CardType.ACTION, "冻结", 0));
            allCards.add(new Card(CardType.ACTION, "二次机会", 0));
        }

        // 洗牌
        Collections.shuffle(allCards);
        deck.addAll(allCards);

        log.info("牌堆初始化完成,共{}张牌", deck.size());
    }

    @Override
    protected String getGameStartMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("─────────────\n");
        sb.append("🎲 ").append(gameName).append(" 开始！房间[").append(roomCode).append("]\n");
        sb.append("─────────────\n");
        sb.append("座位顺序:\n");
        for (int i = 0; i < playerIds.size(); i++) {
            String displayName = getPlayerDisplayName(playerIds.get(i));
            sb.append(i + 1).append(". ").append(displayName).append("\n");
        }

        // 向所有群广播游戏开始消息
        sendBroadcastMessage(sb.toString());
        
        // 发送第一个玩家的回合提示
        sendTurnMessage(playerIds.get(0));

        return ""; // 消息已通过广播发送，不需要返回
    }

    @Override
    protected void doEndGame() {
        log.info("房间[{}]七连翻游戏结束", roomCode);
    }

    @Override
    public Map<String, Integer> calculateScores() {
        Map<String, Integer> scores = new HashMap<>();

        // 按总分排序
        List<Map.Entry<String, Integer>> sortedPlayers = totalScore.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        // 根据排名分配小林游戏积分
        for (int i = 0; i < sortedPlayers.size(); i++) {
            String userId = sortedPlayers.get(i).getKey();
            int gameScore;
            switch (i) {
                case 0: gameScore = 10; break; // 第一名
                case 1: gameScore = 5; break;  // 第二名
                case 2: gameScore = 3; break;  // 第三名
                default: gameScore = 1;        // 其他参与者
            }
            scores.put(userId, gameScore);
        }

        return scores;
    }

    /**
     * 生成结算消息
     */
    private String generateSettlementMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("─────────────\n");
        sb.append("🏆 游戏结算 🏆\n");
        sb.append("─────────────\n\n");

        // 按总分排序
        List<Map.Entry<String, Integer>> sortedPlayers = totalScore.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        // 游戏内成绩排名
        sb.append("🎮 游戏成绩:\n");
        String[] rankIcons = {"🥇", "🥈", "🥉"}; // 金银铜牌
        String[] rankNames = {"第一名", "第二名", "第三名", "第四名"};
        
        for (int i = 0; i < sortedPlayers.size(); i++) {
            String rankIcon = i < rankIcons.length ? rankIcons[i] + " " : "   ";
            String rankName = i < rankNames.length ? rankNames[i] : "第" + (i + 1) + "名";
            String userId = sortedPlayers.get(i).getKey();
            String displayName = getPlayerDisplayName(userId);
            int score = sortedPlayers.get(i).getValue();
            
            sb.append(rankIcon).append(rankName).append(": ")
              .append(displayName).append(" - ")
              .append(score).append("分\n");
        }

        // 游戏系统积分奖励
        sb.append("\n⭐ 系统积分奖励:\n");
        
        for (int i = 0; i < sortedPlayers.size(); i++) {
            String userId = sortedPlayers.get(i).getKey();
            String displayName = getPlayerDisplayName(userId);
            int gameScore;
            String rewardDesc;
            
            switch (i) {
                case 0: 
                    gameScore = 10; 
                    rewardDesc = "🎉 冠军奖励";
                    break;
                case 1: 
                    gameScore = 5; 
                    rewardDesc = "🌟 亚军奖励";
                    break;
                case 2: 
                    gameScore = 3; 
                    rewardDesc = "✨ 季军奖励";
                    break;
                default: 
                    gameScore = 1;
                    rewardDesc = "🎁 参与奖励";
            }
            
            sb.append("  • ").append(displayName)
              .append(": +").append(gameScore).append("积分 ")
              .append(rewardDesc).append("\n");
        }
        
        sb.append("\n─────────────\n");
        sb.append("👏 感谢各位玩家的参与！\n");

        return sb.toString();
    }

    @Override
    public String handleInstruction(String userId, String instruction) {
        // 检查游戏是否已结束
        if (gameEnded) {
            return null; // 游戏已结束,不处理任何指令
        }
        
        // 更新最后活动时间
        updateLastActivityTime();

        if (!isPlayer(userId)) {
            return "您不是本局游戏的参与者~";
        }

        instruction = instruction.trim();

        // 处理退出游戏
        if ("退出游戏".equals(instruction)) {
            return handleQuitGame(userId);
        }

        // 处理待处理的功能牌选择
        if (pendingEffectType != PendingEffectType.NONE) {
            if (userId.equals(pendingOperatorUserId)) {
                return handlePendingEffectChoice(userId, instruction);
            } else {
                String operatorName = getPlayerDisplayName(pendingOperatorUserId);
                return "请等待" + operatorName + "选择目标~";
            }
        }

        // 正常回合处理
        if ("翻牌".equals(instruction)) {
            return handleDrawCard(userId);
        } else if ("结束".equals(instruction)) {
            return handleEndTurn(userId);
        } else {
            return "无效指令,请发送【翻牌】或【结束】";
        }
    }

    /**
     * 处理翻牌
     */
    private String handleDrawCard(String userId) {
        // 检查是否轮到该玩家
        String currentPlayer = playerIds.get(currentSeatIndex);
        if (!userId.equals(currentPlayer)) {
            return "还没轮到您哦~";
        }

        // 检查是否已结束本轮
        if (endedThisRound.get(userId)) {
            return "您已结束本轮次,请等待其他玩家~";
        }

        // 检查是否被冻结
        if (frozenThisRound.get(userId)) {
            return "您本轮被冻结,无法继续翻牌~";
        }

        // 检查牌堆
        if (deck.isEmpty()) {
            return "牌堆已空,自动结束本轮~";
        }

        // 翻牌
        Card card = deck.poll();
        return processDrawnCard(userId, card);
    }

    /**
     * 处理退出游戏
     */
    private String handleQuitGame(String userId) {
        String displayName = getPlayerDisplayName(userId);
        String quitMessage = String.format("房间[%s] 游戏[%s]\n\n玩家 %s 退出游戏,游戏终止!\n\n房间已解散,其他玩家可重新创建或加入房间~",
                roomCode, gameName, displayName);
        
        // 广播退出消息
        sendBroadcastMessage(quitMessage);
        
        // 结束游戏(不结算积分)
        gameEnded = true;
        
        // 通知GameRoomManager解散房间
        // 注意:这里不直接调用finishGame,而是返回特殊标记给上层处理
        return "QUIT_GAME:" + userId;
    }

    /**
     * 处理翻到的牌
     */
    private String processDrawnCard(String userId, Card card) {
        StringBuilder message = new StringBuilder();

        // 构建基础消息
        message.append(buildAtMessage(userId));
        message.append("当前总分:").append(totalScore.get(userId)).append("+")
               .append(calculateRoundScore(userId)).append("(全局+本轮)\n");
        message.append("你翻到了\n『").append(card.name).append("』\n");

        boolean needMoveToNext = true;

        switch (card.type) {
            case BASIC:
                needMoveToNext = processBasicCard(userId, card, message);
                break;
            case SCORE:
                processScoreCard(userId, card, message);
                break;
            case ACTION:
                needMoveToNext = processActionCard(userId, card, message);
                break;
        }

        // 显示当前牌型
        message.append(buildCardStatus(userId));

        // 发送消息
        String result = message.toString();
        sendMessageToPlayer(userId, result);

        // 移动到下一个玩家
        if (needMoveToNext && pendingEffectType == PendingEffectType.NONE) {
            moveToNextPlayer();
        }

        return ""; // 消息已发送,不需要返回
    }

    /**
     * 处理基础牌
     * @return true-继续移动到下一个玩家, false-不移动
     */
    private boolean processBasicCard(String userId, Card card, StringBuilder message) {
        Set<Integer> ownedBasic = roundOwnedBasic.get(userId);

        // 检查是否已拥有该牌
        if (ownedBasic.contains(card.value)) {
            // 检查是否有二次机会
            if (roundHasSecondChance.get(userId)) {
                message.append("很遗憾,你已经有这张牌了\n");
                message.append("消耗『二次机会』继续游戏!\n");
                roundHasSecondChance.put(userId, false);
                // 从功能牌列表移除
                roundActionCards.get(userId).remove("二次机会");
                return true; // 继续当前玩家回合
            } else {
                // 强制结束
                message.append("很遗憾，你已经有这张牌了\n");
                message.append("强制结束，本轮次计分为0！\n");
                endPlayerRound(userId, 0);
                return true; // 移动到下一个玩家
            }
        } else {
            // 添加基础牌
            ownedBasic.add(card.value);
            roundBaseSum.put(userId, roundBaseSum.get(userId) + card.value);

            // 检查七连翻（翻了7张基础牌）
            if (checkSevenCards(ownedBasic)) {
                roundExtraSum.put(userId, roundExtraSum.get(userId) + 15);
                int finalScore = calculateRoundScore(userId);
                message.append("wow，你完成了七连翻,所得积分额外+15!!\n");
                message.append("强制结束，本轮次计分为").append(finalScore)
                       .append("，总分为").append(totalScore.get(userId) + finalScore).append("!");
                endPlayerRound(userId, finalScore);
                
                // 向所有群广播七连翻喜讯
                String displayName = getPlayerDisplayName(userId);
                String broadcastMsg = String.format("🎉 喜讯！玩家【%s】完成了七连翻，额外获得15分奖励！\n",
                        displayName, finalScore, totalScore.get(userId) + finalScore);
                sendBroadcastMessage(broadcastMsg);
                
                return true;
            } else {
                message.append("恭喜,此轮安全,成功计分!\n");
                return true; // 继续当前玩家回合
            }
        }
    }

    /**
     * 处理计分牌
     */
    private void processScoreCard(String userId, Card card, StringBuilder message) {
        roundScoreCards.get(userId).add(card.name);

        if ("x2".equals(card.name)) {
            roundHasX2.put(userId, true);
            message.append("获得计分牌『x2』!\n");
        } else {
            roundExtraSum.put(userId, roundExtraSum.get(userId) + card.value);
            message.append("获得额外积分+").append(card.value).append("!\n");
        }
    }

    /**
     * 处理功能牌
     * @return true-移动到下一个玩家, false-等待选择
     */
    private boolean processActionCard(String userId, Card card, StringBuilder message) {
        roundActionCards.get(userId).add(card.name);

        if ("二次机会".equals(card.name)) {
            // 检查是否已有二次机会
            if (roundHasSecondChance.get(userId)) {
                message.append("你已有『二次机会』,此牌顺位转移给下一位玩家...\n");
                // 从当前玩家列表中移除这张卡
                roundActionCards.get(userId).remove("二次机会");
                // 从当前玩家开始查找下一个没有二次机会的玩家
                boolean transferred = false;
                for (int i = 1; i < playerIds.size(); i++) {
                    int nextIndex = (currentSeatIndex + i) % playerIds.size();
                    String nextPlayer = playerIds.get(nextIndex);
                    if (!roundHasSecondChance.get(nextPlayer)) {
                        roundHasSecondChance.put(nextPlayer, true);
                        roundActionCards.get(nextPlayer).add("二次机会");
                        String nextPlayerName = getPlayerDisplayName(nextPlayer);
                        message.append("『二次机会』已转移给").append(nextPlayerName).append("!\n");
                        transferred = true;
                        break;
                    }
                }
                if (!transferred) {
                    message.append("所有玩家都已有『二次机会』,此牌作废!\n");
                }
            } else {
                roundHasSecondChance.put(userId, true);
                message.append("获得『二次机会』!\n");
            }
            return true;
        } else if ("冻结".equals(card.name)) {
            message.append("🎯 请选择目标（发送序号）：\n");
            List<String> targets = buildTargetList(userId, message);
            pendingEffectType = PendingEffectType.FREEZE;
            pendingOperatorUserId = userId;
            pendingTargets = targets;
            return false; // 等待选择
        } else if ("再翻三张".equals(card.name)) {
            message.append("🎯 请选择目标（发送序号）：\n");
            List<String> targets = buildTargetList(userId, message);
            pendingEffectType = PendingEffectType.RE_DRAW_3;
            pendingOperatorUserId = userId;
            pendingTargets = targets;
            return false; // 等待选择
        }

        return true;
    }

    /**
     * 处理功能牌选择
     */
    private String handlePendingEffectChoice(String userId, String choice) {
        try {
            int index = Integer.parseInt(choice) - 1;
            if (index < 0 || index >= pendingTargets.size()) {
                return "序号无效，请输入有效数字序号~";
            }

            String targetUserId = pendingTargets.get(index);
            StringBuilder message = new StringBuilder();

            if (pendingEffectType == PendingEffectType.FREEZE) {
                // 冻结目标玩家 - 按正常结算,不清空得分
                int targetRoundScore = calculateRoundScore(targetUserId);
                frozenThisRound.put(targetUserId, true);
                message.append(buildAtMessage(targetUserId));
                String operatorName = getPlayerDisplayName(userId);
                String targetName = getPlayerDisplayName(targetUserId);
                message.append("很遗憎,你被").append(operatorName).append("冻结了!\n");
                message.append("本轮次强制结束,本轮得分:").append(targetRoundScore).append("!\n");
                message.append(buildCardStatus(targetUserId));
                sendMessageToPlayer(targetUserId, message.toString());

                endPlayerRound(targetUserId, targetRoundScore);

                // 清除待处理状态
                pendingEffectType = PendingEffectType.NONE;
                pendingOperatorUserId = null;
                pendingTargets.clear();
                
                // 检查是否有待处理的功能牌
                if (!pendingActionQueue.isEmpty()) {
                    // 触发下一个功能牌
                    PendingAction nextAction = pendingActionQueue.poll();
                    pendingEffectType = nextAction.type;
                    pendingOperatorUserId = nextAction.operatorUserId;
                    
                    StringBuilder actionMessage = new StringBuilder();
                    actionMessage.append(buildAtMessage(nextAction.operatorUserId));
                    if (nextAction.type == PendingEffectType.FREEZE) {
                        actionMessage.append("你在再翻三张过程中获得了『冻结』!\n");
                    } else if (nextAction.type == PendingEffectType.RE_DRAW_3) {
                        actionMessage.append("你在再翻三张过程中获得了『再翻三张』!\n");
                    }
                    actionMessage.append("请选择使用对象(发序号):\n");
                    List<String> targets = buildTargetList(nextAction.operatorUserId, actionMessage);
                    pendingTargets = targets;
                    
                    sendMessageToPlayer(nextAction.operatorUserId, actionMessage.toString());
                    return ""; // 消息已发送,不需要返回
                }

                // 移动到下一个玩家
                moveToNextPlayer();

                return ""; // 消息已发送,不需要返回

            } else if (pendingEffectType == PendingEffectType.RE_DRAW_3) {
                // 目标玩家连翻三张
                message.append(buildAtMessage(targetUserId));
                String operatorName = getPlayerDisplayName(userId);
                message.append("你被").append(operatorName).append("使用了『再翻三张』!\n\n");

                boolean forceEnded = false;
                List<String> triggeredActions = new ArrayList<>(); // 记录触发的功能牌
                
                for (int i = 0; i < 3; i++) {
                    if (deck.isEmpty()) {
                        message.append("牌堆已空,无法继续翻牌\n");
                        break;
                    }

                    Card card = deck.poll();
                    message.append("第").append(i + 1).append("张:『").append(card.name).append("』");

                    // 处理卡牌效果,检查是否触发强制结束
                    String result = processCardForReDraw3(targetUserId, card, message);
                    if ("FORCE_END".equals(result)) {
                        forceEnded = true;
                        break;
                    } else if ("NEED_FREEZE".equals(result)) {
                        // 触发了冻结,添加到队列
                        triggeredActions.add("冻结");
                        pendingActionQueue.offer(new PendingAction(PendingEffectType.FREEZE, targetUserId));
                    } else if ("NEED_RE_DRAW_3".equals(result)) {
                        // 触发了再翻三张,添加到队列
                        triggeredActions.add("再翻三张");
                        pendingActionQueue.offer(new PendingAction(PendingEffectType.RE_DRAW_3, targetUserId));
                    }
                    message.append("\n");
                }

                message.append("\n").append(buildCardStatus(targetUserId));
                sendMessageToPlayer(targetUserId, message.toString());

                // 清除当前待处理状态
                pendingEffectType = PendingEffectType.NONE;
                pendingOperatorUserId = null;
                pendingTargets.clear();

                // 如果被强制结束,已在processCardForReDraw3中处理
                if (!forceEnded) {
                    // 检查是否有待处理的功能牌
                    if (!pendingActionQueue.isEmpty()) {
                        // 触发第一个功能牌
                        PendingAction nextAction = pendingActionQueue.poll();
                        pendingEffectType = nextAction.type;
                        pendingOperatorUserId = nextAction.operatorUserId;
                        
                        StringBuilder actionMessage = new StringBuilder();
                        actionMessage.append(buildAtMessage(nextAction.operatorUserId));
                        if (nextAction.type == PendingEffectType.FREEZE) {
                            actionMessage.append("你在再翻三张过程中获得了『冻结』!\n");
                        } else if (nextAction.type == PendingEffectType.RE_DRAW_3) {
                            actionMessage.append("你在再翻三张过程中获得了『再翻三张』!\n");
                        }
                        actionMessage.append("请选择使用对象(发序号):\n");
                        List<String> targets = buildTargetList(nextAction.operatorUserId, actionMessage);
                        pendingTargets = targets;
                        
                        sendMessageToPlayer(nextAction.operatorUserId, actionMessage.toString());
                        return ""; // 消息已发送,不需要返回
                    }
                }

                // 移动到下一个玩家
                moveToNextPlayer();

                return ""; // 消息已发送,不需要返回
            }

        } catch (NumberFormatException e) {
            return "请输入有效的数字序号~";
        }

        return "处理失败~";
    }

    /**
     * 处理再翻三张的卡牌(完善版,处理所有逻辑)
     * @return "FORCE_END" 表示强制结束, "CONTINUE" 表示继续
     */
    private String processCardForReDraw3(String userId, Card card, StringBuilder message) {
        switch (card.type) {
            case BASIC:
                Set<Integer> ownedBasic = roundOwnedBasic.get(userId);
                if (ownedBasic.contains(card.value)) {
                    // 翻到重复的基础牌
                    if (roundHasSecondChance.get(userId)) {
                        message.append(" - 重复了,消耗『二次机会』继续!");
                        roundHasSecondChance.put(userId, false);
                        roundActionCards.get(userId).remove("二次机会");
                        return "CONTINUE";
                    } else {
                        message.append(" - 重复了,强制结束,本轮计分为0!");
                        endPlayerRound(userId, 0);
                        return "FORCE_END";
                    }
                } else {
                    ownedBasic.add(card.value);
                    roundBaseSum.put(userId, roundBaseSum.get(userId) + card.value);
                    
                    // 检查七连翻（翻了7张基础牌）
                    if (checkSevenCards(ownedBasic)) {
                        roundExtraSum.put(userId, roundExtraSum.get(userId) + 15);
                        int finalScore = calculateRoundScore(userId);
                        message.append(" - 完成七连翻!+15分,强制结束,本轮得分:").append(finalScore);
                        endPlayerRound(userId, finalScore);
                        
                        // 向所有群广播七连翻喜讯（在再翻三张过程中触发）
                        String displayName = getPlayerDisplayName(userId);
                        String broadcastMsg = String.format("🎉 喜讯！玩家【%s】在再翻三张过程中完成了七连翻，额外获得15分奖励！\n本轮得分：%d分，总分：%d分", 
                                displayName, finalScore, totalScore.get(userId) + finalScore);
                        sendBroadcastMessage(broadcastMsg);
                        
                        return "FORCE_END";
                    } else {
                        message.append(" - 安全!");
                    }
                }
                break;
            case SCORE:
                roundScoreCards.get(userId).add(card.name);
                if ("x2".equals(card.name)) {
                    roundHasX2.put(userId, true);
                    message.append(" - 获得x2!");
                } else {
                    roundExtraSum.put(userId, roundExtraSum.get(userId) + card.value);
                    message.append(" - 获得+").append(card.value).append("分!");
                }
                break;
            case ACTION:
                roundActionCards.get(userId).add(card.name);
                if ("二次机会".equals(card.name)) {
                    // 检查是否已有二次机会
                    if (roundHasSecondChance.get(userId)) {
                        message.append(" - 已有二次机会,顺位转移...");
                        // 从当前玩家列表中移除这张卡
                        roundActionCards.get(userId).remove("二次机会");
                        // 从当前玩家开始查找下一个没有二次机会的玩家
                        boolean transferred = false;
                        int currentIndex = playerIds.indexOf(userId);
                        for (int i = 1; i < playerIds.size(); i++) {
                            int nextIndex = (currentIndex + i) % playerIds.size();
                            String nextPlayer = playerIds.get(nextIndex);
                            if (!roundHasSecondChance.get(nextPlayer)) {
                                roundHasSecondChance.put(nextPlayer, true);
                                roundActionCards.get(nextPlayer).add("二次机会");
                                String nextPlayerName = getPlayerDisplayName(nextPlayer);
                                message.append("已转移给").append(nextPlayerName);
                                transferred = true;
                                break;
                            }
                        }
                        if (!transferred) {
                            message.append("所有人都有,作废");
                        }
                    } else {
                        roundHasSecondChance.put(userId, true);
                        message.append(" - 获得二次机会!");
                    }
                } else if ("冻结".equals(card.name)) {
                    message.append(" - 获得冻结!");
                    // 在再翻三张中抽到冻结,记录待处理状态
                    return "NEED_FREEZE";
                } else if ("再翻三张".equals(card.name)) {
                    message.append(" - 获得再翻三张!");
                    // 在再翻三张中抽到再翻三张,记录待处理状态
                    return "NEED_RE_DRAW_3";
                }
                break;
        }
        return "CONTINUE";
    }

    /**
     * 构建目标列表(包含所有玩家,可以选择自己)
     */
    private List<String> buildTargetList(String operatorUserId, StringBuilder message) {
        List<String> targets = new ArrayList<>();
        List<String> endedPlayers = new ArrayList<>();
        
        // 先收集可选和已结束的玩家
        for (String playerId : playerIds) {
            if (endedThisRound.get(playerId)) {
                endedPlayers.add(playerId);
            } else {
                targets.add(playerId);
            }
        }
        
        // 显示可选玩家(带序号)
        int index = 1;
        for (String playerId : targets) {
            int currentScore = totalScore.get(playerId);
            int roundScore = calculateRoundScore(playerId);
            String displayName = getPlayerDisplayName(playerId);
            
            message.append(index++).append(". ").append(displayName)
                   .append(",当前积分").append(currentScore).append("+")
                   .append(roundScore).append("\n");
        }
        
        // 显示已结束玩家(不带序号,在下方)
        if (!endedPlayers.isEmpty()) {
            message.append("\n已结束:\n");
            for (String playerId : endedPlayers) {
                int currentScore = totalScore.get(playerId);
                int roundScore = calculateRoundScore(playerId);
                String displayName = getPlayerDisplayName(playerId);
                
                message.append("- ").append(displayName)
                       .append(",当前积分").append(currentScore).append(" [已结束]\n");
            }
        }
        
        return targets;
    }

    /**
     * 处理手动结束回合
     */
    private String handleEndTurn(String userId) {
        // 检查是否轮到该玩家
        String currentPlayer = playerIds.get(currentSeatIndex);
        if (!userId.equals(currentPlayer)) {
            return "还没轮到您哦~";
        }

        // 检查是否已结束本轮
        if (endedThisRound.get(userId)) {
            return "您已结束本轮次~";
        }

        // 计算本轮得分
        int roundScore = calculateRoundScore(userId);

        StringBuilder message = new StringBuilder();
        message.append(buildAtMessage(userId));
        message.append("当前总分:").append(totalScore.get(userId)).append("+")
               .append(roundScore).append("(全局+本轮)\n");
        message.append("你手动结束了当前轮次\n");
        message.append("本轮得分:").append(roundScore)
               .append(",总分:").append(totalScore.get(userId) + roundScore).append("\n");
        message.append(buildCardStatus(userId));

        endPlayerRound(userId, roundScore);

        String result = message.toString();
        sendMessageToPlayer(userId, result);

        // 移动到下一个玩家
        moveToNextPlayer();

        return ""; // 消息已发送,不需要返回
    }

    /**
     * 结束玩家本轮
     */
    private void endPlayerRound(String userId, int score) {
        endedThisRound.put(userId, true);
        totalScore.put(userId, totalScore.get(userId) + score);

        // 检查是否达到200分
        if (totalScore.get(userId) >= 200 && !hasPlayerReached200) {
            hasPlayerReached200 = true;
            playerReached200 = userId;
        }
    }

    /**
     * 移动到下一个玩家
     */
    private void moveToNextPlayer() {
        int startIndex = currentSeatIndex;

        do {
            currentSeatIndex = (currentSeatIndex + 1) % playerIds.size();

            // 如果回到起点,说明所有玩家都结束了
            if (currentSeatIndex == startIndex) {
                // 检查是否所有玩家都结束了本轮
                boolean allEnded = true;
                for (Boolean ended : endedThisRound.values()) {
                    if (!ended) {
                        allEnded = false;
                        break;
                    }
                }

                if (allEnded) {
                    // 检查是否有人达到200分
                    if (hasPlayerReached200) {
                        // 游戏结束
                        finishGame();
                        return;
                    } else {
                        // 开始新轮次
                        startNewRound();
                        return;
                    }
                }
            }

            String nextPlayer = playerIds.get(currentSeatIndex);
            if (!endedThisRound.get(nextPlayer) && !frozenThisRound.get(nextPlayer)) {
                // 发送回合提示
                sendTurnMessage(nextPlayer);
                return;
            }

        } while (true);
    }

    /**
     * 开始新轮次
     */
    private void startNewRound() {
        roundIndex++;

        // 重置本轮状态
        for (String playerId : playerIds) {
            endedThisRound.put(playerId, false);
            roundBaseSum.put(playerId, 0);
            roundExtraSum.put(playerId, 0);
            roundHasX2.put(playerId, false);
            roundHasSecondChance.put(playerId, false);
            roundOwnedBasic.put(playerId, new HashSet<>());
            roundScoreCards.put(playerId, new ArrayList<>());
            roundActionCards.put(playerId, new ArrayList<>());
            frozenThisRound.put(playerId, false);
        }

        // 重新洗牌
        initDeck();

        // 从座位0开始
        currentSeatIndex = 0;

        // 发送新轮次开始消息(包含所有玩家分数)
        StringBuilder message = new StringBuilder();
        message.append("第").append(roundIndex).append("轮次开始!\n\n");
        message.append("当前积分榜:\n");
        
        // 按分数排序显示
        List<Map.Entry<String, Integer>> sortedPlayers = totalScore.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());
        
        for (int i = 0; i < sortedPlayers.size(); i++) {
            String playerId = sortedPlayers.get(i).getKey();
            String displayName = getPlayerDisplayName(playerId);
            Integer score = sortedPlayers.get(i).getValue();
            message.append(i + 1).append(". ").append(displayName)
                   .append(": ").append(score).append("分\n");
        }
        
        sendBroadcastMessage(message.toString());

        // 发送第一个玩家的回合提示
        sendTurnMessage(playerIds.get(0));
    }

    /**
     * 游戏结束内部逻辑
     */
    private void finishGame() {
        gameEnded = true;
        String settlement = generateSettlementMessage();
        sendBroadcastMessage(settlement);
        
        // 通知GameRoomManager结算积分并解散房间
        // 注意:这里不直接调用,而是通过设置标志位,由GameRoomManager定时检查
        log.info("房间[{}]游戏自然结束,等待结算和解散", roomCode);
    }

    /**
     * 发送回合提示消息
     */
    private void sendTurnMessage(String userId) {
        StringBuilder message = new StringBuilder();
        message.append(buildAtMessage(userId));
        message.append("当前总分:").append(totalScore.get(userId)).append("+")
               .append(calculateRoundScore(userId)).append("(全局+本轮)\n");
        message.append("🎯 轮到你啦！发送【翻牌】或【结束】\n");

        // 检查是否有人达到200分
        if (hasPlayerReached200 && !userId.equals(playerReached200)) {
            String reachedPlayerName = getPlayerDisplayName(playerReached200);
            message.append("\n请注意!").append(reachedPlayerName)
                   .append("已拿到").append(totalScore.get(playerReached200))
                   .append("分!\n本轮次结束后将进行结算!\n");
        }

        message.append("\n").append(buildCardStatus(userId));

        sendMessageToPlayer(userId, message.toString());
    }

    /**
     * 计算本轮得分
     */
    private int calculateRoundScore(String userId) {
        int baseSum = roundBaseSum.get(userId);
        int extraSum = roundExtraSum.get(userId);
        boolean hasX2 = roundHasX2.get(userId);

        if (hasX2) {
            return baseSum * 2 + extraSum;
        } else {
            return baseSum + extraSum;
        }
    }

    /**
     * 检查是否翻了7张基础牌（不要求连续）
     */
    private boolean checkSevenCards(Set<Integer> ownedBasic) {
        return ownedBasic.size() >= 7;
    }

    /**
     * 检查七连翻（保留旧方法，不再使用）
     * @deprecated 根据游戏规则，应检查是否翻了7张牌而不是7张连续牌，请使用 checkSevenCards
     */
    @Deprecated
    private boolean checkSevenInRow(Set<Integer> ownedBasic) {
        if (ownedBasic.size() < 7) {
            return false;
        }

        List<Integer> sorted = new ArrayList<>(ownedBasic);
        Collections.sort(sorted);

        for (int i = 0; i <= sorted.size() - 7; i++) {
            boolean isSevenInRow = true;
            for (int j = 0; j < 6; j++) {
                if (sorted.get(i + j + 1) != sorted.get(i + j) + 1) {
                    isSevenInRow = false;
                    break;
                }
            }
            if (isSevenInRow) {
                return true;
            }
        }

        return false;
    }

    /**
     * 构建牌型状态
     */
    private String buildCardStatus(String userId) {
        StringBuilder sb = new StringBuilder("\n当前牌型\n");

        // 基础牌
        Set<Integer> basic = roundOwnedBasic.get(userId);
        if (!basic.isEmpty()) {
            sb.append("基础:");
            List<Integer> sortedBasic = new ArrayList<>(basic);
            Collections.sort(sortedBasic);
            for (Integer value : sortedBasic) {
                sb.append("『").append(value).append("』");
            }
            sb.append("\n");
        }

        // 计分牌
        List<String> scoreCards = roundScoreCards.get(userId);
        if (!scoreCards.isEmpty()) {
            sb.append("计分:");
            for (String card : scoreCards) {
                sb.append("『").append(card).append("』");
            }
            sb.append("\n");
        }

        // 功能牌
        List<String> actionCards = roundActionCards.get(userId);
        if (!actionCards.isEmpty()) {
            sb.append("功能:");
            for (String card : actionCards) {
                if (!"冻结".equals(card) && !"再翻三张".equals(card)) {
                    sb.append("『").append(card).append("』");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 获取玩家显示名称
     */
    private String getPlayerDisplayName(String userId) {
        if (participationMap == null) {
            return userId;
        }
        String groupId = participationMap.get(userId);
        if (groupId != null && !groupId.trim().isEmpty()) {
            // 群聊参与,获取群昵称
            String nickName = SendMsgUtil.getGroupNickName(groupId, userId);
            return nickName != null && !nickName.trim().isEmpty() ? nickName : userId;
        } else {
            // 私聊参与,直接返回userId
            return userId;
        }
    }

    /**
     * 构建@消息
     */
    private String buildAtMessage(String userId) {
        return "";
    }

    /**
     * 发送消息给玩家
     */
    private void sendMessageToPlayer(String userId, String message) {
        if (participationMap == null) {
            SendMsgUtil.sendMsg(userId, message);
            return;
        }
        String groupId = participationMap.get(userId);
        if (groupId != null && !groupId.trim().isEmpty()) {
            // 群聊参与
            SendMsgUtil.sendGroupMsgForGame(groupId, message, userId);
        } else {
            // 私聊参与
            SendMsgUtil.sendMsg(userId, message);
        }
    }

    /**
     * 广播消息给所有玩家
     */
    private void sendBroadcastMessage(String message) {
        Map<String, List<String>> groupPlayers = new HashMap<>();
        List<String> privatePlayers = new ArrayList<>();

        // 分组
        for (String playerId : playerIds) {
            String groupId = participationMap != null ? participationMap.get(playerId) : null;
            if (groupId != null && !groupId.trim().isEmpty()) {
                groupPlayers.computeIfAbsent(groupId, k -> new ArrayList<>()).add(playerId);
            } else {
                privatePlayers.add(playerId);
            }
        }

        // 群聊发送(每个群只发一次)
        for (Map.Entry<String, List<String>> entry : groupPlayers.entrySet()) {
            SendMsgUtil.sendGroupMsgForGame(entry.getKey(), message, "");
        }

        // 私聊发送
        for (String playerId : privatePlayers) {
            SendMsgUtil.sendMsg(playerId, message);
        }
    }
}
