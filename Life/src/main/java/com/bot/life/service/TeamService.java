package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;

/**
 * 组队服务接口
 * @author Assistant
 */
public interface TeamService {
    
    /**
     * 创建队伍
     * @param leader 队长
     * @param teamName 队伍名称
     * @return 创建结果
     */
    String createTeam(LifePlayer leader, String teamName);
    
    /**
     * 申请加入队伍
     * @param player 申请者
     * @param teamId 队伍ID
     * @return 申请结果
     */
    String joinTeam(LifePlayer player, Long teamId);
    
    /**
     * 同意队员申请
     * @param leader 队长
     * @param playerId 申请者ID
     * @return 处理结果
     */
    String acceptTeamMember(LifePlayer leader, Long playerId);
    
    /**
     * 拒绝队员申请
     * @param leader 队长
     * @param playerId 申请者ID
     * @return 处理结果
     */
    String rejectTeamMember(LifePlayer leader, Long playerId);
    
    /**
     * 离开队伍
     * @param player 玩家
     * @return 结果
     */
    String leaveTeam(LifePlayer player);
    
    /**
     * 解散队伍
     * @param leader 队长
     * @return 结果
     */
    String disbandTeam(LifePlayer leader);
    
    /**
     * 查看队伍信息
     * @param player 玩家
     * @return 队伍信息
     */
    String getTeamInfo(LifePlayer player);
    
    /**
     * 获取可加入的队伍列表
     * @param player 玩家
     * @return 队伍列表
     */
    String getAvailableTeams(LifePlayer player);
    
    /**
     * 挑战副本
     * @param leader 队长
     * @param dungeonId 副本ID
     * @return 挑战结果
     */
    String challengeDungeon(LifePlayer leader, Long dungeonId);
}
