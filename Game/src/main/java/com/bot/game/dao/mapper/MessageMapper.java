package com.bot.game.dao.mapper;


import com.bot.game.dao.entity.Message;

import java.util.List;

public interface MessageMapper {

    int deleteByPrimaryKey(String id);

    int insert(Message record);

    int insertSelective(Message record);

    Message selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Message record);

    int updateByPrimaryKey(Message record);

    List<Message> selectBySelective(Message record);
}