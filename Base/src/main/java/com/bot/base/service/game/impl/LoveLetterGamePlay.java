package com.bot.base.service.game.impl;

import com.bot.base.service.game.BaseGamePlay;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import com.bot.game.dao.entity.BotGameUserScore;
import com.bot.game.dao.entity.BotGameUserScoreExample;
import com.bot.game.dao.mapper.BotGameUserScoreMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 情书游戏实现 - 私聊玩法
 * 注意:不使用@Service注解,由GameRoomManager动态创建实例
 * @author Assistant
 */
@Slf4j
public class LoveLetterGamePlay extends BaseGamePlay {

    private BotGameUserScoreMapper gameUserScoreMapper;

    // ========== 卡牌定义 ==========
    private static class Card {
        int number;      // 数字1-8
        String name;     // 卡牌名称
        String desc;     // 效果描述

        Card(int number, String name, String desc) {
            this.number = number;
            this.name = name;
            this.desc = desc;
        }
    }

    // ========== 玩家状态 ==========
    private static class PlayerState {
        String userId;
        boolean alive;             // 是否存活
        Card handCard;             // 当前手牌
        boolean isProtected;       // 是否受侍女保护（本回合）
        List<Card> discardedCards; // 已弃牌堆

        PlayerState(String userId) {
            this.userId = userId;
            this.alive = true;
            this.isProtected = false;
            this.discardedCards = new ArrayList<>();
        }
    }

    // ========== 游戏状态 ==========
    private LinkedList<Card> deck = new LinkedList<>();
    private List<Card> removedCards = new ArrayList<>();
    private Map<String, PlayerState> playerStates = new HashMap<>();
    private Map<String, Integer> loveMarks = new HashMap<>();
    private int currentRound = 1;
    private int currentSeatIndex = 0;
    
    // ========== 待处理操作 ==========
    private enum PendingActionType {
        NONE, PLAY_CARD, SELECT_TARGET, GUESS_CARD
    }
    
    private PendingActionType pendingActionType = PendingActionType.NONE;
    private String pendingOperatorUserId;
    private Card pendingCard;
    private List<String> pendingTargets;
    private ScheduledFuture<?> actionTimeoutFuture;
    private boolean actionHandled = false;

    public LoveLetterGamePlay(String roomCode, String gameCode, String gameName, List<String> playerIds) {
        super(roomCode, gameCode, gameName, playerIds);
    }

    public void setGameUserScoreMapper(BotGameUserScoreMapper gameUserScoreMapper) {
        this.gameUserScoreMapper = gameUserScoreMapper;
    }

    @Override
    protected void initGame() {
        Collections.shuffle(playerIds);
        for (String playerId : playerIds) {
            loveMarks.put(playerId, 0);
        }
        startNewRound();
        log.info("房间[{}]情书游戏初始化完成,玩家数:{}", roomCode, playerIds.size());
    }

    private void startNewRound() {
        initDeck();
        playerStates.clear();
        for (String playerId : playerIds) {
            playerStates.put(playerId, new PlayerState(playerId));
        }
        removedCards.clear();
        if (playerIds.size() == 2) {
            for (int i = 0; i < 3; i++) {
                removedCards.add(deck.poll());
            }
        } else {
            removedCards.add(deck.poll());
        }
        for (String playerId : playerIds) {
            PlayerState state = playerStates.get(playerId);
            state.handCard = deck.poll();
        }
        currentSeatIndex = 0;
        log.info("房间[{}]第{}局开始", roomCode, currentRound);
    }

    private void initDeck() {
        deck.clear();
        List<Card> allCards = new ArrayList<>();
        for (int i = 0; i < 5; i++) allCards.add(new Card(1, "守卫", "猜测对手手牌（不能猜守卫）"));
        for (int i = 0; i < 2; i++) allCards.add(new Card(2, "祭司", "查看对手手牌"));
        for (int i = 0; i < 2; i++) allCards.add(new Card(3, "男爵", "比较手牌，小者出局"));
        for (int i = 0; i < 2; i++) allCards.add(new Card(4, "侍女", "下回合前免疫效果"));
        for (int i = 0; i < 2; i++) allCards.add(new Card(5, "王子", "令对手弃牌重抽"));
        allCards.add(new Card(6, "国王", "交换手牌"));
        allCards.add(new Card(7, "女伯爵", "持有国王或王子时必须弃"));
        allCards.add(new Card(8, "公主", "打出或弃掉则出局"));
        Collections.shuffle(allCards);
        deck.addAll(allCards);
        log.info("牌堆初始化完成,共{}张牌", deck.size());
    }

