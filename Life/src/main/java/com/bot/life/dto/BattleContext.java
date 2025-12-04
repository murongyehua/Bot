package com.bot.life.dto;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifeMonster;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 战斗上下文
 * @author Assistant
 */
@Data
public class BattleContext {
    private LifePlayer player;
    private LifeMonster monster;
    private List<LifePlayer> teammates; // 队友列表（组队战斗时使用）
    
    private Integer currentRound; // 当前回合数
    private String battleId; // 战斗ID
    private Integer battleType; // 战斗类型：1普通战斗2副本战斗3世界BOSS战斗
    
    // 战斗状态效果
    private Map<String, BattleEffect> playerEffects; // 玩家身上的效果
    private Map<String, BattleEffect> monsterEffects; // 怪物身上的效果
    
    private boolean isPlayerTurn; // 是否玩家回合
    private boolean battleEnded; // 战斗是否结束
    private boolean playerWin; // 玩家是否获胜
    
    private StringBuilder battleLog; // 战斗日志
    
    public BattleContext() {
        this.currentRound = 1;
        this.isPlayerTurn = true;
        this.battleEnded = false;
        this.battleLog = new StringBuilder();
    }
    
    /**
     * 添加战斗日志
     */
    public void addLog(String log) {
        this.battleLog.append(log).append("\n");
    }
    
    /**
     * 获取战斗日志
     */
    public String getBattleLog() {
        return this.battleLog.toString();
    }
}
