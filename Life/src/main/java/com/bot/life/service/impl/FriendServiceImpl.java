package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifeFriend;
import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.mapper.LifeFriendMapper;
import com.bot.life.service.FriendService;
import com.bot.life.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 好友服务实现
 * @author Assistant
 */
@Service
public class FriendServiceImpl implements FriendService {
    
    @Autowired
    private LifeFriendMapper friendMapper;
    
    @Autowired
    private PlayerService playerService;
    
    @Override
    public String addFriend(LifePlayer player, String friendNickname) {
        // 不能添加自己为好友
        if (player.getNickname().equals(friendNickname)) {
            return "不能添加自己为好友！";
        }
        
        // 查找目标玩家
        LifePlayer targetPlayer = playerService.getPlayerByNickname(friendNickname);
        if (targetPlayer == null) {
            return String.format("玩家『%s』不存在！", friendNickname);
        }
        
        // 检查是否已经是好友
        if (friendMapper.selectByPlayerIdAndFriendId(player.getId(), targetPlayer.getId()) != null) {
            return String.format("『%s』已经是你的好友了！", friendNickname);
        }
        
        // 检查是否已经发送过申请
        if (friendMapper.selectByPlayerIdAndFriendId(targetPlayer.getId(), player.getId()) != null) {
            return String.format("已向『%s』发送过好友申请！", friendNickname);
        }
        
        // 创建好友申请
        try {
            LifeFriend friendRequest = new LifeFriend();
            friendRequest.setPlayerId(targetPlayer.getId()); // 被申请者
            friendRequest.setFriendId(player.getId()); // 申请者
            friendRequest.setStatus(0); // 待确认
            friendRequest.setCreateTime(new Date());
            friendRequest.setUpdateTime(new Date());
            
            friendMapper.insert(friendRequest);
            
            return String.format("『好友申请已发送！』\n\n已向『%s』发送好友申请，等待对方确认。", friendNickname);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "发送好友申请失败！";
        }
    }
    
    @Override
    public String acceptFriend(LifePlayer player, Long friendId) {
        // 查找好友申请
        LifeFriend friendRequest = friendMapper.selectByPlayerIdAndFriendId(player.getId(), friendId);
        if (friendRequest == null || friendRequest.getStatus() != 0) {
            return "好友申请不存在或已处理！";
        }
        
        try {
            // 更新申请状态
            friendRequest.setStatus(1);
            friendRequest.setUpdateTime(new Date());
            friendMapper.updateByPrimaryKey(friendRequest);
            
            // 创建反向好友关系
            LifeFriend reverseFriend = new LifeFriend();
            reverseFriend.setPlayerId(friendId);
            reverseFriend.setFriendId(player.getId());
            reverseFriend.setStatus(1);
            reverseFriend.setCreateTime(new Date());
            reverseFriend.setUpdateTime(new Date());
            friendMapper.insert(reverseFriend);
            
            LifePlayer friendPlayer = playerService.getPlayerById(friendId);
            return String.format("『好友添加成功！』\n\n与『%s』成为了好友！", 
                               friendPlayer != null ? friendPlayer.getNickname() : "未知玩家");
            
        } catch (Exception e) {
            e.printStackTrace();
            return "处理好友申请失败！";
        }
    }
    
    @Override
    public String rejectFriend(LifePlayer player, Long friendId) {
        // 查找好友申请
        LifeFriend friendRequest = friendMapper.selectByPlayerIdAndFriendId(player.getId(), friendId);
        if (friendRequest == null || friendRequest.getStatus() != 0) {
            return "好友申请不存在或已处理！";
        }
        
        try {
            // 删除申请
            friendMapper.deleteByPrimaryKey(friendRequest.getId());
            
            LifePlayer friendPlayer = playerService.getPlayerById(friendId);
            return String.format("『已拒绝好友申请』\n\n已拒绝『%s』的好友申请。", 
                               friendPlayer != null ? friendPlayer.getNickname() : "未知玩家");
            
        } catch (Exception e) {
            e.printStackTrace();
            return "处理好友申请失败！";
        }
    }
    
    @Override
    public String deleteFriend(LifePlayer player, Long friendId) {
        // TODO: 实现删除好友功能
        return "删除好友功能开发中...";
    }
    
    @Override
    public String getFriendListDisplay(LifePlayer player) {
        List<LifeFriend> friends = friendMapper.selectFriendsByPlayerId(player.getId());
        
        if (friends.isEmpty()) {
            return "『好友列表』\n\n暂无好友\n\n发送『添加好友+昵称』来添加好友\n例如：添加好友张三";
        }
        
        StringBuilder display = new StringBuilder();
        display.append("『").append(player.getNickname()).append("的好友』\n\n");
        
        for (int i = 0; i < friends.size(); i++) {
            LifeFriend friend = friends.get(i);
            LifePlayer friendPlayer = playerService.getPlayerById(friend.getFriendId());
            if (friendPlayer != null) {
                display.append(String.format("%d. %s（%s级）\n", 
                             i + 1, friendPlayer.getNickname(), friendPlayer.getLevel()));
            }
        }
        
        display.append("\n发送『添加好友+昵称』添加新好友");
        
        return display.toString();
    }
    
    @Override
    public String getPendingFriendRequests(LifePlayer player) {
        List<LifeFriend> requests = friendMapper.selectPendingRequestsByPlayerId(player.getId());
        
        if (requests.isEmpty()) {
            return "『好友申请』\n\n暂无待处理的好友申请";
        }
        
        StringBuilder display = new StringBuilder();
        display.append("『待处理好友申请』\n\n");
        
        for (LifeFriend request : requests) {
            LifePlayer requester = playerService.getPlayerById(request.getFriendId());
            if (requester != null) {
                display.append(String.format("『%s』想要添加你为好友\n", requester.getNickname()));
                display.append(String.format("发送『同意好友%d』或『拒绝好友%d』\n\n", 
                             requester.getId(), requester.getId()));
            }
        }
        
        return display.toString();
    }
}
