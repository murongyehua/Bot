package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifePlayerSkill;
import com.bot.life.dao.entity.LifeSkill;
import com.bot.life.dao.mapper.LifePlayerSkillMapper;
import com.bot.life.dao.mapper.LifeSkillMapper;
import com.bot.life.enums.ENAttribute;
import com.bot.life.enums.ENSkillType;
import com.bot.life.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 技能服务实现
 * @author Assistant
 */
@Service
public class SkillServiceImpl implements SkillService {
    
    @Autowired
    private LifePlayerSkillMapper playerSkillMapper;
    
    @Autowired
    private LifeSkillMapper skillMapper;
    
    @Override
    public String getPlayerSkills(LifePlayer player) {
        List<LifePlayerSkill> playerSkills = playerSkillMapper.selectByPlayerId(player.getId());
        
        if (playerSkills.isEmpty()) {
            return "『技能列表』\n\n你还没有学会任何技能\n\n技能需要通过使用背包中的技能书学习";
        }
        
        StringBuilder skillList = new StringBuilder();
        skillList.append("『").append(player.getNickname()).append("的技能』\n\n");
        
        for (LifePlayerSkill playerSkill : playerSkills) {
            LifeSkill skill = playerSkill.getSkill();
            if (skill != null) {
                skillList.append(String.format("『%s』Lv.%d\n", skill.getName(), playerSkill.getSkillLevel()));
                skillList.append(String.format("类型：%s\n", getSkillTypeName(skill.getType())));
                skillList.append(String.format("威力：%d\n", skill.getPower()));
                skillList.append(String.format("冷却：%d秒\n", skill.getCooldown()));
                
                // 检查冷却状态
                if (isSkillAvailable(player.getId(), skill.getId())) {
                    skillList.append("状态：可用\n");
                } else {
                    skillList.append("状态：冷却中\n");
                }
                
                skillList.append(String.format("%s\n\n", skill.getDescription()));
            }
        }
        
        skillList.append("发送『可学技能』查看可学习的技能\n");
        skillList.append("发送『学习技能+技能ID』学习新技能");
        
        return skillList.toString();
    }
    
    @Override
    public String learnSkill(LifePlayer player, Long skillId) {
        // 检查技能是否存在
        LifeSkill skill = skillMapper.selectByPrimaryKey(skillId);
        if (skill == null) {
            return "技能不存在！";
        }
        
        // 检查是否已经学会
        LifePlayerSkill existingSkill = playerSkillMapper.selectByPlayerIdAndSkillId(player.getId(), skillId);
        if (existingSkill != null) {
            return String.format("你已经学会了『%s』！", skill.getName());
        }
        
        // 检查等级要求
        if (player.getLevel() < skill.getRequiredLevel()) {
            return String.format("『%s』需要达到%d级才能学习！", skill.getName(), skill.getRequiredLevel());
        }
        
        // 检查属性要求
        if (skill.getAttribute() != 0 && !skill.getAttribute().equals(player.getAttribute())) {
            String requiredAttr = getAttributeName(skill.getAttribute());
            String playerAttr = getAttributeName(player.getAttribute());
            return String.format("『%s』需要%s属性才能学习！你的属性是%s", skill.getName(), requiredAttr, playerAttr);
        }
        
        // 检查修为要求
        if (player.getCultivation() < skill.getRequiredCultivation()) {
            return String.format("『%s』需要%d修为才能学习！当前修为：%d", 
                               skill.getName(), skill.getRequiredCultivation(), player.getCultivation());
        }
        
        try {
            // 学习技能
            LifePlayerSkill playerSkill = new LifePlayerSkill();
            playerSkill.setPlayerId(player.getId());
            playerSkill.setSkillId(skillId);
            playerSkill.setSkillLevel(1);
            playerSkill.setCurrentCooldown(0);
            playerSkill.setLearnTime(new Date());
            
            playerSkillMapper.insert(playerSkill);
            
            return String.format("『学习成功！』\n\n学会了技能『%s』\n\n%s", skill.getName(), skill.getDescription());
            
        } catch (Exception e) {
            e.printStackTrace();
            return "学习技能失败！";
        }
    }
    
    @Override
    public String upgradeSkill(LifePlayer player, Long skillId) {
        LifePlayerSkill playerSkill = playerSkillMapper.selectByPlayerIdAndSkillId(player.getId(), skillId);
        if (playerSkill == null) {
            return "你还没有学会这个技能！";
        }
        
        LifeSkill skill = skillMapper.selectByPrimaryKey(skillId);
        if (skill == null) {
            return "技能不存在！";
        }
        
        if (playerSkill.getSkillLevel() >= skill.getMaxLevel()) {
            return String.format("『%s』已经达到最高等级！", skill.getName());
        }
        
        // 计算升级所需修为
        int requiredCultivation = skill.getRequiredCultivation() * (playerSkill.getSkillLevel() + 1);
        if (player.getCultivation() < requiredCultivation) {
            return String.format("升级『%s』需要%d修为！当前修为：%d", 
                               skill.getName(), requiredCultivation, player.getCultivation());
        }
        
        try {
            // 升级技能
            playerSkill.setSkillLevel(playerSkill.getSkillLevel() + 1);
            playerSkillMapper.updateByPrimaryKey(playerSkill);
            
            // 扣除修为
            player.setCultivation(player.getCultivation() - requiredCultivation);
            
            return String.format("『升级成功！』\n\n『%s』升级到Lv.%d\n消耗修为：%d", 
                               skill.getName(), playerSkill.getSkillLevel(), requiredCultivation);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "技能升级失败！";
        }
    }
    
