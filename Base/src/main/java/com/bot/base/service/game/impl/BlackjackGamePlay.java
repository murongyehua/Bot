package com.bot.base.service.game.impl;

import com.bot.base.service.game.BaseGamePlay;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 二十一点游戏完整实现
 * 注意:不使用@Service注解,由GameRoomManager动态创建实例
 * @author Assistant
 */
@Slf4j
public class BlackjackGamePlay extends BaseGamePlay {

    // ========== 卡牌定义 ==========
    private enum CardType { NORMAL, ACTION }

    private static class Card {
        CardType type;
        String name;      // 卡牌名称: A, 2-10, J, Q, K, 冻结, 速翻
        double value;     // 分值

        Card(CardType type, String name, double value) {
            this.type = type;
            this.name = name;
            this.value = value;
        }
    }

    // ========== 游戏状态 ==========
    private LinkedList<Card> deck = new LinkedList<>();      // 牌堆
    private int currentSeatIndex = 0;                         // 当前座位索引
    private int roundIndex = 1;                               // 当前轮次(1-4)
    private static final int TOTAL_ROUNDS = 4;                // 总轮次数

    // ========== 玩家状态 ==========
    // 本轮状态
    private Map<String, Double> roundScore = new HashMap<>();           // 本轮分数
    private Map<String, Boolean> roundEnded = new HashMap<>();          // 本轮是否结束
    private Map<String, Integer> roundCardCount = new HashMap<>();      // 本轮翻的普通牌数量
    private Map<String, Boolean> roundFiveSmall = new HashMap<>();      // 本轮是否达成五小龙
    private Map<String, List<String>> roundCards = new HashMap<>();     // 本轮翻到的牌

    // 总分统计
    private Map<String, Integer> totalScore = new HashMap<>();          // 总得分(4轮累计)

    // ========== 功能牌处理 ==========
    private enum PendingActionType { NONE, FREEZE, SPEED_FLIP }
    private PendingActionType pendingActionType = PendingActionType.NONE;
    private String pendingOperatorUserId;
    private List<String> pendingTargets = new ArrayList<>();
    private ScheduledFuture<?> actionTimeoutFuture;

    // ========== 超时控制 ==========
    private boolean turnHandled = false;
    private ScheduledFuture<?> turnTimeoutFuture;

    /**
     * 构造函数
     */
    public BlackjackGamePlay(String roomCode, String gameCode, String gameName, List<String> playerIds) {
        super(roomCode, gameCode, gameName, playerIds);
    }

    @Override
    protected void initGame() {
        // 随机打乱座位
        Collections.shuffle(playerIds);

        // 初始化玩家状态
        for (String playerId : playerIds) {
            totalScore.put(playerId, 0);
            resetPlayerRoundState(playerId);
        }

        // 初始化并洗牌
        initDeck();
        currentSeatIndex = 0;
        roundIndex = 1;

        log.info("房间[{}]二十一点游戏初始化完成,玩家数:{}", roomCode, playerIds.size());
    }

    /**
     * 重置玩家单轮状态
     */
    private void resetPlayerRoundState(String playerId) {
        roundScore.put(playerId, 0.0);
        roundEnded.put(playerId, false);
        roundCardCount.put(playerId, 0);
        roundFiveSmall.put(playerId, false);
        roundCards.put(playerId, new ArrayList<>());
    }

    /**
     * 初始化牌堆
     */
    private void initDeck() {
        deck.clear();
        List<Card> allCards = new ArrayList<>();

        // 两副完整扑克牌(A-K各8张)
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        for (String rank : ranks) {
            double value;
            if (rank.equals("A")) {
                value = 1.0;
            } else if (rank.equals("J") || rank.equals("Q") || rank.equals("K")) {
                value = 0.5;
            } else {
                value = Double.parseDouble(rank);
            }

            // 每种牌8张
            for (int i = 0; i < 8; i++) {
                allCards.add(new Card(CardType.NORMAL, rank, value));
            }
        }

        // 功能牌
        for (int i = 0; i < 4; i++) {
            allCards.add(new Card(CardType.ACTION, "冻结", 0));
            allCards.add(new Card(CardType.ACTION, "速翻", 0));
        }

        // 洗牌
        Collections.shuffle(allCards);
        deck.addAll(allCards);

        log.info("房间[{}]牌堆初始化完成，共{}张牌", roomCode, deck.size());
    }

