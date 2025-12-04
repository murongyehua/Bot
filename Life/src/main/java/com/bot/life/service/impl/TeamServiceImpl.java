package com.bot.life.service.impl;

import com.bot.life.dao.entity.*;
import com.bot.life.dao.mapper.*;
import com.bot.life.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 组队服务实现
 * @author Assistant
 */
@Service
public class TeamServiceImpl implements TeamService {
    
    @Autowired
    private LifeTeamMapper teamMapper;
    
    @Autowired
    private LifeTeamMemberMapper teamMemberMapper;
    
    @Autowired
    private LifeDungeonMapper dungeonMapper;
    
    @Override
    public String createTeam(LifePlayer leader, String teamName) {
        // 检查玩家是否已经在队伍中
        if (isPlayerInTeam(leader.getId())) {
            return "你已经在队伍中了！请先离开当前队伍。";
        }
        
        try {
            // 创建队伍
            LifeTeam team = new LifeTeam();
            team.setLeaderId(leader.getId());
            team.setTeamName(teamName);
            team.setMaxMembers(2); // 最多2人队伍
            team.setCurrentMembers(1);
            team.setTeamStatus(0); // 待组队
            team.setCreateTime(new Date());
            team.setUpdateTime(new Date());
            
            teamMapper.insert(team);
            
            // 队长自动加入队伍
            LifeTeamMember leaderMember = new LifeTeamMember();
            leaderMember.setTeamId(team.getId());
            leaderMember.setPlayerId(leader.getId());
            leaderMember.setMemberStatus(1); // 已同意
            leaderMember.setJoinTime(new Date());
            
            teamMemberMapper.insert(leaderMember);
            
            return String.format("『队伍创建成功！』\n\n队伍名称：%s\n队长：%s\n当前人数：1/2\n\n其他玩家可通过队伍ID %d 申请加入", 
                               teamName, leader.getNickname(), team.getId());
            
        } catch (Exception e) {
            e.printStackTrace();
            return "创建队伍失败！";
        }
    }
    
    @Override
    public String joinTeam(LifePlayer player, Long teamId) {
        // 检查玩家是否已经在队伍中
        if (isPlayerInTeam(player.getId())) {
            return "你已经在队伍中了！";
        }
        
        LifeTeam team = teamMapper.selectByPrimaryKey(teamId);
        if (team == null) {
            return "队伍不存在！";
        }
        
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            return "队伍已满员！";
        }
        
        if (team.getTeamStatus() != 0) {
            return "队伍不在招募状态！";
        }
        
        // 检查是否已经申请过
        LifeTeamMember existingMember = teamMemberMapper.selectByTeamIdAndPlayerId(teamId, player.getId());
        if (existingMember != null) {
            if (existingMember.getMemberStatus() == 0) {
                return "你已经申请过加入该队伍，请等待队长处理！";
            } else {
                return "你已经是该队伍成员！";
            }
        }
        