    @Override
    public boolean isSkillAvailable(Long playerId, Long skillId) {
        LifePlayerSkill playerSkill = playerSkillMapper.selectByPlayerIdAndSkillId(playerId, skillId);
        if (playerSkill == null) {
            return false;
        }
        
        // 检查冷却时间
        if (playerSkill.getLastUsedTime() == null) {
            return true;
        }
        
        LifeSkill skill = skillMapper.selectByPrimaryKey(skillId);
        if (skill == null) {
            return false;
        }
        
        long timeSinceLastUse = (System.currentTimeMillis() - playerSkill.getLastUsedTime().getTime()) / 1000;
        return timeSinceLastUse >= skill.getCooldown();
    }
    
    @Override
    public void setSkillCooldown(Long playerId, Long skillId) {
        LifePlayerSkill playerSkill = playerSkillMapper.selectByPlayerIdAndSkillId(playerId, skillId);
        if (playerSkill != null) {
            playerSkill.setLastUsedTime(new Date());
            playerSkillMapper.updateByPrimaryKey(playerSkill);
        }
    }
    
    @Override
    public String getAvailableSkills(LifePlayer player) {
        List<LifeSkill> allSkills = skillMapper.selectAll();
        List<LifePlayerSkill> learnedSkills = playerSkillMapper.selectByPlayerId(player.getId());
        
        StringBuilder availableSkills = new StringBuilder();
        availableSkills.append("『可学习技能』\n\n");
        
        boolean hasAvailable = false;
        for (LifeSkill skill : allSkills) {
            // 检查是否已经学会
            boolean alreadyLearned = learnedSkills.stream()
                .anyMatch(ps -> ps.getSkillId().equals(skill.getId()));
            
            if (alreadyLearned) {
                continue;
            }
            
            // 检查学习条件
            boolean canLearn = true;
            StringBuilder requirements = new StringBuilder();
            
            if (player.getLevel() < skill.getRequiredLevel()) {
                canLearn = false;
                requirements.append(String.format("等级%d ", skill.getRequiredLevel()));
            }
            
            if (skill.getAttribute() != 0 && !skill.getAttribute().equals(player.getAttribute())) {
                canLearn = false;
                requirements.append(String.format("%s属性 ", getAttributeName(skill.getAttribute())));
            }
            
            if (player.getCultivation() < skill.getRequiredCultivation()) {
                canLearn = false;
                requirements.append(String.format("修为%d ", skill.getRequiredCultivation()));
            }
            
            if (canLearn || requirements.length() > 0) {
                hasAvailable = true;
                availableSkills.append(String.format("%d. %s\n", skill.getId(), skill.getName()));
                availableSkills.append(String.format("类型：%s\n", getSkillTypeName(skill.getType())));
                availableSkills.append(String.format("威力：%d\n", skill.getPower()));
                
                if (canLearn) {
                    availableSkills.append("状态：可学习\n");
                } else {
                    availableSkills.append(String.format("要求：%s\n", requirements.toString().trim()));
                }
                
                availableSkills.append(String.format("%s\n\n", skill.getDescription()));
            }
        }
        
        if (!hasAvailable) {
            availableSkills.append("暂无可学习的技能");
        } else {
            availableSkills.append("发送『学习技能+技能ID』学习技能");
        }
        
        return availableSkills.toString();
    }
    
    @Override
    public void initPlayerBasicSkills(Long playerId, Integer attribute) {
        // 根据属性给予基础技能
        Long basicSkillId = getBasicSkillByAttribute(attribute);
        if (basicSkillId != null) {
            try {
                LifePlayerSkill playerSkill = new LifePlayerSkill();
                playerSkill.setPlayerId(playerId);
                playerSkill.setSkillId(basicSkillId);
                playerSkill.setSkillLevel(1);
                playerSkill.setCurrentCooldown(0);
                playerSkill.setLearnTime(new Date());
                
                playerSkillMapper.insert(playerSkill);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private String getSkillTypeName(Integer type) {
        ENSkillType skillType = ENSkillType.getByCode(type);
        return skillType != null ? skillType.getDesc() : "未知类型";
    }
    
    private String getAttributeName(Integer attribute) {
        ENAttribute attr = ENAttribute.getByCode(attribute);
        return attr != null ? attr.getDesc() : "未知属性";
    }
    
    private Long getBasicSkillByAttribute(Integer attribute) {
        // 根据属性返回对应的基础技能ID
        // 这里需要根据实际的技能数据来配置
        switch (attribute) {
            case 1: return 1L; // 金属性基础技能
            case 2: return 2L; // 木属性基础技能
            case 3: return 3L; // 水属性基础技能
            case 4: return 4L; // 火属性基础技能
            case 5: return 5L; // 土属性基础技能
            default: return null;
        }
    }
}
