package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.PlayerFriends;

public interface PlayerFriendsMapper {
    int deleteByPrimaryKey(String id);

    int insert(PlayerFriends record);

    int insertSelective(PlayerFriends record);

    PlayerFriends selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PlayerFriends record);

    int updateByPrimaryKey(PlayerFriends record);
}