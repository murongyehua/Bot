package com.bot.life.dto;

import lombok.Data;

import java.util.List;

/**
 * 战斗结果
 * @author Assistant
 */
@Data
public class BattleResult {
    private boolean victory; // 是否获胜
    private boolean escaped; // 是否逃跑
    private Long damageDealt; // 造成的伤害
    private Long damageTaken; // 受到的伤害
    private Integer roundsCount; // 战斗回合数
    
    // 奖励
    private Long spiritReward; // 灵粹奖励
    private Long cultivationReward; // 修为奖励
    private List<ItemReward> itemRewards; // 道具奖励
    private List<EquipmentReward> equipmentRewards; // 装备奖励
    
    private String battleLog; // 战斗日志
    
    @Data
    public static class ItemReward {
        private Long itemId;
        private String itemName;
        private Integer quantity;
    }
    
    @Data
    public static class EquipmentReward {
        private Long equipmentId;
        private String equipmentName;
        private Integer quantity;
    }
}