    @Override
    protected String getGameStartMessage() {
        StringBuilder message = new StringBuilder();
        message.append("════════════════\n");
        message.append("🎰 二十一点游戏开始！\n");
        message.append("════════════════\n\n");
        message.append("📋 游戏信息\n");
        message.append("• 总轮数：4轮\n");
        message.append("• 玩家数：").append(playerIds.size()).append("人\n\n");
        
        message.append("🎯 座位顺序\n");
        for (int i = 0; i < playerIds.size(); i++) {
            String displayName = getPlayerDisplayName(playerIds.get(i));
            message.append((i + 1)).append(". ").append(displayName).append("\n");
        }
        
        message.append("\n════════════════\n");
        message.append("第1轮开始！\n");
        
        sendBroadcastMessage(message.toString());

        // 发送第一个玩家的回合提示
        sendTurnMessage(playerIds.get(0));
        
        return null; // 消息已通过广播发送
    }

    @Override
    protected void doEndGame() {
        cancelTurnTimeout();
        cancelActionTimeout();
    }

    @Override
    public Map<String, Integer> calculateScores() {
        // 返回最终积分(已在游戏结束时计算好)
        return totalScore;
    }

    @Override
    public String handleInstruction(String userId, String instruction) {
        updateLastActivityTime();
        instruction = instruction.trim();

        // 检查是否是游戏参与者
        if (!isPlayer(userId)) {
            return null;
        }

        // 退出游戏
        if ("退出游戏".equals(instruction)) {
            return handleQuitGame(userId);
        }

        // 查询指令
        if ("积分".equals(instruction)) {
            return showScoreBoard();
        }
        if ("牌堆".equals(instruction)) {
            return showDeckInfo();
        }

        // 功能牌选择处理
        if (pendingActionType != PendingActionType.NONE && userId.equals(pendingOperatorUserId)) {
            return handleActionCardChoice(userId, instruction);
        }

        // 游戏指令
        if ("翻牌".equals(instruction)) {
            return handleDrawCard(userId);
        }
        if ("结束".equals(instruction)) {
            return handleEndTurn(userId);
        }

        // 非游戏指令，返回空字符串静默处理，不触发兜底聊天
        return "";
    }

    /**
     * 处理翻牌
     */
    private String handleDrawCard(String userId) {
        // 防止并发重复翻牌：检查是否已处理过本回合
        if (turnHandled) {
            return "";  // 静默处理
        }
        
        // 检查是否轮到该玩家
        String currentPlayer = playerIds.get(currentSeatIndex);
        if (!userId.equals(currentPlayer)) {
            return "";  // 静默处理，不提示
        }
        
        // 取消超时并立即标记为已处理，防止并发
        cancelTurnTimeout();
        turnHandled = true;

        // 检查牌堆
        if (deck.isEmpty()) {
            return "牌堆已空，本轮自动结束~";
        }

        // 翻牌
        Card card = deck.poll();
        processDrawnCard(userId, card);
        return "";  // 消息已发送，返回空字符串
    }

