package com.bot.base.service.game;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 游戏模版基类 - 定义所有游戏的通用行为
 * 采用模版模式，子类实现具体游戏逻辑
 * @author Assistant
 */
@Slf4j
@Getter
public abstract class BaseGamePlay {

    /**
     * 房间编号
     */
    protected String roomCode;

    /**
     * 游戏编码
     */
    protected String gameCode;

    /**
     * 游戏名称
     */
    protected String gameName;

    /**
     * 玩家ID列表
     */
    protected List<String> playerIds;

    /**
     * 玩家参与方式 Map<userId, groupId> 如果groupId为空则为私聊
     */
    protected Map<String, String> participationMap;

    /**
     * 游戏是否已开始
     */
    protected boolean gameStarted = false;

    /**
     * 游戏是否已结束
     */
    protected boolean gameEnded = false;

    /**
     * 最后活动时间
     */
    protected long lastActivityTime = System.currentTimeMillis();

    /**
     * 构造函数
     */
    public BaseGamePlay(String roomCode, String gameCode, String gameName, List<String> playerIds) {
        this.roomCode = roomCode;
        this.gameCode = gameCode;
        this.gameName = gameName;
        this.playerIds = playerIds;
    }

    /**
     * 模版方法 - 启动游戏的标准流程
     * 定义游戏启动的骨架流程，子类不可重写
     */
    public final String startGame() {
        try {
            log.info("房间[{}]开始游戏[{}]，玩家数：{}", roomCode, gameName, playerIds.size());
            
            // 1. 初始化游戏
            initGame();
            gameStarted = true;
            
            // 2. 返回游戏开始提示
            return getGameStartMessage();
            
        } catch (Exception e) {
            log.error("房间[{}]游戏启动失败", roomCode, e);
            return "游戏启动失败，请稍后重试~";
        }
    }

    /**
     * 模版方法 - 结束游戏的标准流程
     */
    public final void endGame() {
        try {
            log.info("房间[{}]游戏[{}]结束", roomCode, gameName);
            
            // 1. 执行游戏结束逻辑
            doEndGame();
            
            // 2. 标记游戏结束
            gameEnded = true;
            
        } catch (Exception e) {
            log.error("房间[{}]游戏结束处理失败", roomCode, e);
        }
    }

    /**
     * 处理玩家游戏指令
     * @param userId 用户ID
     * @param instruction 指令内容
     * @return 处理结果
     */
    public abstract String handleInstruction(String userId, String instruction);

    /**
     * 初始化游戏 - 子类实现
     * 包括初始化牌堆、玩家手牌、游戏状态等
     */
    protected abstract void initGame();

    /**
     * 获取游戏开始消息 - 子类实现
     */
    protected abstract String getGameStartMessage();

    /**
     * 执行游戏结束逻辑 - 子类实现
     */
    protected abstract void doEndGame();

    /**
     * 计算游戏积分 - 子类实现
     * @return Map<userId, score> 每个玩家获得的积分
     */
    public abstract Map<String, Integer> calculateScores();

    /**
     * 检查游戏是否可以开始
     */
    protected boolean canStart() {
        return !gameStarted && playerIds != null && !playerIds.isEmpty();
    }

    /**
     * 检查是否是游戏参与者
     */
    protected boolean isPlayer(String userId) {
        return playerIds != null && playerIds.contains(userId);
    }

    /**
     * 获取玩家索引
     */
    protected int getPlayerIndex(String userId) {
        if (playerIds == null) {
            return -1;
        }
        return playerIds.indexOf(userId);
    }

    /**
     * 设置玩家参与方式
     */
    public void setParticipationMap(Map<String, String> participationMap) {
        this.participationMap = participationMap;
    }

    /**
     * 获取玩家参与方式
     */
    public Map<String, String> getParticipationMap() {
        return this.participationMap;
    }

    /**
     * 更新最后活动时间
     */
    protected void updateLastActivityTime() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * 获取最后活动时间
     */
    public long getLastActivityTime() {
        return this.lastActivityTime;
    }

    /**
     * 检查游戏是否已结束
     */
    public boolean isGameEnded() {
        return this.gameEnded;
    }
}
