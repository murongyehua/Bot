package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;

/**
 * 好友服务接口
 * @author Assistant
 */
public interface FriendService {
    
    /**
     * 添加好友
     * @param player 玩家信息
     * @param friendNickname 好友昵称
     * @return 添加结果描述
     */
    String addFriend(LifePlayer player, String friendNickname);
    
    /**
     * 同意好友申请
     * @param player 玩家信息
     * @param friendId 好友ID
     * @return 结果描述
     */
    String acceptFriend(LifePlayer player, Long friendId);
    
    /**
     * 拒绝好友申请
     * @param player 玩家信息
     * @param friendId 好友ID
     * @return 结果描述
     */
    String rejectFriend(LifePlayer player, Long friendId);
    
    /**
     * 删除好友
     * @param player 玩家信息
     * @param friendId 好友ID
     * @return 结果描述
     */
    String deleteFriend(LifePlayer player, Long friendId);
    
    /**
     * 获取好友列表显示
     * @param player 玩家信息
     * @return 好友列表描述
     */
    String getFriendListDisplay(LifePlayer player);
    
    /**
     * 获取待处理的好友申请
     * @param player 玩家信息
     * @return 申请列表描述
     */
    String getPendingFriendRequests(LifePlayer player);
}
