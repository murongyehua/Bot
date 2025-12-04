package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeFriend;

import java.util.List;

/**
 * 好友Mapper接口
 * @author Assistant
 */
public interface LifeFriendMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeFriend record);
    
    /**
     * 根据主键查询
     */
    LifeFriend selectByPrimaryKey(Long id);
    
    /**
     * 根据玩家ID和好友ID查询
     */
    LifeFriend selectByPlayerIdAndFriendId(Long playerId, Long friendId);
    
    /**
     * 获取玩家的好友列表
     */
    List<LifeFriend> selectFriendsByPlayerId(Long playerId);
    
    /**
     * 获取待处理的好友申请
     */
    List<LifeFriend> selectPendingRequestsByPlayerId(Long playerId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeFriend record);
}