    /**
     * 处理翻到的牌
     */
    private void processDrawnCard(String userId, Card card) {
        StringBuilder message = new StringBuilder();
        message.append(buildAtMessage(userId));

        if (card.type == CardType.NORMAL) {
            // 普通牌
            roundCards.get(userId).add(card.name);
            roundScore.put(userId, roundScore.get(userId) + card.value);
            roundCardCount.put(userId, roundCardCount.get(userId) + 1);

            double currentScore = roundScore.get(userId);
            int cardCount = roundCardCount.get(userId);

            message.append("翻到：『").append(card.name).append("』 +").append(formatScore(card.value)).append("分\n");
            message.append("─────────────\n");
            message.append("💰 本轮分数：").append(formatScore(currentScore)).append("\n");
            message.append("🎴 已翻牌数：").append(cardCount).append("张\n");

            // 检查是否爆牌
            if (currentScore > 21) {
                message.append("\n💥 爆牌了！分数超过21，本轮得0分！\n");
                endPlayerRound(userId, 0.0);
                sendMessageToPlayer(userId, message.toString());
                proceedToNextPlayer();
                return;
            }

            // 检查五小龙（五张牌分数不超过5）
            if (cardCount >= 5 && currentScore <= 5) {
                message.append("\n🐉 恭喜达成【五小龙】！视为21分且优先级最高！\n");
                roundFiveSmall.put(userId, true);
                endPlayerRound(userId, 21.0);
                sendMessageToPlayer(userId, message.toString());
                
                // 向所有群广播
                String displayName = getPlayerDisplayName(userId);
                sendBroadcastMessage("🎉 玩家【" + displayName + "】达成【五小龙】！");
                
                proceedToNextPlayer();
                return;
            }

            // 翻完一张牌后自动进入下一位玩家回合
            sendMessageToPlayer(userId, message.toString());
            proceedToNextPlayer();

        } else {
            // 功能牌
            message.append("翻到功能牌：『").append(card.name).append("』\n");
            sendMessageToPlayer(userId, message.toString());
            
            // 处理功能牌
            handleActionCard(userId, card);
        }
    }

    /**
     * 处理功能牌
     */
    private void handleActionCard(String userId, Card card) {
        List<String> targets = buildActionTargetList(userId);
        
        if (targets.isEmpty()) {
            // 没有可用目标，功能牌作废
            sendMessageToPlayer(userId, "当前没有可用目标，功能牌作废。");
            proceedToNextPlayer();
            return;
        }

        // 设置待处理状态
        if ("冻结".equals(card.name)) {
            pendingActionType = PendingActionType.FREEZE;
        } else {
            pendingActionType = PendingActionType.SPEED_FLIP;
        }
        pendingOperatorUserId = userId;
        pendingTargets = targets;

        // 发送选择提示
        StringBuilder message = new StringBuilder();
        message.append("请选择对象（发送序号或【弃用】）：\n\n");
        for (int i = 0; i < targets.size(); i++) {
            String targetId = targets.get(i);
            String displayName = getPlayerDisplayName(targetId);
            double score = roundScore.get(targetId);
            message.append(i + 1).append(". ").append(displayName)
                   .append(" - ").append(formatScore(score)).append("分\n");
        }
        message.append("\n⏱ 25秒内未选择将自动弃用");
        
        sendMessageToPlayer(userId, message.toString());

        // 启动选择超时
        scheduleActionTimeout(userId);
    }

    /**
     * 构建功能牌可用目标列表
     */
    private List<String> buildActionTargetList(String operatorUserId) {
        List<String> targets = new ArrayList<>();
        for (String playerId : playerIds) {
            if (!roundEnded.get(playerId)) {
                targets.add(playerId);
            }
        }
        return targets;
    }

