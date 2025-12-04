package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifePlayerSignin;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 玩家签到记录Mapper
 * @author Assistant
 */
public interface LifePlayerSigninMapper {
    
    /**
     * 插入签到记录
     */
    int insert(LifePlayerSignin record);
    
    /**
     * 根据主键查询
     */
    LifePlayerSignin selectByPrimaryKey(Long id);
    
    /**
     * 检查玩家今日是否已签到
     */
    LifePlayerSignin selectTodaySignin(@Param("playerId") Long playerId, @Param("signinDate") Date signinDate);
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifePlayerSignin record);
}
