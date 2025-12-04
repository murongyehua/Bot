package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeBattleState;
import org.apache.ibatis.annotations.Param;

/**
 * 战斗状态Mapper接口
 * @author Assistant
 */
public interface LifeBattleStateMapper {
    int deleteByPrimaryKey(Long id);
    int insert(LifeBattleState record);
    LifeBattleState selectByPrimaryKey(Long id);
    int updateByPrimaryKey(LifeBattleState record);
    
    /**
     * 根据玩家ID查询当前战斗状态
     */
    LifeBattleState selectByPlayerId(@Param("playerId") Long playerId);
    
    /**
     * 删除玩家的战斗状态
     */
    int deleteByPlayerId(@Param("playerId") Long playerId);
}