    @Override
    protected String getGameStartMessage() {
        StringBuilder message = new StringBuilder();
        message.append("════════════════\n💌 情书游戏开始！\n════════════════\n\n");
        message.append("📋 游戏信息\n• 玩家数：").append(playerIds.size()).append("人\n");
        message.append("• 胜利条件：先集齐5个钟情标记\n• 参与方式：私聊互动\n\n🎯 座位顺序\n");
        for (int i = 0; i < playerIds.size(); i++) {
            String displayName = getPlayerDisplayName(playerIds.get(i));
            int marks = loveMarks.get(playerIds.get(i));
            message.append((i + 1)).append(". ").append(displayName).append(" [").append(marks).append("💕]\n");
        }
        message.append("\n════════════════\n第1局开始！移除").append(removedCards.size()).append("张牌\n");
        sendBroadcastMessage(message.toString());
        for (String playerId : playerIds) {
            PlayerState state = playerStates.get(playerId);
            sendInitialHandCard(playerId, state.handCard);
        }
        sendPlayerTurnMessage(playerIds.get(0));
        return null;
    }

    private void sendInitialHandCard(String userId, Card card) {
        StringBuilder message = new StringBuilder();
        message.append("═══ 你的起始手牌 ═══\n");
        message.append(formatCard(card));
        sendMessageToPlayer(userId, message.toString());
    }

    private void sendPlayerTurnMessage(String userId) {
        if (deck.isEmpty()) {
            settleRound();
            return;
        }
        PlayerState state = playerStates.get(userId);
        if (!state.alive) {
            moveToNextPlayer();
            return;
        }
        Card drawnCard = deck.poll();
        StringBuilder message = new StringBuilder();
        message.append("─────────────\n🎴 轮到你了！\n─────────────\n\n📥 你摸到了：\n");
        message.append(formatCard(drawnCard));
        message.append("\n📋 你的手牌：\n1️⃣ ").append(formatCard(state.handCard));
        message.append("2️⃣ ").append(formatCard(drawnCard)).append("\n");
        boolean mustPlayCountess = checkMustPlayCountess(state.handCard, drawnCard);
        if (mustPlayCountess) {
            message.append("⚠️ 你持有女伯爵和国王/王子，必须打出女伯爵！\n\n请回复：【打 序号】\n例如：【打 1】");
        } else {
            message.append("请选择操作：\n• 【打 1】- 打出第1张牌\n• 【打 2】- 打出第2张牌\n• 【丢弃 1】或【丢弃 2】- 不触发效果直接丢弃");
        }
        sendMessageToPlayer(userId, message.toString());
        pendingActionType = PendingActionType.PLAY_CARD;
        pendingOperatorUserId = userId;
        pendingCard = drawnCard;
        scheduleActionTimeout();
    }

    private boolean checkMustPlayCountess(Card card1, Card card2) {
        boolean hasCountess = card1.number == 7 || card2.number == 7;
        boolean hasKingOrPrince = (card1.number == 5 || card1.number == 6) || (card2.number == 5 || card2.number == 6);
        return hasCountess && hasKingOrPrince;
    }

    private String formatCard(Card card) {
        return String.format("『%d-%s』%s\n", card.number, card.name, card.desc);
    }

    @Override
    protected void doEndGame() {
        cancelActionTimeout();
    }

    @Override
    public Map<String, Integer> calculateScores() {
        Map<String, Integer> scores = new HashMap<>();
        int maxMarks = loveMarks.values().stream().max(Integer::compareTo).orElse(0);
        for (String playerId : playerIds) {
            int marks = loveMarks.get(playerId);
            scores.put(playerId, (marks >= 5 || marks == maxMarks) ? 6 : 1);
        }
        return scores;
    }

    @Override
    public String handleInstruction(String userId, String instruction) {
        updateLastActivityTime();
        if (!isPlayer(userId)) return null;
        instruction = instruction.trim();
        if ("退出游戏".equals(instruction)) return handleQuitGame(userId);
        if ("钟情标记".equals(instruction) || "标记".equals(instruction)) return showLoveMarks();
        if (pendingActionType != PendingActionType.NONE && userId.equals(pendingOperatorUserId)) {
            return handlePendingAction(userId, instruction);
        }
        return "";
    }

