package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.PlayerAppellation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlayerAppellationMapper {
    int deleteByPrimaryKey(String id);

    int deleteByAppellation(@Param("appellation") String appellation);

    int insert(PlayerAppellation record);

    int insertSelective(PlayerAppellation record);

    PlayerAppellation selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PlayerAppellation record);

    int updateByPrimaryKey(PlayerAppellation record);

    List<PlayerAppellation> selectBySelective(PlayerAppellation record);
}