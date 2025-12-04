package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeWorldBossReward;

import java.util.List;

/**
 * 世界BOSS奖励Mapper接口
 * @author Assistant
 */
public interface LifeWorldBossRewardMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeWorldBossReward record);
    
    /**
     * 根据主键查询
     */
    LifeWorldBossReward selectByPrimaryKey(Long id);
    
    /**
     * 根据世界BOSS ID查询奖励配置
     */
    List<LifeWorldBossReward> selectByWorldBossId(Long worldBossId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeWorldBossReward record);
}