    private String handlePendingAction(String userId, String instruction) {
        // 防止并发：原子地检查并设置 actionHandled 标志
        synchronized (this) {
            if (actionHandled) {
                // 已经被处理过了（可能是超时线程处理的）
                return "操作已处理，请勿重复提交。";
            }
            actionHandled = true; // 先设置为true，防止超时线程同时执行
        }
        
        cancelActionTimeout();
        
        String result = "";
        if (pendingActionType == PendingActionType.PLAY_CARD) {
            result = handlePlayCardChoice(userId, instruction);
        } else if (pendingActionType == PendingActionType.SELECT_TARGET) {
            result = handleTargetChoice(userId, instruction);
        } else if (pendingActionType == PendingActionType.GUESS_CARD) {
            result = handleGuessChoice(userId, instruction);
        }
        
        // 如果返回的是错误提示（非空字符串），说明验证失败，允许重试
        if (result != null && !result.isEmpty()) {
            // 重置标志，允许用户重新输入
            actionHandled = false;
            // 重新启动超时计时器
            scheduleActionTimeout();
        }
        
        return result;
    }

    private String handlePlayCardChoice(String userId, String instruction) {
        PlayerState state = playerStates.get(userId);
        Card handCard = state.handCard;
        Card drawnCard = pendingCard;
        boolean discard = instruction.startsWith("丢弃");
        String choiceStr = instruction.replaceAll("[^0-9]", "").trim();
        if (choiceStr.isEmpty()) return "请输入有效的序号（1或2）";
        int choice;
        try {
            choice = Integer.parseInt(choiceStr);
        } catch (NumberFormatException e) {
            return "请输入有效的序号（1或2）";
        }
        if (choice != 1 && choice != 2) return "请输入1或2";
        
        Card playedCard = (choice == 1) ? handCard : drawnCard;
        Card keptCard = (choice == 1) ? drawnCard : handCard;
        
        // 检查女伯爵规则：如果持有女伯爵和国王/王子，必须打出女伯爵
        boolean mustPlayCountess = checkMustPlayCountess(handCard, drawnCard);
        if (mustPlayCountess && playedCard.number != 7) {
            return "⚠️ 你持有女伯爵和国王/王子，必须打出女伯爵！";
        }
        
        state.handCard = keptCard;
        state.discardedCards.add(playedCard);
        // 注意：不在这里清除保护，侍女的保护应该持续到下回合开始前
        String displayName = getPlayerDisplayName(userId);
        String notification = String.format("🎴 %s 打出了『%d-%s』", displayName, playedCard.number, playedCard.name);
        sendBroadcastMessage(notification);
        if (discard) {
            sendMessageToPlayer(userId, "你选择丢弃此牌，不触发效果。");
            resetPendingAction();
            moveToNextPlayer();
            return "";
        }
        executeCardEffect(userId, playedCard);
        return "";
    }

    private void executeCardEffect(String userId, Card card) {
        switch (card.number) {
            case 1: executeGuard(userId); break;
            case 2: executePriest(userId); break;
            case 3: executeBaron(userId); break;
            case 4: executeMaid(userId); break;
            case 5: executePrince(userId); break;
            case 6: executeKing(userId); break;
            case 7: executeCountess(userId); break;
            case 8: executePrincess(userId); break;
        }
    }

