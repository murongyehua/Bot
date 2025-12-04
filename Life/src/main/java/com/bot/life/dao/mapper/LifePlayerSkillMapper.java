package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifePlayerSkill;

import java.util.List;

/**
 * 玩家技能Mapper接口
 * @author Assistant
 */
public interface LifePlayerSkillMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifePlayerSkill record);
    
    /**
     * 根据主键查询
     */
    LifePlayerSkill selectByPrimaryKey(Long id);
    
    /**
     * 根据玩家ID查询所有技能
     */
    List<LifePlayerSkill> selectByPlayerId(Long playerId);
    
    /**
     * 根据玩家ID和技能ID查询
     */
    LifePlayerSkill selectByPlayerIdAndSkillId(Long playerId, Long skillId);
    
    /**
     * 根据玩家ID和技能ID查询（用于检查是否已学会）
     */
    LifePlayerSkill selectByPlayerAndSkillId(Long playerId, Long skillId);
    
    /**
     * 根据玩家ID查询可用技能（冷却完成）
     */
    List<LifePlayerSkill> selectAvailableSkillsByPlayerId(Long playerId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifePlayerSkill record);
}