    /**
     * 处理功能牌选择
     */
    private String handleActionCardChoice(String userId, String choice) {
        cancelActionTimeout();

        if ("弃用".equals(choice)) {
            sendMessageToPlayer(userId, "已弃用功能牌。");
            resetActionState();
            proceedToNextPlayer();
            return "";
        }

        // 解析序号
        try {
            int index = Integer.parseInt(choice) - 1;
            if (index < 0 || index >= pendingTargets.size()) {
                return "序号无效，请重新选择（1-" + pendingTargets.size() + "）或【弃用】";
            }

            String targetUserId = pendingTargets.get(index);
            String operatorName = getPlayerDisplayName(userId);
            String targetName = getPlayerDisplayName(targetUserId);
            PendingActionType actionType = pendingActionType;

            // 执行功能牌效果
            if (actionType == PendingActionType.FREEZE) {
                // 冻结：令目标立即结束本轮
                double targetScore = roundScore.get(targetUserId);
                endPlayerRound(targetUserId, targetScore);
                
                String notification = String.format("🧊【%s】对【%s】使用了『冻结』，强制结束本轮，得%.1f分",
                        operatorName, targetName, targetScore);
                sendBroadcastMessage(notification);
                
            } else {
                // 速翻：令目标立即翻一张牌
                if (deck.isEmpty()) {
                    sendMessageToPlayer(userId, "牌堆已空，功能牌作废。");
                    resetActionState();
                    proceedToNextPlayer();
                    return null;
                }
                
                Card drawnCard = deck.poll();
                String notification = String.format("⚡【%s】对【%s】使用了『速翻』",
                        operatorName, targetName);
                sendBroadcastMessage(notification);
                
                // 目标玩家翻牌
                processSpeedFlipCard(targetUserId, drawnCard);
            }

            // 重置功能牌状态，继续下一位玩家
            resetActionState();
            proceedToNextPlayer();
            return "";

        } catch (NumberFormatException e) {
            return "请发送数字序号（1-" + pendingTargets.size() + "）或【弃用】";
        }
    }

    /**
     * 处理速翻效果抽到的牌
     */
    private void processSpeedFlipCard(String userId, Card card) {
        StringBuilder message = new StringBuilder();
        message.append(buildAtMessage(userId));
        message.append("『速翻』效果：翻到『").append(card.name).append("』");

        if (card.type == CardType.NORMAL) {
            roundCards.get(userId).add(card.name);
            roundScore.put(userId, roundScore.get(userId) + card.value);
            roundCardCount.put(userId, roundCardCount.get(userId) + 1);

            double currentScore = roundScore.get(userId);
            int cardCount = roundCardCount.get(userId);

            message.append(" +").append(formatScore(card.value)).append("分\n");
            message.append("💰 本轮分数：").append(formatScore(currentScore)).append("\n");

            // 检查爆牌
            if (currentScore > 21) {
                message.append("💥 爆牌了！本轮得0分！");
                sendMessageToPlayer(userId, message.toString());
                endPlayerRound(userId, 0.0);
                return;
            }

            // 检查五小龙（五张牌分数不超过5）
            if (cardCount >= 5 && currentScore <= 5) {
                message.append("🐉 达成【五小龙】！");
                sendMessageToPlayer(userId, message.toString());
                roundFiveSmall.put(userId, true);
                endPlayerRound(userId, 21.0);
                return;
            }

            sendMessageToPlayer(userId, message.toString());
            
        } else {
            // 速翻抽到功能牌，作废
            message.append("，功能牌作废。");
            sendMessageToPlayer(userId, message.toString());
        }
    }

    /**
     * 重置功能牌状态
     */
    private void resetActionState() {
        pendingActionType = PendingActionType.NONE;
        pendingOperatorUserId = null;
        pendingTargets.clear();
    }

    /**
     * 处理结束回合
     */
    private String handleEndTurn(String userId) {
        // 防止并发重复操作
        if (turnHandled) {
            return "";  // 静默处理
        }
        
        // 检查是否轮到该玩家
        String currentPlayer = playerIds.get(currentSeatIndex);
        if (!userId.equals(currentPlayer)) {
            return "";  // 静默处理，不提示
        }
        
        // 检查是否已结束本轮
        if (roundEnded.get(userId)) {
            return "";  // 静默处理
        }
        
        // 取消超时并标记已处理
        cancelTurnTimeout();
        turnHandled = true;

        double finalScore = roundScore.get(userId);
        endPlayerRound(userId, finalScore);
        
        String message = String.format("您选择结束，本轮得%s分", formatScore(finalScore));
        sendMessageToPlayer(userId, message);
        
        proceedToNextPlayer();
        return "";  // 消息已发送
    }