    private void executeGuard(String userId) {
        List<String> targets = getValidTargets(userId);
        if (targets.isEmpty()) {
            sendMessageToPlayer(userId, "没有可选目标，效果作废。");
            resetPendingAction();
            moveToNextPlayer();
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("请选择目标（回复序号）：\n\n");
        for (int i = 0; i < targets.size(); i++) {
            String targetName = getPlayerDisplayName(targets.get(i));
            message.append(i + 1).append(". ").append(targetName).append("\n");
        }
        message.append("\n⏱ 25秒内未选择将自动选择第一个");
        sendMessageToPlayer(userId, message.toString());
        pendingActionType = PendingActionType.SELECT_TARGET;
        pendingCard = new Card(1, "守卫", "");
        pendingTargets = targets;
        scheduleActionTimeout();
    }

    private String handleTargetChoice(String userId, String instruction) {
        try {
            int index = Integer.parseInt(instruction.trim()) - 1;
            if (index < 0 || index >= pendingTargets.size()) {
                return "序号无效，请输入1-" + pendingTargets.size();
            }
            String targetUserId = pendingTargets.get(index);
            String operatorName = getPlayerDisplayName(userId);
            String targetName = getPlayerDisplayName(targetUserId);
            if (pendingCard.number == 1) {
                executeGuardWithTarget(userId, targetUserId, operatorName, targetName);
            } else if (pendingCard.number == 2) {
                executePriestWithTarget(userId, targetUserId, operatorName, targetName);
            } else if (pendingCard.number == 3) {
                executeBaronWithTarget(userId, targetUserId, operatorName, targetName);
            } else if (pendingCard.number == 5) {
                executePrinceWithTarget(userId, targetUserId, operatorName, targetName);
            } else if (pendingCard.number == 6) {
                executeKingWithTarget(userId, targetUserId, operatorName, targetName);
            }
            return "";
        } catch (NumberFormatException e) {
            return "请输入有效的数字序号";
        }
    }

    private void executeGuardWithTarget(String userId, String targetUserId, String operatorName, String targetName) {
        sendMessageToPlayer(userId, String.format("你选择了【%s】，现在请猜测TA的手牌数字（2-8）：\n\n⏱ 25秒内未猜测将废弃效果", targetName));
        pendingActionType = PendingActionType.GUESS_CARD;
        pendingTargets = Arrays.asList(targetUserId);
        scheduleActionTimeout();
    }

    private String handleGuessChoice(String userId, String instruction) {
        try {
            int guess = Integer.parseInt(instruction.trim());
            if (guess < 2 || guess > 8) return "请输入2-8之间的数字";
            String targetUserId = pendingTargets.get(0);
            PlayerState targetState = playerStates.get(targetUserId);
            String operatorName = getPlayerDisplayName(userId);
            String targetName = getPlayerDisplayName(targetUserId);
            if (targetState.handCard.number == guess) {
                targetState.alive = false;
                String notification = String.format("💥 %s 猜中了 %s 的手牌『%d-%s』，%s 出局！",
                        operatorName, targetName, guess, targetState.handCard.name, targetName);
                sendBroadcastMessage(notification);
            } else {
                String notification = String.format("😅 %s 猜测 %s 持有『%d』，但猜错了！",
                        operatorName, targetName, guess);
                sendBroadcastMessage(notification);
            }
            resetPendingAction();
            if (checkRoundEnd()) {
                settleRound();
            } else {
                moveToNextPlayer();
            }
            return "";
        } catch (NumberFormatException e) {
            return "请输入有效的数字（2-8）";
        }
    }

    private void executePriest(String userId) {
        List<String> targets = getValidTargets(userId);
        if (targets.isEmpty()) {
            sendMessageToPlayer(userId, "没有可选目标，效果作废。");
            resetPendingAction();
            moveToNextPlayer();
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("请选择要查看手牌的目标（回复序号）：\n\n");
        for (int i = 0; i < targets.size(); i++) {
            String targetName = getPlayerDisplayName(targets.get(i));
            message.append(i + 1).append(". ").append(targetName).append("\n");
        }
        message.append("\n⏱ 25秒内未选择将自动选择第一个");
        sendMessageToPlayer(userId, message.toString());
        pendingActionType = PendingActionType.SELECT_TARGET;
        pendingCard = new Card(2, "祭司", "");
        pendingTargets = targets;
        scheduleActionTimeout();
    }

    private void executePriestWithTarget(String userId, String targetUserId, String operatorName, String targetName) {
        PlayerState targetState = playerStates.get(targetUserId);
        sendMessageToPlayer(userId, String.format("🔍 %s 的手牌是：%s", targetName, formatCard(targetState.handCard)));
        String notification = String.format("👁️ %s 查看了 %s 的手牌", operatorName, targetName);
        sendBroadcastMessage(notification);
        resetPendingAction();
        moveToNextPlayer();
    }

    private void executeBaron(String userId) {
        List<String> targets = getValidTargets(userId);
        if (targets.isEmpty()) {
            sendMessageToPlayer(userId, "没有可选目标，效果作废。");
            resetPendingAction();
            moveToNextPlayer();
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("请选择要比较手牌的目标（回复序号）：\n\n");
        for (int i = 0; i < targets.size(); i++) {
            String targetName = getPlayerDisplayName(targets.get(i));
            message.append(i + 1).append(". ").append(targetName).append("\n");
        }
        message.append("\n⏱ 25秒内未选择将自动选择第一个");
        sendMessageToPlayer(userId, message.toString());
        pendingActionType = PendingActionType.SELECT_TARGET;
        pendingCard = new Card(3, "男爵", "");
        pendingTargets = targets;
        scheduleActionTimeout();
    }

    private void executeBaronWithTarget(String userId, String targetUserId, String operatorName, String targetName) {
        PlayerState operatorState = playerStates.get(userId);
        PlayerState targetState = playerStates.get(targetUserId);
        int operatorValue = operatorState.handCard.number;
        int targetValue = targetState.handCard.number;
        
        // 向所有人广播结果（不展示具体数值）
        String notification;
        if (operatorValue > targetValue) {
            targetState.alive = false;
            notification = String.format("⚔️ %s vs %s，%s 手牌更大，%s 出局！", operatorName, targetName, operatorName, targetName);
        } else if (operatorValue < targetValue) {
            operatorState.alive = false;
            notification = String.format("⚔️ %s vs %s，%s 手牌更大，%s 出局！", operatorName, targetName, targetName, operatorName);
        } else {
            notification = String.format("⚔️ %s vs %s，双方手牌相同，平局！", operatorName, targetName);
        }
        sendBroadcastMessage(notification);
        resetPendingAction();
        if (checkRoundEnd()) {
            settleRound();
        } else {
            moveToNextPlayer();
        }
    }

    private void executeMaid(String userId) {
        PlayerState state = playerStates.get(userId);
        state.isProtected = true;
        String displayName = getPlayerDisplayName(userId);
        String notification = String.format("🛡️ %s 受到侍女保护，本回合免疫效果！", displayName);
        sendBroadcastMessage(notification);
        resetPendingAction();
        moveToNextPlayer();
    }

    private void executePrince(String userId) {
        List<String> targets = new ArrayList<>();
        for (String playerId : playerIds) {
            // 王子可以指定任何玩家（包括自己），但不能指定受侍女保护的玩家
            if (playerId.equals(userId)) {
                // 可以选择自己
                PlayerState state = playerStates.get(playerId);
                if (state.alive) {
                    targets.add(playerId);
                }
            } else {
                // 选择其他人时，需要排除受保护的
                PlayerState state = playerStates.get(playerId);
                if (state.alive && !state.isProtected) {
                    targets.add(playerId);
                }
            }
        }
        if (targets.isEmpty()) {
            sendMessageToPlayer(userId, "没有可选目标，效果作废。");
            resetPendingAction();
            moveToNextPlayer();
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("请选择令其弃牌重抽的目标（回复序号）：\n\n");
        for (int i = 0; i < targets.size(); i++) {
            String targetName = getPlayerDisplayName(targets.get(i));
            message.append(i + 1).append(". ").append(targetName).append("\n");
        }
        message.append("\n⏱ 25秒内未选择将自动选择第一个");
        sendMessageToPlayer(userId, message.toString());
        pendingActionType = PendingActionType.SELECT_TARGET;
        pendingCard = new Card(5, "王子", "");
        pendingTargets = targets;
        scheduleActionTimeout();
    }

    private void executePrinceWithTarget(String userId, String targetUserId, String operatorName, String targetName) {
        PlayerState targetState = playerStates.get(targetUserId);
        Card discarded = targetState.handCard;
        targetState.discardedCards.add(discarded);
        String notification = String.format("👑 %s 令 %s 弃掉了『%d-%s』", operatorName, targetName, discarded.number, discarded.name);
        sendBroadcastMessage(notification);
        if (discarded.number == 8) {
            targetState.alive = false;
            String princessNotif = String.format("💥 %s 弃掉了公主，出局！", targetName);
            sendBroadcastMessage(princessNotif);
        } else if (deck.isEmpty()) {
            targetState.alive = false;
            String emptyNotif = String.format("💥 牌堆已空，%s 出局！", targetName);
            sendBroadcastMessage(emptyNotif);
        } else {
            Card newCard = deck.poll();
            targetState.handCard = newCard;
            sendMessageToPlayer(targetUserId, String.format("你重新抽到了：%s", formatCard(newCard)));
        }
        resetPendingAction();
        if (checkRoundEnd()) {
            settleRound();
        } else {
            moveToNextPlayer();
        }
    }

    private void executeKing(String userId) {
        List<String> targets = getValidTargets(userId);
        if (targets.isEmpty()) {
            sendMessageToPlayer(userId, "没有可选目标，效果作废。");
            resetPendingAction();
            moveToNextPlayer();
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("请选择交换手牌的目标（回复序号）：\n\n");
        for (int i = 0; i < targets.size(); i++) {
            String targetName = getPlayerDisplayName(targets.get(i));
            message.append(i + 1).append(". ").append(targetName).append("\n");
        }
        message.append("\n⏱ 25秒内未选择将自动选择第一个");
        sendMessageToPlayer(userId, message.toString());
        pendingActionType = PendingActionType.SELECT_TARGET;
        pendingCard = new Card(6, "国王", "");
        pendingTargets = targets;
        scheduleActionTimeout();
    }

    private void executeKingWithTarget(String userId, String targetUserId, String operatorName, String targetName) {
        PlayerState operatorState = playerStates.get(userId);
        PlayerState targetState = playerStates.get(targetUserId);
        
        // 记录交换前的手牌（用于检查公主）
        Card operatorOriginalCard = operatorState.handCard;
        Card targetOriginalCard = targetState.handCard;
        
        // 交换手牌
        Card temp = operatorState.handCard;
        operatorState.handCard = targetState.handCard;
        targetState.handCard = temp;
        
        String notification = String.format("👑 %s 和 %s 交换了手牌", operatorName, targetName);
        sendBroadcastMessage(notification);
        sendMessageToPlayer(userId, String.format("你现在的手牌是：%s", formatCard(operatorState.handCard)));
        sendMessageToPlayer(targetUserId, String.format("你现在的手牌是：%s", formatCard(targetState.handCard)));
        
        // 检查公主规则：交出公主的玩家立即出局
        boolean operatorLost = false;
        boolean targetLost = false;
        
        if (operatorOriginalCard.number == 8) {
            // 操作者交出了公主
            operatorState.alive = false;
            operatorLost = true;
            String loseNotification = String.format("💥 %s 交出了公主，出局！", operatorName);
            sendBroadcastMessage(loseNotification);
        }
        
        if (targetOriginalCard.number == 8) {
            // 目标玩家交出了公主
            targetState.alive = false;
            targetLost = true;
            String loseNotification = String.format("💥 %s 交出了公主，出局！", targetName);
            sendBroadcastMessage(loseNotification);
        }
        
        resetPendingAction();
        
        // 检查是否因公主出局导致回合结束
        if (operatorLost || targetLost) {
            if (checkRoundEnd()) {
                settleRound();
            } else {
                moveToNextPlayer();
            }
        } else {
            moveToNextPlayer();
        }
    }

    private void executeCountess(String userId) {
        String displayName = getPlayerDisplayName(userId);
        String notification = String.format("👸 %s 打出了女伯爵", displayName);
        sendBroadcastMessage(notification);
        resetPendingAction();
        moveToNextPlayer();
    }

    private void executePrincess(String userId) {
        PlayerState state = playerStates.get(userId);
        state.alive = false;
        String displayName = getPlayerDisplayName(userId);
        String notification = String.format("💥 %s 打出了公主，出局！", displayName);
        sendBroadcastMessage(notification);
        resetPendingAction();
        if (checkRoundEnd()) {
            settleRound();
        } else {
            moveToNextPlayer();
        }
    }

    private List<String> getValidTargets(String operatorUserId) {
        List<String> targets = new ArrayList<>();
        for (String playerId : playerIds) {
            if (playerId.equals(operatorUserId)) continue;
            PlayerState state = playerStates.get(playerId);
            if (state.alive && !state.isProtected) {
                targets.add(playerId);
            }
        }
        return targets;
    }

    private void clearAllProtection() {
        for (PlayerState state : playerStates.values()) {
            state.isProtected = false;
        }
    }

    private boolean checkRoundEnd() {
        long aliveCount = playerStates.values().stream().filter(s -> s.alive).count();
        return aliveCount <= 1 || deck.isEmpty();
    }

    private void settleRound() {
        List<PlayerState> aliveStates = playerStates.values().stream()
                .filter(s -> s.alive)
                .sorted((a, b) -> Integer.compare(b.handCard.number, a.handCard.number))
                .collect(Collectors.toList());
        StringBuilder message = new StringBuilder();
        message.append("════════════════\n📊 第").append(currentRound).append("局结算\n════════════════\n\n");
        if (aliveStates.size() == 1) {
            PlayerState winner = aliveStates.get(0);
            String winnerName = getPlayerDisplayName(winner.userId);
            loveMarks.put(winner.userId, loveMarks.get(winner.userId) + 1);
            message.append("🏆 胜者：").append(winnerName).append(" [").append(loveMarks.get(winner.userId)).append("💕]\n");
        } else if (aliveStates.size() > 1) {
            PlayerState winner = aliveStates.get(0);
            String winnerName = getPlayerDisplayName(winner.userId);
            loveMarks.put(winner.userId, loveMarks.get(winner.userId) + 1);
            message.append("🏆 胜者：").append(winnerName).append(" 『").append(winner.handCard.number).append("-")
                    .append(winner.handCard.name).append("』").append(" [").append(loveMarks.get(winner.userId)).append("💕]\n\n其他存活玩家：\n");
            for (int i = 1; i < aliveStates.size(); i++) {
                PlayerState state = aliveStates.get(i);
                String name = getPlayerDisplayName(state.userId);
                message.append("• ").append(name).append(" 『").append(state.handCard.number).append("-")
                        .append(state.handCard.name).append("』\n");
            }
        }
        sendBroadcastMessage(message.toString());
        boolean gameEnd = loveMarks.values().stream().anyMatch(m -> m >= 5);
        if (gameEnd) {
            finishGame();
        } else {
            currentRound++;
            startNewRound();
            String nextRoundMsg = String.format("\n════════════════\n第%d局开始！\n════════════════", currentRound);
            sendBroadcastMessage(nextRoundMsg);
            for (String playerId : playerIds) {
                PlayerState state = playerStates.get(playerId);
                sendInitialHandCard(playerId, state.handCard);
            }
            sendPlayerTurnMessage(playerIds.get(0));
        }
    }

    private void finishGame() {
        gameEnded = true;
        cancelActionTimeout();
        StringBuilder message = new StringBuilder();
        message.append("════════════════\n💕 游戏结束！最终结算\n════════════════\n\n📊 钟情标记排行\n");
        List<Map.Entry<String, Integer>> ranking = loveMarks.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())).collect(Collectors.toList());
        for (int i = 0; i < ranking.size(); i++) {
            String name = getPlayerDisplayName(ranking.get(i).getKey());
            int marks = ranking.get(i).getValue();
            String icon = (i == 0) ? "🏆" : "  ";
            message.append(icon).append(" ").append(i + 1).append(". ").append(name).append(": ").append(marks).append("💕\n");
        }
        message.append("\n💰 积分奖励\n");
        String winnerName = getPlayerDisplayName(ranking.get(0).getKey());
        message.append("🎉 ").append(winnerName).append(" +6积分（冠军）\n");
        for (int i = 1; i < ranking.size(); i++) {
            String name = getPlayerDisplayName(ranking.get(i).getKey());
            message.append("🎁 ").append(name).append(" +1积分（参与奖）\n");
        }
        message.append("\n感谢参与，期待下次对决！");
        sendBroadcastMessage(message.toString());
    }

    private String showLoveMarks() {
        StringBuilder sb = new StringBuilder();
        sb.append("════════════════\n💕 钟情标记情况\n════════════════\n当前第").append(currentRound).append("局\n\n");
        List<Map.Entry<String, Integer>> sorted = loveMarks.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())).collect(Collectors.toList());
        for (Map.Entry<String, Integer> entry : sorted) {
            String name = getPlayerDisplayName(entry.getKey());
            int marks = entry.getValue();
            sb.append("• ").append(name).append(": ").append(marks).append("/5 💕\n");
        }
        return sb.toString();
    }

    private String handleQuitGame(String userId) {
        String displayName = getPlayerDisplayName(userId);
        PlayerState state = playerStates.get(userId);
        if (state != null) state.alive = false;
        playerIds.remove(userId);
        String quitMessage = String.format("房间[%s] 游戏[%s]\n\n玩家 %s 退出游戏！", roomCode, gameName, displayName);
        sendBroadcastMessage(quitMessage);
        if (playerIds.size() < 2) {
            String endMessage = "\n剩余玩家不足，游戏结束！";
            sendBroadcastMessage(endMessage);
            gameEnded = true;
            return "QUIT_GAME:" + userId;
        }
        String continueMessage = "\n游戏继续！";
        sendBroadcastMessage(continueMessage);
        if (checkRoundEnd()) {
            settleRound();
        } else {
            moveToNextPlayer();
        }
        return "玩家已退出，游戏继续。";
    }

    private void moveToNextPlayer() {
        int attempts = 0;
        do {
            currentSeatIndex = (currentSeatIndex + 1) % playerIds.size();
            String nextPlayer = playerIds.get(currentSeatIndex);
            PlayerState state = playerStates.get(nextPlayer);
            if (state.alive) {
                // 在轮到下一个玩家时，清除该玩家的保护状态（侍女效果结束）
                state.isProtected = false;
                sendPlayerTurnMessage(nextPlayer);
                return;
            }
            attempts++;
            if (attempts > playerIds.size()) {
                settleRound();
                return;
            }
        } while (true);
    }

    protected String getPlayerDisplayName(String userId) {
        String nickName = userId;  // 默认使用userId
        
        // 从数据库获取昵称（情书游戏仅支持私聊）
        if (gameUserScoreMapper != null) {
            try {
                BotGameUserScoreExample example = new BotGameUserScoreExample();
                example.createCriteria().andUserIdEqualTo(userId);
                List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(example);
                if (scores != null && !scores.isEmpty()) {
                    BotGameUserScore userScore = scores.get(0);
                    String dbNickname = userScore.getNickname();
                    if (dbNickname != null && !dbNickname.trim().isEmpty()) {
                        nickName = dbNickname;
                    }
                }
            } catch (Exception e) {
                log.error("获取用户昵称失败，userId: {}", userId, e);
            }
        }
        
        // 添加词条装饰（如果有）
        if (SystemConfigCache.userWordMap != null && SystemConfigCache.userWordMap.containsKey(userId)) {
            String word = SystemConfigCache.userWordMap.get(userId);
            if (word != null && !word.trim().isEmpty()) {
                return nickName + "「" + word + "」";
            }
        }
        
        return nickName;
    }

    protected void sendMessageToPlayer(String userId, String message) {
        SendMsgUtil.sendMsg(userId, message);
    }

    protected void sendBroadcastMessage(String message) {
        for (String playerId : playerIds) {
            sendMessageToPlayer(playerId, message);
        }
    }

    private void resetPendingAction() {
        pendingActionType = PendingActionType.NONE;
        pendingOperatorUserId = null;
        pendingCard = null;
        pendingTargets = null;
        actionHandled = false;
    }

    private void scheduleActionTimeout() {
        cancelActionTimeout();
        actionHandled = false;
        actionTimeoutFuture = ThreadPoolManager.schedule(() -> {
            try {
                // 防止并发：原子地检查并设置 actionHandled 标志
                synchronized (this) {
                    if (gameEnded || actionHandled) return;
                    if (pendingActionType == PendingActionType.NONE) return;
                    if (pendingOperatorUserId == null) return;
                    actionHandled = true; // 设置标志，防止玩家同时操作
                }
                
                if (pendingActionType == PendingActionType.PLAY_CARD) {
                    sendMessageToPlayer(pendingOperatorUserId, "⏰ 超时未操作，自动为你打出第1张牌");
                    handlePlayCardChoice(pendingOperatorUserId, "打 1");
                } else if (pendingActionType == PendingActionType.SELECT_TARGET) {
                    sendMessageToPlayer(pendingOperatorUserId, "⏰ 超时未选择，自动为你选择第1个目标");
                    handleTargetChoice(pendingOperatorUserId, "1");
                } else if (pendingActionType == PendingActionType.GUESS_CARD) {
                    // 猜数字超时，废弃效果
                    String operatorName = getPlayerDisplayName(pendingOperatorUserId);
                    String notification = String.format("⏰ %s 猜牌超时，守卫效果废弃！", operatorName);
                    sendBroadcastMessage(notification);
                    sendMessageToPlayer(pendingOperatorUserId, "⏰ 超时未猜测，守卫效果废弃。");
                    resetPendingAction();
                    moveToNextPlayer();
                }
            } catch (Exception ignored) {
            }
        }, 25, TimeUnit.SECONDS);
    }

    private void cancelActionTimeout() {
        if (actionTimeoutFuture != null && !actionTimeoutFuture.isDone()) {
            try {
                actionTimeoutFuture.cancel(false);
            } catch (Exception ignored) {
            }
        }
    }
}
