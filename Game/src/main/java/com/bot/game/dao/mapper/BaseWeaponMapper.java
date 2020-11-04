package com.bot.game.dao.mapper;


import com.bot.game.dao.entity.BaseWeapon;

import java.util.List;

public interface BaseWeaponMapper {
    int deleteByPrimaryKey(String id);

    int insert(BaseWeapon record);

    int insertSelective(BaseWeapon record);

    BaseWeapon selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BaseWeapon record);

    int updateByPrimaryKey(BaseWeapon record);

}