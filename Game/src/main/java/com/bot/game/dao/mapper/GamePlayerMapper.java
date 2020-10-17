package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.GamePlayer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GamePlayerMapper {
    int deleteByPrimaryKey(String id);

    int insert(GamePlayer record);

    int insertSelective(GamePlayer record);

    GamePlayer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(GamePlayer record);

    int updateByPrimaryKey(GamePlayer record);

    List<GamePlayer> selectBySelective(GamePlayer gamePlayer);

    List<GamePlayer> getByIds(@Param("ids") List<String> ids);
}