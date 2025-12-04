package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeTeam;

import java.util.List;

/**
 * 队伍Mapper接口
 * @author Assistant
 */
public interface LifeTeamMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeTeam record);
    
    /**
     * 根据主键查询
     */
    LifeTeam selectByPrimaryKey(Long id);
    
    /**
     * 根据队长ID查询队伍
     */
    LifeTeam selectByLeaderId(Long leaderId);
    
    /**
     * 查询可加入的队伍列表
     */
    List<LifeTeam> selectAvailableTeams();
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeTeam record);
}
