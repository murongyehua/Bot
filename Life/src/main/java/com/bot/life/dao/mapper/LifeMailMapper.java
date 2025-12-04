package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeMail;

import java.util.List;

/**
 * 邮件Mapper接口
 * @author Assistant
 */
public interface LifeMailMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeMail record);
    
    /**
     * 根据主键查询
     */
    LifeMail selectByPrimaryKey(Long id);
    
    /**
     * 根据接收者ID查询邮件列表
     */
    List<LifeMail> selectByReceiverId(Long receiverId);
    
    /**
     * 根据发送者ID查询邮件列表
     */
    List<LifeMail> selectBySenderId(Long senderId);
    
    /**
     * 查询未读邮件数量
     */
    int selectUnreadCountByReceiverId(Long receiverId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeMail record);
}
