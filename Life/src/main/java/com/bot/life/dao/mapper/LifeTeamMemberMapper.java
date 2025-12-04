package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeTeamMember;

import java.util.List;

/**
 * 队伍成员Mapper接口
 * @author Assistant
 */
public interface LifeTeamMemberMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeTeamMember record);
    
    /**
     * 根据主键查询
     */
    LifeTeamMember selectByPrimaryKey(Long id);
    
    /**
     * 根据队伍ID查询所有成员
     */
    List<LifeTeamMember> selectByTeamId(Long teamId);
    
    /**
     * 根据玩家ID查询队伍成员信息
     */
    LifeTeamMember selectByPlayerId(Long playerId);
    
    /**
     * 根据队伍ID和玩家ID查询
     */
    LifeTeamMember selectByTeamIdAndPlayerId(Long teamId, Long playerId);
    
    /**
     * 查询队伍的待处理申请
     */
    List<LifeTeamMember> selectPendingMembersByTeamId(Long teamId);
    
    /**
     * 根据队伍ID删除所有成员
     */
    int deleteByTeamId(Long teamId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeTeamMember record);
}