    /**
     * 处理退出游戏
     */
    private String handleQuitGame(String userId) {
        String displayName = getPlayerDisplayName(userId);
        
        // 从玩家列表中移除
        int quitIndex = playerIds.indexOf(userId);
        if (quitIndex == -1) {
            return "你不在游戏中~";
        }
        
        // 将玩家标记为已结束
        roundEnded.put(userId, true);
        
        // 移除玩家
        playerIds.remove(quitIndex);
        
        // 广播退出消息
        String quitMessage = String.format("房间[%s] 游戏[%s]\n\n玩家 %s 退出游戏！", 
                roomCode, gameName, displayName);
        sendBroadcastMessage(quitMessage);
        
        // 检查剩余玩家数量
        if (playerIds.size() < 2) {
            // 玩家不足，结束游戏，不结算积分
            String endMessage = "\n剩余玩家不足，游戏结束！";
            sendBroadcastMessage(endMessage);
            gameEnded = true;
            cancelTurnTimeout();
            cancelActionTimeout();
            return "QUIT_GAME:" + userId;
        }
        
        // 调整当前座位索引
        if (quitIndex <= currentSeatIndex && currentSeatIndex > 0) {
            currentSeatIndex--;
        }
        if (currentSeatIndex >= playerIds.size()) {
            currentSeatIndex = 0;
        }
        
        // 游戏继续
        String continueMessage = "\n游戏继续！";
        sendBroadcastMessage(continueMessage);
        
        // 检查是否所有人都结束了
        boolean allEnded = playerIds.stream().allMatch(id -> roundEnded.get(id));
        if (allEnded) {
            // 本轮结束，结算
            settleRound();
        } else {
            // 发送下一个玩家的回合消息
            String nextPlayer = playerIds.get(currentSeatIndex);
            if (!roundEnded.get(nextPlayer)) {
                sendTurnMessage(nextPlayer);
            } else {
                proceedToNextPlayer();
            }
        }
        
        return "玩家已退出，游戏继续。";
    }

    /**
     * 结束玩家本轮
     */
    private void endPlayerRound(String userId, double score) {
        roundEnded.put(userId, true);
        roundScore.put(userId, score);
    }

    /**
     * 进入下一位玩家回合
     */
    private void proceedToNextPlayer() {
        // 检查是否所有人都结束了本轮
        boolean allEnded = playerIds.stream().allMatch(id -> roundEnded.get(id));
        
        if (allEnded) {
            // 本轮结束，结算
            settleRound();
            return;
        }

        // 找下一位未结束的玩家
        do {
            currentSeatIndex = (currentSeatIndex + 1) % playerIds.size();
            String nextPlayer = playerIds.get(currentSeatIndex);
            
            if (!roundEnded.get(nextPlayer)) {
                sendTurnMessage(nextPlayer);
                return;
            }
        } while (true);
    }

