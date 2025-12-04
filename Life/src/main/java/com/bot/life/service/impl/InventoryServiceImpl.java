package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifeItem;
import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifePlayerItem;
import com.bot.life.dao.mapper.LifeItemMapper;
import com.bot.life.dao.mapper.LifePlayerItemMapper;
import com.bot.life.dao.entity.LifeSkill;
import com.bot.life.dao.entity.LifePlayerSkill;
import com.bot.life.dao.mapper.LifeSkillMapper;
import com.bot.life.dao.mapper.LifePlayerSkillMapper;
import com.bot.life.enums.ENItemType;
import com.bot.life.service.InventoryService;
import com.bot.life.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 背包服务实现
 * @author Assistant
 */
@Service
public class InventoryServiceImpl implements InventoryService {
    
    @Autowired
    private LifePlayerItemMapper playerItemMapper;
    
    @Autowired
    private LifeItemMapper itemMapper;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private LifeSkillMapper skillMapper;
    
    @Autowired
    private LifePlayerSkillMapper playerSkillMapper;
    
    @Override
    public List<LifePlayerItem> getPlayerItems(LifePlayer player) {
        return playerItemMapper.selectByPlayerId(player.getId());
    }
    
    @Override
    public boolean addItem(Long playerId, Long itemId, Integer quantity) {
        try {
            LifePlayerItem existingItem = playerItemMapper.selectByPlayerIdAndItemId(playerId, itemId);
            
            if (existingItem != null) {
                // 增加现有道具数量
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                existingItem.setUpdateTime(new Date());
                playerItemMapper.updateByPrimaryKey(existingItem);
            } else {
                // 创建新的道具记录
                LifePlayerItem newItem = new LifePlayerItem();
                newItem.setPlayerId(playerId);
                newItem.setItemId(itemId);
                newItem.setQuantity(quantity);
                newItem.setUsedCount(0);
                newItem.setCreateTime(new Date());
                newItem.setUpdateTime(new Date());
                playerItemMapper.insert(newItem);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public String useItem(LifePlayer player, Long itemId) {
        LifePlayerItem playerItem = playerItemMapper.selectByPlayerIdAndItemId(player.getId(), itemId);
        if (playerItem == null || playerItem.getQuantity() <= 0) {
            return "你没有这个道具！";
        }
        
        LifeItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            return "道具不存在！";
        }
        
        // 检查使用次数限制（仅属性类道具有限制）
        if (item.getType() == ENItemType.ATTRIBUTE.getCode() && item.getMaxUseCount() > 0) {
            if (playerItem.getUsedCount() >= item.getMaxUseCount()) {
                return String.format("『使用失败』\n\n『%s』已达使用上限！\n已使用：%d/%d次", 
                    item.getName(), playerItem.getUsedCount(), item.getMaxUseCount());
            }
        }
        
        // 应用道具效果
        String result = applyItemEffect(player, item);
        
        // 更新道具数量和使用次数
        playerItem.setQuantity(playerItem.getQuantity() - 1);
        if (item.getMaxUseCount() > 0) {
            playerItem.setUsedCount(playerItem.getUsedCount() + 1);
        }
        playerItem.setUpdateTime(new Date());
        playerItemMapper.updateByPrimaryKey(playerItem);
        
        // 更新玩家信息
        playerService.updatePlayer(player);
        
        return result;
    }
    
    @Override
    public boolean canUseItem(LifePlayer player, Long itemId) {
        LifePlayerItem playerItem = playerItemMapper.selectByPlayerIdAndItemId(player.getId(), itemId);
        if (playerItem == null || playerItem.getQuantity() <= 0) {
            return false;
        }
        
        LifeItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            return false;
        }
        
        // 检查使用次数限制
        if (item.getMaxUseCount() > 0 && playerItem.getUsedCount() >= item.getMaxUseCount()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String getInventoryDisplay(LifePlayer player) {
        List<LifePlayerItem> items = getPlayerItems(player);
        
        StringBuilder display = new StringBuilder();
        display.append("『").append(player.getNickname()).append("的背包』\n\n");
        display.append("当前灵粹：").append(player.getSpirit() != null ? player.getSpirit() : 0).append("\n\n");
        
        if (items.isEmpty()) {
            display.append("背包空空如也...");
            return display.toString();
        }
        
        String currentType = "";
        for (LifePlayerItem playerItem : items) {
            LifeItem item = playerItem.getItem();
            String itemType = getItemTypeName(item.getType());
            
            if (!itemType.equals(currentType)) {
                display.append("『").append(itemType).append("』\n");
                currentType = itemType;
            }
            
            display.append(String.format("%d. %s x%d", 
                         item.getId(), item.getName(), playerItem.getQuantity()));
            
            if (item.getMaxUseCount() > 0) {
                display.append(String.format("（已使用%d/%d次）", 
                             playerItem.getUsedCount(), item.getMaxUseCount()));
            }
            
            display.append("\n");
        }
        
        display.append("\n发送『使用+道具ID』使用道具\n");
        display.append("例如：使用1");
        
        return display.toString();
    }
    
    private String applyItemEffect(LifePlayer player, LifeItem item) {
        ENItemType itemType = ENItemType.getByCode(item.getType());
        if (itemType == null) {
            return "未知道具类型！";
        }
        
        switch (itemType) {
            case CULTIVATION:
                player.setCultivation(player.getCultivation() + item.getEffectValue());
                return String.format("『使用成功！』\n\n服用了『%s』\n获得修为：%d\n当前修为：%s", 
                                   item.getName(), item.getEffectValue(), 
                                   formatNumber(player.getCultivation()));
                
            case ATTRIBUTE:
                // 根据effect_attribute字段决定加哪个属性
                String attrName = applyAttributeBonus(player, item);
                if (attrName == null) {
                    return String.format("『使用失败』\n\n『%s』的属性配置错误！", item.getName());
                }
                player.calculateExtendedAttributes();
                return String.format("『使用成功！』\n\n服用了『%s』\n%s永久增加：%d", 
                                   item.getName(), attrName, item.getEffectValue());
                
            case STAMINA:
                int oldStamina = player.getStamina();
                player.setStamina(Math.min(player.getMaxStamina(), player.getStamina() + item.getEffectValue()));
                int recovered = player.getStamina() - oldStamina;
                return String.format("『使用成功！』\n\n服用了『%s』\n恢复体力：%d\n当前体力：%d/%d", 
                                   item.getName(), recovered, player.getStamina(), player.getMaxStamina());
                
            case RECOVERY:
                int oldHealth = player.getHealth();
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + item.getEffectValue()));
                int healed = player.getHealth() - oldHealth;
                return String.format("『使用成功！』\n\n服用了『%s』\n恢复血量：%d\n当前血量：%d/%d", 
                                   item.getName(), healed, player.getHealth(), player.getMaxHealth());
                
            case TREASURE_UPGRADE:
                // TODO: 实现法宝升级逻辑
                return String.format("『使用成功！』\n\n使用了『%s』\n法宝熟练度功能开发中...", item.getName());
            
            case SKILL_BOOK:
                return learnSkillFromBook(player, item);
                
            default:
                return "未知的道具效果！";
        }
    }
    
    private String getItemTypeName(Integer type) {
        ENItemType itemType = ENItemType.getByCode(type);
        return itemType != null ? itemType.getDesc() : "未知类型";
    }
    
    /**
     * 应用属性加成
     * @return 属性中文名称，如果属性配置错误则返回null
     */
    private String applyAttributeBonus(LifePlayer player, LifeItem item) {
        String effectAttr = item.getEffectAttribute();
        if (effectAttr == null || effectAttr.trim().isEmpty()) {
            return null;
        }
        
        int value = item.getEffectValue();
        switch (effectAttr) {
            case "speed":
                player.setSpeed(player.getSpeed() + value);
                return "速度";
            case "constitution":
                player.setConstitution(player.getConstitution() + value);
                return "体质";
            case "spirit_power":
                player.setSpiritPower(player.getSpiritPower() + value);
                return "灵力";
            case "strength":
                player.setStrength(player.getStrength() + value);
                return "力量";
            default:
                return null;
        }
    }
    
    private String formatNumber(Long number) {
        if (number < 10000) {
            return number.toString();
        } else if (number < 100000000) {
            return String.format("%.1f万", number / 10000.0);
        } else {
            return String.format("%.1f亿", number / 100000000.0);
        }
    }
    
    /**
     * 从技能书学习技能
     */
    private String learnSkillFromBook(LifePlayer player, LifeItem skillBook) {
        if (skillBook.getSkillId() == null) {
            return "『学习失败！』\n\n这本技能书没有关联技能！";
        }
        
        // 获取技能信息
        LifeSkill skill = skillMapper.selectByPrimaryKey(skillBook.getSkillId());
        if (skill == null) {
            return "『学习失败！』\n\n找不到对应的技能！";
        }
        
        // 检查是否已经学会该技能
        LifePlayerSkill existingSkill = playerSkillMapper.selectByPlayerAndSkillId(player.getId(), skill.getId());
        if (existingSkill != null) {
            return String.format("『学习失败！』\n\n你已经学会了『%s』，无法重复学习！", skill.getName());
        }
        
        // 检查属性匹配（只能学习相同属性或无属性的技能）
        if (skill.getAttribute() != 0 && skill.getAttribute() != player.getAttribute()) {
            String playerAttr = getAttributeName(player.getAttribute());
            String skillAttr = getAttributeName(skill.getAttribute());
            return String.format("『学习失败！』\n\n你是『%s』属性，无法学习『%s』属性的技能『%s』！", 
                               playerAttr, skillAttr, skill.getName());
        }
        
        // 检查等级要求
        if (player.getLevel() < skill.getRequiredLevel()) {
            return String.format("『学习失败！』\n\n学习『%s』需要%d级，当前等级：%d", 
                               skill.getName(), skill.getRequiredLevel(), player.getLevel());
        }
        
        // 检查修为要求
        if (player.getCultivation() < skill.getRequiredCultivation()) {
            return String.format("『学习失败！』\n\n学习『%s』需要%d修为，当前修为：%d", 
                               skill.getName(), skill.getRequiredCultivation(), player.getCultivation());
        }
        
        try {
            // 学习技能
            LifePlayerSkill playerSkill = new LifePlayerSkill();
            playerSkill.setPlayerId(player.getId());
            playerSkill.setSkillId(skill.getId());
            playerSkill.setSkillLevel(1);
            playerSkill.setCurrentCooldown(0);
            playerSkill.setLearnTime(new Date());
            
            playerSkillMapper.insert(playerSkill);
            
            return String.format("『学习成功！』\n\n成功学会了技能『%s』！\n\n技能描述：%s\n\n可在技能界面查看详情。", 
                               skill.getName(), skill.getDescription());
                               
        } catch (Exception e) {
            e.printStackTrace();
            return "『学习失败！』\n\n技能学习过程中出现错误！";
        }
    }
    
    private String getAttributeName(Integer attribute) {
        switch (attribute) {
            case 0: return "无属性";
            case 1: return "金";
            case 2: return "木";
            case 3: return "水";
            case 4: return "火";
            case 5: return "土";
            default: return "未知";
        }
    }
}
