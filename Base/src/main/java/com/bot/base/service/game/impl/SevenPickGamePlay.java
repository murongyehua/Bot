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

    // 功能牌目标选择超时控制
    private ScheduledFuture<?> choiceTimeoutFuture;
    private volatile boolean choiceHandled = false;
    
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
    private int initialPlayerCount = 0; // 游戏开始时的参与人数（用于结算判断）
    private Set<String> quitPlayers = new HashSet<>(); // 中途退出的玩家

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

    // 回合超时控制
    private ScheduledFuture<?> turnTimeoutFuture;
    private volatile boolean turnHandled = false;

    public SevenPickGamePlay(String roomCode, String gameCode, String gameName, List<String> playerIds) {
        super(roomCode, gameCode, gameName, playerIds);
    }

    @Override
    protected void initGame() {
        // 随机打乱座位
        Collections.shuffle(playerIds);

        // 记录初始参与人数（用于结算判断）
        initialPlayerCount = playerIds.size();
        quitPlayers.clear();

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

        // 过滤掉中途退出的玩家，只对完整参与的玩家进行结算
        List<Map.Entry<String, Integer>> sortedPlayers = totalScore.entrySet()
                .stream()
                .filter(entry -> !quitPlayers.contains(entry.getKey())) // 过滤退出玩家
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        // 根据初始参与人数和排名分配小林游戏积分
        // 当初始参与人数 <= 3 时，只有第一名获得3分，其他人1分
        // 当初始参与人数 >= 4 时，按正常规则：第一10分，第二5分，第三3分，其他1分
        for (int i = 0; i < sortedPlayers.size(); i++) {
            String userId = sortedPlayers.get(i).getKey();
            int gameScore;
            
            if (initialPlayerCount <= 3) {
                // 少于等于3人：第一名3分，其他1分
                gameScore = (i == 0) ? 3 : 1;
            } else {
                // 4人及以上：正常结算规则
                switch (i) {
                    case 0: gameScore = 10; break; // 第一名
                    case 1: gameScore = 5; break;  // 第二名
                    case 2: gameScore = 3; break;  // 第三名
                    default: gameScore = 1;        // 其他参与者
                }
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

        // 分离退出玩家和完整参与玩家
        List<Map.Entry<String, Integer>> activePlayers = totalScore.entrySet()
                .stream()
                .filter(entry -> !quitPlayers.contains(entry.getKey()))
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());
        
        List<Map.Entry<String, Integer>> quitPlayersList = totalScore.entrySet()
                .stream()
                .filter(entry -> quitPlayers.contains(entry.getKey()))
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        // 游戏内成绩排名（仅显示完整参与玩家）
        sb.append("🎮 游戏成绩:\n");
        String[] rankIcons = {"🥇", "🥈", "🥉"}; // 金银铜牌
        
        for (int i = 0; i < activePlayers.size(); i++) {
            String rankIcon = i < rankIcons.length ? rankIcons[i] + " " : "   ";
            String rankName = "第" + convertToChineseNumber(i + 1) + "名";
            String userId = activePlayers.get(i).getKey();
            String displayName = getPlayerDisplayName(userId);
            int score = activePlayers.get(i).getValue();
            
            sb.append(rankIcon).append(rankName).append(": ")
              .append(displayName).append(" - ")
              .append(score).append("分\n");
        }
        
        // 如果有退出玩家，单独列出（不参与排名和结算）
        if (!quitPlayersList.isEmpty()) {
            sb.append("\n⚠️ 中途退出（不参与结算）:\n");
            for (Map.Entry<String, Integer> entry : quitPlayersList) {
                String displayName = getPlayerDisplayName(entry.getKey());
                int score = entry.getValue();
                sb.append("  • ").append(displayName)
                  .append(" - ").append(score).append("分\n");
            }
        }

        // 游戏系统积分奖励（仅奖励完整参与玩家）
        sb.append("\n⭐ 系统积分奖励:\n");
        
        // 根据初始参与人数决定奖励规则
        if (initialPlayerCount <= 3) {
            sb.append("(参与人数≤3，防刷分模式)\n");
        }
        
        for (int i = 0; i < activePlayers.size(); i++) {
            String userId = activePlayers.get(i).getKey();
            String displayName = getPlayerDisplayName(userId);
            int gameScore;
            String rewardDesc;
            
            if (initialPlayerCount <= 3) {
                // 少于等于3人：第一名3分，其他1分
                if (i == 0) {
                    gameScore = 3;
                    rewardDesc = "🏆 第一名";
                } else {
                    gameScore = 1;
                    rewardDesc = "🎁 参与奖励";
                }
            } else {
                // 4人及以上：正常结算
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
            return null;
        }

        instruction = instruction.trim();

        // 处理退出游戏
        if ("退出游戏".equals(instruction)) {
            return handleQuitGame(userId);
        }
        
        // 处理积分查询
        if ("积分".equals(instruction)) {
            return handleQueryScores();
        }
        
        // 处理牌堆查询
        if ("牌堆".equals(instruction)) {
            return handleQueryDeck();
        }

        // 处理待处理的功能牌选择
        if (pendingEffectType != PendingEffectType.NONE) {
            if (userId.equals(pendingOperatorUserId)) {
                // 玩家选择目标：取消本次选择超时
                cancelChoiceTimeout();
                choiceHandled = true;
                return handlePendingEffectChoice(userId, instruction);
            } else {
                String operatorName = getPlayerDisplayName(pendingOperatorUserId);
                return "请等待" + operatorName + "选择目标~";
            }
        }

        // 正常回合处理：只有有效指令才取消超时
        if ("翻牌".equals(instruction)) {
            return handleDrawCard(userId);
        } else if ("结束".equals(instruction)) {
            return handleEndTurn(userId);
        } else {
            return "";
        }
    }

    /**
     * 处理翻牌
     */
    private String handleDrawCard(String userId) {
        // 防止并发重复翻牌：检查是否已处理过本回合
        if (turnHandled) {
            return "操作已处理，请等待下一回合~";
        }
        
        // 检查是否轮到该玩家
        String currentPlayer = playerIds.get(currentSeatIndex);
        if (!userId.equals(currentPlayer)) {
            return "还没轮到您哦~";
        }
        
        // 取消超时并立即标记为已处理，防止并发
        cancelTurnTimeout();
        turnHandled = true;

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
        
        // 将玩家标记为已结束
        endedThisRound.put(userId, true);
        
        // 标记为中途退出（不参与结算）
        quitPlayers.add(userId);
        
        // 从玩家列表中移除
        int quitIndex = playerIds.indexOf(userId);
        if (quitIndex == -1) {
            return "你不在游戏中~";
        }
        
        playerIds.remove(quitIndex);
        
        // 广播退出消息
        String quitMessage = String.format("房间[%s] 游戏[%s]\n\n玩家 %s 退出游戏!", 
                roomCode, gameName, displayName);
        sendBroadcastMessage(quitMessage);
        
        // 检查剩余玩家数量
        if (playerIds.size() < 2) {
            // 玩家不足，结束游戏
            String endMessage = "\n剩余玩家不足，游戏结束！";
            sendBroadcastMessage(endMessage);
            gameEnded = true;
            return "QUIT_GAME:" + userId;
        }
        
        // 调整当前座位索引
        if (quitIndex <= currentSeatIndex && currentSeatIndex > 0) {
            currentSeatIndex--;
        }
        if (currentSeatIndex >= playerIds.size()) {
            currentSeatIndex = 0;
        }
        
        // 如果当前轮到退出玩家，移动到下一个玩家
        String continueMessage = "\n游戏继续！";
        sendBroadcastMessage(continueMessage);
        
        // 发送下一个玩家的回合消息
        String nextPlayer = playerIds.get(currentSeatIndex);
        sendTurnMessage(nextPlayer);
        
        return "玩家已退出，游戏继续。";
    }

    /**
     * 处理积分查询
     */
    private String handleQueryScores() {
        StringBuilder sb = new StringBuilder();
        sb.append("─────────────\n");
        sb.append("📊 当前积分情况\n");
        sb.append("─────────────\n\n");
            
        // 按总分排序
        List<Map.Entry<String, Integer>> sortedPlayers = totalScore.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());
            
        for (Map.Entry<String, Integer> entry : sortedPlayers) {
            String userId = entry.getKey();
            String displayName = getPlayerDisplayName(userId);
            int total = entry.getValue();
                
            // 检查玩家是否已结束
            boolean isEnded = endedThisRound.getOrDefault(userId, false) || !playerIds.contains(userId);
                
            if (isEnded) {
                // 已结束的玩家只显示全局积分
                sb.append(String.format("%s: %d(已结束)\n", displayName, total));
            } else {
                // 未结束的玩家显示全局+本轮
                int round = calculateRoundScore(userId);
                sb.append(String.format("%s: %d+%d(全局+本轮)\n", displayName, total, round));
            }
        }
            
        return sb.toString();
    }

    /**
     * 处理牌堆查询
     */
    private String handleQueryDeck() {
        StringBuilder sb = new StringBuilder();
        sb.append("─────────────\n");
        sb.append("🎴 剩余牌堆\n");
        sb.append("─────────────\n\n");
        
        // 统计牌堆中的牌
        Map<String, Integer> cardCount = new HashMap<>();
        for (Card card : deck) {
            String cardName = card.name;
            cardCount.put(cardName, cardCount.getOrDefault(cardName, 0) + 1);
        }
        
        if (cardCount.isEmpty()) {
            sb.append("牌堆已空~\n");
        } else {
            // 按特定顺序展示：基础牌、计分牌、功能牌
            // 基础牌
            for (int i = 12; i >= 1; i--) {
                String cardName = String.valueOf(i);
                if (cardCount.containsKey(cardName)) {
                    sb.append(String.format("『%s』 x%d张\n", cardName, cardCount.get(cardName)));
                }
            }
            
            // 计分牌
            String[] scoreCards = {"x2", "+2", "+4", "+6", "+8", "+10"};
            for (String cardName : scoreCards) {
                if (cardCount.containsKey(cardName)) {
                    sb.append(String.format("『%s』 x%d张\n", cardName, cardCount.get(cardName)));
                }
            }
            
            // 功能牌
            String[] actionCards = {"再翻三张", "冻结", "二次机会"};
            for (String cardName : actionCards) {
                if (cardCount.containsKey(cardName)) {
                    sb.append(String.format("『%s』 x%d张\n", cardName, cardCount.get(cardName)));
                }
            }
        }
        
        sb.append(String.format("\n总计剩余：%d张", deck.size()));
        
        return sb.toString();
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
        
        // 翻牌卡面简报
        message.append("─────────────\n");
        message.append("你翻到了：『").append(card.name).append("』\n");

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
                message.append("第1张：『").append(card.value).append("』 | 重复 → 消耗『二次机会』继续\n");
                roundHasSecondChance.put(userId, false);
                // 从功能牌列表移除
                roundActionCards.get(userId).remove("二次机会");
                return true; // 继续当前玩家回合
            } else {
                // 强制结束
                message.append("第1张：『").append(card.value).append("』 | 重复 → 强制结束，本轮记分清零\n");
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
                message.append("第1张：『").append(card.value)
                       .append("』 | 基础 +").append(card.value)
                       .append(" | 本轮基础:").append(roundBaseSum.get(userId))
                       .append(" | 七张达成 → 强制结束 +15\n");
                message.append("wow，你完成了七连翻,所得积分额外+15!!\n");
                message.append("强制结束，本轮次计分为").append(finalScore)
                       .append("，总分为").append(totalScore.get(userId) + finalScore).append("!\n");
                endPlayerRound(userId, finalScore);
                
                // 向所有群广播七连翻喜讯
                String displayName = getPlayerDisplayName(userId);
                String broadcastMsg = String.format("🎉 喜讯！玩家【%s】完成了七连翻，额外获得15分奖励！\n",
                        displayName, finalScore, totalScore.get(userId) + finalScore);
                sendBroadcastMessage(broadcastMsg);
                
                return true;
            } else {
                message.append("第1张：『").append(card.value)
                       .append("』 | 基础 +").append(card.value)
                       .append(" | 本轮基础:").append(roundBaseSum.get(userId)).append("\n");
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
            
            // 检查是否有可选目标
            if (targets.isEmpty()) {
                message.append("\n暂无可选目标，『冻结』作废!");
                return true; // 直接移动到下一个玩家
            }
            
            pendingEffectType = PendingEffectType.FREEZE;
            pendingOperatorUserId = userId;
            pendingTargets = targets;
            // 启动功能牌选择超时计时
            scheduleChoiceTimeout();
            return false; // 等待选择
        } else if ("再翻三张".equals(card.name)) {
            message.append("🎯 请选择目标（发送序号）：\n");
            List<String> targets = buildTargetList(userId, message);
            
            // 检查是否有可选目标
            if (targets.isEmpty()) {
                message.append("\n暂无可选目标，『再翻三张』作废!");
                return true; // 直接移动到下一个玩家
            }
            
            pendingEffectType = PendingEffectType.RE_DRAW_3;
            pendingOperatorUserId = userId;
            pendingTargets = targets;
            // 启动功能牌选择超时计时
            scheduleChoiceTimeout();
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
                
                // 向其他群广播简短通知（排除操作者和目标玩家所在群）
                broadcastActionCardNotification(userId, targetUserId, "冻结");

                // 清除待处理状态
                pendingEffectType = PendingEffectType.NONE;
                pendingOperatorUserId = null;
                pendingTargets.clear();
                choiceHandled = true;
                cancelChoiceTimeout();
                
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
                    
                    // 检查是否有可选目标，如果没有则跳过该功能牌
                    if (targets.isEmpty()) {
                        // 清除待处理状态
                        pendingEffectType = PendingEffectType.NONE;
                        pendingOperatorUserId = null;
                        pendingTargets.clear();
                        
                        // 通知玩家功能牌作废
                        String cardName = nextAction.type == PendingEffectType.FREEZE ? "冻结" : "再翻三张";
                        sendMessageToPlayer(nextAction.operatorUserId, 
                            actionMessage.toString() + "\n暂无可选目标，『" + cardName + "』作废!");
                        
                        // 继续检查队列中是否还有其他功能牌
                        // 递归处理（通过移动到下一个玩家会自动处理队列）
                    } else {
                        pendingTargets = targets;
                        sendMessageToPlayer(nextAction.operatorUserId, actionMessage.toString());
                        return ""; // 消息已发送,不需要返回
                    }
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
                
                // 向其他群广播简短通知（排除操作者和目标玩家所在群）
                broadcastActionCardNotification(userId, targetUserId, "再翻三张");

                // 清除当前待处理状态
                pendingEffectType = PendingEffectType.NONE;
                pendingOperatorUserId = null;
                pendingTargets.clear();
                choiceHandled = true;
                cancelChoiceTimeout();

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
                        
                        // 检查是否有可选目标，如果没有则跳过该功能牌
                        if (targets.isEmpty()) {
                            // 清除待处理状态
                            pendingEffectType = PendingEffectType.NONE;
                            pendingOperatorUserId = null;
                            pendingTargets.clear();
                            
                            // 通知玩家功能牌作废
                            String cardName = nextAction.type == PendingEffectType.FREEZE ? "冻结" : "再翻三张";
                            sendMessageToPlayer(nextAction.operatorUserId, 
                                actionMessage.toString() + "\n暂无可选目标，『" + cardName + "』作废!");
                            
                            // 继续检查队列中是否还有其他功能牌
                            // 递归处理（通过移动到下一个玩家会自动处理队列）
                        } else {
                            pendingTargets = targets;

                            // 新的功能牌选择：启动选择超时
                            scheduleChoiceTimeout();

                            sendMessageToPlayer(nextAction.operatorUserId, actionMessage.toString());
                            return ""; // 消息已发送,不需要返回
                        }
                    }
                } else {
                    // 强制结束时，清空所有待处理的功能牌队列
                    // 因为玩家已经结束，不能再使用功能牌
                    pendingActionQueue.clear();
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

    // 功能牌目标选择：启动与取消
    private void scheduleChoiceTimeout() {
        // 取消之前的选择计时
        cancelChoiceTimeout();
        choiceHandled = false;
        // 若没有待选择的效果或目标为空，直接返回
        if (pendingEffectType == PendingEffectType.NONE || pendingTargets == null || pendingTargets.isEmpty()) {
            return;
        }
        final PendingEffectType effectSnapshot = pendingEffectType;
        final String operatorSnapshot = pendingOperatorUserId;
        final List<String> targetsSnapshot = new ArrayList<>(pendingTargets);

        choiceTimeoutFuture = ThreadPoolManager.schedule(() -> {
            try {
                if (gameEnded) return;
                if (effectSnapshot == PendingEffectType.NONE) return;
                if (choiceHandled) return;
                if (operatorSnapshot == null) return;
                if (targetsSnapshot.isEmpty()) return;

                // 自动选择第一个目标
                String targetUserId = targetsSnapshot.get(0);
                String operatorName = getPlayerDisplayName(operatorSnapshot);
                String targetName = getPlayerDisplayName(targetUserId);
                sendMessageToPlayer(operatorSnapshot,
                        "【系统提示】25 秒内未选择目标，已自动为你选择【" + targetName + "】作为『" +
                                (effectSnapshot == PendingEffectType.FREEZE ? "冻结" : "再翻三张") + "』目标。");

                // 构造一个“选择第一个目标”的虚拟指令
                handlePendingEffectChoice(operatorSnapshot, "1");
            } catch (Exception ignored) {
            }
        }, 25, TimeUnit.SECONDS);
    }

    private void cancelChoiceTimeout() {
        if (choiceTimeoutFuture != null && !choiceTimeoutFuture.isDone()) {
            try { choiceTimeoutFuture.cancel(false); } catch (Exception ignored) {}
        }
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
                        String broadcastMsg = String.format("🎉 喜讯！玩家【%s】在再翻三张过程中完成了七连翻，额外获得15分奖励！",
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
        // 结束游戏：取消回合超时计时
        cancelTurnTimeout();
        turnHandled = true;
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
        message.append("⏱ 若 25 秒内未操作，系统将自动为你翻牌。\n");

        // 检查是否有人达到200分
        if (hasPlayerReached200 && !userId.equals(playerReached200)) {
            String reachedPlayerName = getPlayerDisplayName(playerReached200);
            message.append("\n请注意!").append(reachedPlayerName)
                   .append("已拿到").append(totalScore.get(playerReached200))
                   .append("分!\n本轮次结束后将进行结算!\n");
        }

        message.append("\n").append(buildCardStatus(userId));

        sendMessageToPlayer(userId, message.toString());

        // 启动本回合超时计时
        scheduleTurnTimeout(userId);
    }

    // 回合超时：启动与取消
    private void scheduleTurnTimeout(String userId) {
        // 取消上一轮残留计时
        cancelTurnTimeout();
        turnHandled = false;
        turnTimeoutFuture = ThreadPoolManager.schedule(() -> {
            try {
                // 校验状态仍然有效
                if (gameEnded) return;
                String currentPlayer = playerIds.get(currentSeatIndex);
                if (!userId.equals(currentPlayer)) return;
                if (endedThisRound.get(userId) || frozenThisRound.get(userId)) return;
                if (turnHandled) return;

                // 提示并自动翻牌
                sendMessageToPlayer(userId, "【系统提示】超过 25 秒未操作，系统已自动为你翻牌。");
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
    if (basic != null && !basic.isEmpty()) {
        sb.append("基础:");
        List<Integer> sortedBasic = new ArrayList<>(basic);
        Collections.sort(sortedBasic);
        for (Integer value : sortedBasic) {
            sb.append("『").append(value).append("』");
        }
        int baseSum = roundBaseSum.get(userId);
        sb.append("  合计:").append(baseSum);
        Boolean hasX2 = roundHasX2.get(userId);
        if (hasX2 != null && hasX2) {
            sb.append("（x2生效 → ").append(baseSum * 2).append("）");
        }
        sb.append("\n");
    }

    // 计分牌
    List<String> scoreCards = roundScoreCards.get(userId);
    if (scoreCards != null && !scoreCards.isEmpty()) {
        sb.append("计分:");
        int extraSum = 0;
        for (String card : scoreCards) {
            sb.append("『").append(card).append("』");
            if (card != null && card.startsWith("+")) {
                try {
                    extraSum += Integer.parseInt(card.substring(1));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        sb.append("  合计:+").append(extraSum).append("\n");
    }

    // 功能牌
    List<String> actionCards = roundActionCards.get(userId);
    if (actionCards != null && !actionCards.isEmpty()) {
        sb.append("功能:");
        for (String card : actionCards) {
            // 冻结 / 再翻三张 属于即时功能，通过功能触发提示展示
            if (!"冻结".equals(card) && !"再翻三张".equals(card)) {
                sb.append("『").append(card).append("』");
            }
        }
        sb.append("\n");
    }

    return sb.toString();
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
            // 群聊参与,获取群昵称
            nickName = SendMsgUtil.getGroupNickName(groupId, userId);
            nickName = nickName != null && !nickName.trim().isEmpty() ? nickName : userId;
        } else {
            // 私聊参与,直接返回userId
            nickName = userId;
        }
        
        // 带上佩戴的词条（如果有）
        if (SystemConfigCache.userWordMap != null && SystemConfigCache.userWordMap.containsKey(userId)) {
            String word = SystemConfigCache.userWordMap.get(userId);
            if (word != null && !word.trim().isEmpty()) {
                return nickName + "「" + word + "」";
            }
        }
        
        return nickName;
    }

    /**
     * 将阿拉伯数字转换为中文数字
     */
    private String convertToChineseNumber(int num) {
        String[] chineseNumbers = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        if (num <= 10) {
            return chineseNumbers[num];
        } else if (num < 20) {
            return "十" + chineseNumbers[num - 10];
        } else {
            return chineseNumbers[num / 10] + "十" + (num % 10 == 0 ? "" : chineseNumbers[num % 10]);
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
     * 广播功能牌使用通知（排除操作者和目标玩家所在群）
     */
    private void broadcastActionCardNotification(String operatorUserId, String targetUserId, String actionCardName) {
        String operatorName = getPlayerDisplayName(operatorUserId);
        String targetName = getPlayerDisplayName(targetUserId);
        
        // 构建极简通知消息
        String notification = String.format("🎯 [%s] 使用『%s』→ [%s]", 
                operatorName, actionCardName, targetName);
        
        // 获取操作者和目标玩家所在的群ID
        String operatorGroupId = participationMap != null ? participationMap.get(operatorUserId) : null;
        String targetGroupId = participationMap != null ? participationMap.get(targetUserId) : null;
        
        // 收集需要排除的群ID
        Set<String> excludeGroups = new HashSet<>();
        if (operatorGroupId != null && !operatorGroupId.trim().isEmpty()) {
            excludeGroups.add(operatorGroupId);
        }
        if (targetGroupId != null && !targetGroupId.trim().isEmpty()) {
            excludeGroups.add(targetGroupId);
        }
        
        // 分组并广播
        Map<String, List<String>> groupPlayers = new HashMap<>();
        for (String playerId : playerIds) {
            String groupId = participationMap != null ? participationMap.get(playerId) : null;
            if (groupId != null && !groupId.trim().isEmpty() && !excludeGroups.contains(groupId)) {
                groupPlayers.computeIfAbsent(groupId, k -> new ArrayList<>()).add(playerId);
            }
        }
        
        // 向其他群发送通知（每个群只发一次）
        for (String groupId : groupPlayers.keySet()) {
            SendMsgUtil.sendGroupMsgForGame(groupId, notification, "");
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
