package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.PlayerPhantom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlayerPhantomMapper {
    int deleteByPrimaryKey(String id);

    int insert(PlayerPhantom record);

    int insertSelective(PlayerPhantom record);

    PlayerPhantom selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PlayerPhantom record);

    int updateByPrimaryKey(PlayerPhantom record);

    List<PlayerPhantom> selectBySelective(PlayerPhantom record);

    Integer getMaxLevel(@Param("playerId") String token);
}