        try {
            // 创建申请记录
            LifeTeamMember member = new LifeTeamMember();
            member.setTeamId(teamId);
            member.setPlayerId(player.getId());
            member.setMemberStatus(0); // 申请中
            member.setJoinTime(new Date());
            
            teamMemberMapper.insert(member);
            
            return String.format("『申请已发送！』\n\n已向队伍『%s』发送加入申请\n等待队长『%s』处理", 
                               team.getTeamName(), 
                               team.getLeader() != null ? team.getLeader().getNickname() : "未知");
            
        } catch (Exception e) {
            e.printStackTrace();
            return "申请加入队伍失败！";
        }
    }
    
    @Override
    public String acceptTeamMember(LifePlayer leader, Long playerId) {
        LifeTeam team = getTeamByLeader(leader.getId());
        if (team == null) {
            return "你不是任何队伍的队长！";
        }
        
        LifeTeamMember member = teamMemberMapper.selectByTeamIdAndPlayerId(team.getId(), playerId);
        if (member == null || member.getMemberStatus() != 0) {
            return "没有找到该玩家的申请！";
        }
        
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            return "队伍已满员！";
        }
        
        try {
            // 同意申请
            member.setMemberStatus(1);
            teamMemberMapper.updateByPrimaryKey(member);
            
            // 更新队伍人数
            team.setCurrentMembers(team.getCurrentMembers() + 1);
            if (team.getCurrentMembers() >= team.getMaxMembers()) {
                team.setTeamStatus(1); // 已满员
            }
            team.setUpdateTime(new Date());
            teamMapper.updateByPrimaryKey(team);
            
            // TODO: 可以发送系统邮件通知被接受的玩家
            
            return String.format("『同意成功！』\n\n玩家加入队伍\n当前人数：%d/%d", 
                               team.getCurrentMembers(), team.getMaxMembers());
            
        } catch (Exception e) {
            e.printStackTrace();
            return "处理申请失败！";
        }
    }
    
    @Override
    public String rejectTeamMember(LifePlayer leader, Long playerId) {
        LifeTeam team = getTeamByLeader(leader.getId());
        if (team == null) {
            return "你不是任何队伍的队长！";
        }
        
        LifeTeamMember member = teamMemberMapper.selectByTeamIdAndPlayerId(team.getId(), playerId);
        if (member == null || member.getMemberStatus() != 0) {
            return "没有找到该玩家的申请！";
        }
        
        try {
            // 删除申请记录
            teamMemberMapper.deleteByPrimaryKey(member.getId());
            
            // TODO: 可以发送系统邮件通知被拒绝的玩家
            
            return "『已拒绝申请』";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "处理申请失败！";
        }
    }
    
    @Override
    public String leaveTeam(LifePlayer player) {
        LifeTeamMember member = getPlayerTeamMember(player.getId());
        if (member == null) {
            return "你不在任何队伍中！";
        }
        
        LifeTeam team = teamMapper.selectByPrimaryKey(member.getTeamId());
        if (team == null) {
            return "队伍信息异常！";
        }
        
        // 如果是队长离开，解散队伍
        if (team.getLeaderId().equals(player.getId())) {
            return disbandTeam(player);
        }
        
        try {
            // 删除成员记录
            teamMemberMapper.deleteByPrimaryKey(member.getId());
            
            // 更新队伍人数
            team.setCurrentMembers(team.getCurrentMembers() - 1);
            team.setTeamStatus(0); // 重新开放招募
            team.setUpdateTime(new Date());
            teamMapper.updateByPrimaryKey(team);
            
            return "『离开队伍成功！』";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "离开队伍失败！";
        }
    }
    
    @Override
    public String disbandTeam(LifePlayer leader) {
        LifeTeam team = getTeamByLeader(leader.getId());
        if (team == null) {
            return "你不是任何队伍的队长！";
        }
        
        try {
            // 删除所有成员记录
            teamMemberMapper.deleteByTeamId(team.getId());
            
            // 删除队伍
            teamMapper.deleteByPrimaryKey(team.getId());
            
            // TODO: 可以发送系统邮件通知所有成员队伍解散
            
            return "『队伍解散成功！』";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "解散队伍失败！";
        }
    }
    
    @Override
    public String getTeamInfo(LifePlayer player) {
        LifeTeamMember member = getPlayerTeamMember(player.getId());
        if (member == null) {
            return "『组队系统』\n\n你当前不在任何队伍中\n\n发送『创建队伍+队伍名称』创建队伍\n发送『查看队伍』查看可加入的队伍";
        }
        
        LifeTeam team = teamMapper.selectByPrimaryKey(member.getTeamId());
        if (team == null) {
            return "队伍信息异常！";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("『队伍信息』\n\n");
        info.append(String.format("队伍名称：%s\n", team.getTeamName()));
        info.append(String.format("队伍ID：%d\n", team.getId()));
        info.append(String.format("人数：%d/%d\n", team.getCurrentMembers(), team.getMaxMembers()));
        info.append(String.format("状态：%s\n\n", getTeamStatusName(team.getTeamStatus())));
        
        // 显示成员列表
        List<LifeTeamMember> members = teamMemberMapper.selectByTeamId(team.getId());
        info.append("『队伍成员』\n");
        
        for (LifeTeamMember teamMember : members) {
            if (teamMember.getMemberStatus() == 1) {
                String role = teamMember.getPlayerId().equals(team.getLeaderId()) ? "（队长）" : "";
                info.append(String.format("• %s%s\n", 
                           teamMember.getPlayer() != null ? teamMember.getPlayer().getNickname() : "未知", 
                           role));
            }
        }
        
        // 显示待处理申请（仅队长可见）
        if (team.getLeaderId().equals(player.getId())) {
            List<LifeTeamMember> pendingMembers = teamMemberMapper.selectPendingMembersByTeamId(team.getId());
            if (!pendingMembers.isEmpty()) {
                info.append("\n『待处理申请』\n");
                for (LifeTeamMember pendingMember : pendingMembers) {
                    info.append(String.format("• %s（发送『同意队员%d』或『拒绝队员%d』）\n", 
                               pendingMember.getPlayer() != null ? pendingMember.getPlayer().getNickname() : "未知",
                               pendingMember.getPlayerId(), pendingMember.getPlayerId()));
                }
            }
        }
        
        info.append("\n发送『离开队伍』离开队伍");
        if (team.getLeaderId().equals(player.getId())) {
            info.append("\n发送『解散队伍』解散队伍");
            info.append("\n发送『挑战副本+副本ID』挑战副本");
        }
        
        return info.toString();
    }
    
    @Override
    public String getAvailableTeams(LifePlayer player) {
        List<LifeTeam> availableTeams = teamMapper.selectAvailableTeams();
        
        if (availableTeams.isEmpty()) {
            return "『可加入队伍』\n\n当前没有可加入的队伍\n\n发送『创建队伍+队伍名称』创建新队伍";
        }
        
        StringBuilder teamList = new StringBuilder();
        teamList.append("『可加入队伍』\n\n");
        
        for (LifeTeam team : availableTeams) {
            teamList.append(String.format("ID:%d 『%s』\n", team.getId(), team.getTeamName()));
            teamList.append(String.format("队长：%s\n", 
                           team.getLeader() != null ? team.getLeader().getNickname() : "未知"));
            teamList.append(String.format("人数：%d/%d\n\n", team.getCurrentMembers(), team.getMaxMembers()));
        }
        
        teamList.append("发送『加入队伍+队伍ID』申请加入队伍");
        
        return teamList.toString();
    }
    
    @Override
    public String challengeDungeon(LifePlayer leader, Long dungeonId) {
        LifeTeam team = getTeamByLeader(leader.getId());
        if (team == null) {
            return "你不是队长！";
        }
        
        if (team.getTeamStatus() != 1) {
            return "队伍未满员，无法挑战副本！";
        }
        
        LifeDungeon dungeon = dungeonMapper.selectByPrimaryKey(dungeonId);
        if (dungeon == null) {
            return "副本不存在！";
        }
        
        if (dungeon.getIsActive() != 1) {
            return "该副本暂未开放！";
        }
        
        // TODO: 检查队员等级要求等
        
        try {
            // 更新队伍状态为副本中
            team.setTeamStatus(2);
            team.setDungeonId(dungeonId);
            team.setUpdateTime(new Date());
            teamMapper.updateByPrimaryKey(team);
            
            // TODO: 实现副本战斗逻辑
            
            return String.format("『进入副本』\n\n队伍进入『%s』副本\n\n副本功能开发中...", dungeon.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
            return "进入副本失败！";
        }
    }
    
    private boolean isPlayerInTeam(Long playerId) {
        return getPlayerTeamMember(playerId) != null;
    }
    
    private LifeTeamMember getPlayerTeamMember(Long playerId) {
        return teamMemberMapper.selectByPlayerId(playerId);
    }
    
    private LifeTeam getTeamByLeader(Long leaderId) {
        return teamMapper.selectByLeaderId(leaderId);
    }
    
    private String getTeamStatusName(Integer status) {
        switch (status) {
            case 0: return "招募中";
            case 1: return "已满员";
            case 2: return "副本中";
            default: return "未知";
        }
    }
}