    /**
     * 结算单轮
     */
    private void settleRound() {
        StringBuilder message = new StringBuilder();
        message.append("════════════════\n");
        message.append("📊 第").append(roundIndex).append("轮结算\n");
        message.append("════════════════\n\n");

        // 按分数排序（五小龙优先）
        List<Map.Entry<String, Double>> sortedPlayers = roundScore.entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    boolean isFiveSmall1 = roundFiveSmall.get(e1.getKey());
                    boolean isFiveSmall2 = roundFiveSmall.get(e2.getKey());
                    
                    if (isFiveSmall1 && !isFiveSmall2) return -1;
                    if (!isFiveSmall1 && isFiveSmall2) return 1;
                    
                    return e2.getValue().compareTo(e1.getValue());
                })
                .collect(Collectors.toList());

        // 显示本轮分数
        for (int i = 0; i < sortedPlayers.size(); i++) {
            String playerId = sortedPlayers.get(i).getKey();
            String displayName = getPlayerDisplayName(playerId);
            double score = sortedPlayers.get(i).getValue();
            boolean isFiveSmall = roundFiveSmall.get(playerId);
            
            message.append(i + 1).append(". ").append(displayName).append(": ")
                   .append(formatScore(score)).append("分");
            if (isFiveSmall) {
                message.append(" 🐉");
            }
            message.append("\n");
        }

        // 计算并分配得分
        message.append("\n💎 本轮得分\n");
        Map<Double, List<String>> scoreGroups = new HashMap<>();
        for (Map.Entry<String, Double> entry : sortedPlayers) {
            scoreGroups.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        int[] rankScores = {3, 2, 1}; // 第1,2,3名得分
        int rankIndex = 0;
        int position = 0;

        for (Map.Entry<String, Double> entry : sortedPlayers) {
            String playerId = entry.getKey();
            double score = entry.getValue();
            
            // 跳过0分玩家
            if (score == 0) {
                continue;
            }
            
            // 同分玩家获得相同排名分数
            List<String> sameScorePlayers = scoreGroups.get(score);
            int earnedScore = (rankIndex < rankScores.length) ? rankScores[rankIndex] : 0;
            
            for (String pid : sameScorePlayers) {
                totalScore.put(pid, totalScore.get(pid) + earnedScore);
                String displayName = getPlayerDisplayName(pid);
                message.append("• ").append(displayName).append(" +").append(earnedScore).append("分\n");
            }
            
            // 移除已处理的同分玩家
            scoreGroups.remove(score);
            rankIndex++;
            position += sameScorePlayers.size();
            
            if (position >= 3) break; // 只显示前3名
        }

        message.append("\n📈 总分排行\n");
        List<Map.Entry<String, Integer>> totalSorted = totalScore.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());
        
        for (int i = 0; i < totalSorted.size(); i++) {
            String displayName = getPlayerDisplayName(totalSorted.get(i).getKey());
            int score = totalSorted.get(i).getValue();
            message.append(i + 1).append(". ").append(displayName).append(": ").append(score).append("分\n");
        }

        sendBroadcastMessage(message.toString());

        // 检查是否游戏结束
        if (roundIndex >= TOTAL_ROUNDS) {
            finishGame();
        } else {
            startNextRound();
        }
    }

    /**
     * 开始下一轮
     */
    private void startNextRound() {
        roundIndex++;

        // 重置本轮状态
        for (String playerId : playerIds) {
            resetPlayerRoundState(playerId);
        }

        // 重新洗牌
        initDeck();
        currentSeatIndex = 0;

        String message = String.format("════════════════\n第%d轮开始！\n════════════════", roundIndex);
        sendBroadcastMessage(message);

        // 发送第一个玩家的回合提示
        sendTurnMessage(playerIds.get(0));
    }

    /**
     * 游戏结束
     */
    private void finishGame() {
        gameEnded = true;
        cancelTurnTimeout();
        cancelActionTimeout();

        StringBuilder message = new StringBuilder();
        message.append("════════════════\n");
        message.append("🏆 游戏结束！最终结算\n");
        message.append("════════════════\n\n");

        // 按总分排序
        List<Map.Entry<String, Integer>> finalRanking = totalScore.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        message.append("📊 最终排名\n");
        for (int i = 0; i < finalRanking.size(); i++) {
            String displayName = getPlayerDisplayName(finalRanking.get(i).getKey());
            int score = finalRanking.get(i).getValue();
            message.append(i + 1).append(". ").append(displayName).append(": ").append(score).append("分\n");
        }

        // 计算积分奖励
        int[] finalRewards = {5, 3, 2}; // 第1,2,3名积分
        Map<Integer, List<String>> scoreGroups = new HashMap<>();
        for (Map.Entry<String, Integer> entry : finalRanking) {
            scoreGroups.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        message.append("\n💰 积分奖励\n");
        int rewardIndex = 0;
        int position = 0;

        for (Map.Entry<String, Integer> entry : finalRanking) {
            int score = entry.getValue();
            List<String> samePlayers = scoreGroups.get(score);
            
            if (samePlayers != null) {
                int reward = (rewardIndex < finalRewards.length) ? finalRewards[rewardIndex] : 1;
                
                for (String pid : samePlayers) {
                    String displayName = getPlayerDisplayName(pid);
                    message.append("• ").append(displayName).append(" +").append(reward).append("积分\n");
                    // 更新最终积分（用于结算）
                    totalScore.put(pid, reward);
                }
                
                scoreGroups.remove(score);
                rewardIndex++;
                position += samePlayers.size();
                
                if (position >= 3) {
                    // 剩余玩家都是参与奖
                    for (Map.Entry<String, Integer> remaining : finalRanking) {
                        if (scoreGroups.containsKey(remaining.getValue())) {
                            for (String pid : scoreGroups.get(remaining.getValue())) {
                                String displayName = getPlayerDisplayName(pid);
                                message.append("• ").append(displayName).append(" +1积分（参与奖）\n");
                                totalScore.put(pid, 1);
                            }
                            scoreGroups.remove(remaining.getValue());
                        }
                    }
                    break;
                }
            }
        }

        message.append("\n感谢参与，期待下次对决！");
        sendBroadcastMessage(message.toString());
    }

    /**
     * 查看积分榜
     */
    private String showScoreBoard() {
        StringBuilder sb = new StringBuilder();
        sb.append("════════════════\n");
        sb.append("📊 当前积分情况\n");
        sb.append("════════════════\n");
        sb.append("第").append(roundIndex).append("轮 / 共").append(TOTAL_ROUNDS).append("轮\n\n");

        // 按总分排序
        List<Map.Entry<String, Integer>> sorted = totalScore.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            String playerId = sorted.get(i).getKey();
            String displayName = getPlayerDisplayName(playerId);
            int score = sorted.get(i).getValue();
            double roundScoreValue = roundScore.get(playerId);
            boolean ended = roundEnded.get(playerId);

            sb.append(i + 1).append(". ").append(displayName).append("\n");
            sb.append("   总分:").append(score).append(" | 本轮:");
            if (ended) {
                sb.append(formatScore(roundScoreValue)).append("(已结束)");
            } else {
                sb.append(formatScore(roundScoreValue));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 查看牌堆信息
     */
    private String showDeckInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("════════════════\n");
        sb.append("🎴 牌堆信息\n");
        sb.append("════════════════\n");

        // 统计剩余牌
        Map<String, Integer> cardCount = new HashMap<>();
        for (Card card : deck) {
            cardCount.put(card.name, cardCount.getOrDefault(card.name, 0) + 1);
        }

        // 普通牌
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        sb.append("\n普通牌：\n");
        for (String rank : ranks) {
            if (cardCount.containsKey(rank)) {
                sb.append("『").append(rank).append("』 x").append(cardCount.get(rank)).append("张  ");
            }
        }

        // 功能牌
        sb.append("\n\n功能牌：\n");
        if (cardCount.containsKey("冻结")) {
            sb.append("『冻结』 x").append(cardCount.get("冻结")).append("张  ");
        }
        if (cardCount.containsKey("速翻")) {
            sb.append("『速翻』 x").append(cardCount.get("速翻")).append("张");
        }

        sb.append("\n\n总计剩余：").append(deck.size()).append("张");
        return sb.toString();
    }

    /**
     * 发送回合提示
     */
    private void sendTurnMessage(String userId) {
        StringBuilder message = new StringBuilder();
        message.append(buildAtMessage(userId));
        
        double currentScore = roundScore.get(userId);
        int cardCount = roundCardCount.get(userId);
        
        message.append("💰 本轮分数：").append(formatScore(currentScore)).append("\n");
        message.append("🎴 已翻牌数：").append(cardCount).append("张\n");
        message.append("─────────────\n");
        message.append("🎯 轮到你啦！发送【翻牌】或【结束】\n");
        message.append("⏱ 25秒内未操作将自动翻牌\n");

        if (!roundCards.get(userId).isEmpty()) {
            message.append("\n已翻的牌：");
            message.append(String.join(" ", roundCards.get(userId)));
        }

        sendMessageToPlayer(userId, message.toString());
        scheduleTurnTimeout(userId);
    }

    /**
     * 启动回合超时
     */
    private void scheduleTurnTimeout(String userId) {
        cancelTurnTimeout();
        turnHandled = false;
        
        turnTimeoutFuture = ThreadPoolManager.schedule(() -> {
            try {
                if (gameEnded) return;
                String currentPlayer = playerIds.get(currentSeatIndex);
                if (!userId.equals(currentPlayer)) return;
                if (roundEnded.get(userId)) return;
                if (turnHandled) return;

                sendMessageToPlayer(userId, "【系统提示】超过25秒未操作，系统已自动为你翻牌。");
                handleDrawCard(userId);
            } catch (Exception ignored) {
            }
        }, 25, TimeUnit.SECONDS);
    }

    private void cancelTurnTimeout() {
        if (turnTimeoutFuture != null && !turnTimeoutFuture.isDone()) {
            try { turnTimeoutFuture.cancel(false); } catch (Exception ignored) {}
        }
    }

    /**
     * 启动功能牌选择超时
     */
    private void scheduleActionTimeout(String userId) {
        cancelActionTimeout();
        
        actionTimeoutFuture = ThreadPoolManager.schedule(() -> {
            try {
                if (gameEnded) return;
                if (pendingActionType == PendingActionType.NONE) return;
                if (!userId.equals(pendingOperatorUserId)) return;

                sendMessageToPlayer(userId, "【系统提示】超过25秒未选择，功能牌已自动弃用。");
                resetActionState();
                
                if (!roundEnded.get(userId)) {
                    sendTurnMessage(userId);
                } else {
                    proceedToNextPlayer();
                }
            } catch (Exception ignored) {
            }
        }, 25, TimeUnit.SECONDS);
    }

    private void cancelActionTimeout() {
        if (actionTimeoutFuture != null && !actionTimeoutFuture.isDone()) {
            try { actionTimeoutFuture.cancel(false); } catch (Exception ignored) {}
        }
    }

    /**
     * 格式化分数显示
     */
    private String formatScore(double score) {
        if (score == (int) score) {
            return String.valueOf((int) score);
        }
        return String.format("%.1f", score);
    }

    /**
     * 获取玩家显示名称（带词条）
     */
    private String getPlayerDisplayName(String userId) {
        if (participationMap == null) {
            return userId;
        }
        String groupId = participationMap.get(userId);
        String nickName;
        if (groupId != null && !groupId.trim().isEmpty()) {
            nickName = SendMsgUtil.getGroupNickName(groupId, userId);
            nickName = nickName != null && !nickName.trim().isEmpty() ? nickName : userId;
        } else {
            nickName = userId;
        }

        // 带上佩戴的词条
        if (SystemConfigCache.userWordMap != null && SystemConfigCache.userWordMap.containsKey(userId)) {
            String word = SystemConfigCache.userWordMap.get(userId);
            if (word != null && !word.trim().isEmpty()) {
                return nickName + "「" + word + "」";
            }
        }

        return nickName;
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
            SendMsgUtil.sendGroupMsgForGame(groupId, message, userId);
        } else {
            SendMsgUtil.sendMsg(userId, message);
        }
    }

    /**
     * 广播消息给所有玩家
     */
    private void sendBroadcastMessage(String message) {
        Map<String, List<String>> groupPlayers = new HashMap<>();
        List<String> privatePlayers = new ArrayList<>();

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
