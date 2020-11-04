package com.bot.game.dao.mapper;


import com.bot.game.dao.entity.PlayerWeapon;

import java.util.List;

public interface PlayerWeaponMapper {
    int deleteByPrimaryKey(String id);

    int insert(PlayerWeapon record);

    int insertSelective(PlayerWeapon record);

    PlayerWeapon selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PlayerWeapon record);

    int updateByPrimaryKey(PlayerWeapon record);

    List<PlayerWeapon> selectBySelective(PlayerWeapon record);
}