package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifePlayer;

import java.util.List;

/**
 * 玩家角色Mapper接口
 * @author Assistant
 */
public interface LifePlayerMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifePlayer record);
    
    /**
     * 根据主键查询
     */
    LifePlayer selectByPrimaryKey(Long id);
    
    /**
     * 根据用户ID查询
     */
    LifePlayer selectByUserId(String userId);
    
    /**
     * 根据昵称查询
     */
    LifePlayer selectByNickname(String nickname);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifePlayer record);
    
    /**
     * 查询所有玩家
     */
    List<LifePlayer> selectAllPlayers();
}
