package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 邮件实体
 * @author Assistant
 */
@Data
public class LifeMail {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private Integer mailType; // 邮件类型：1系统邮件2好友邮件
    private Integer hasAttachment; // 是否有附件：0无1有
    private String attachmentData; // 附件数据（JSON格式）
    private Integer isRead; // 是否已读：0未读1已读
    private Integer isReceived; // 附件是否已领取：0未领取1已领取
    private Date sendTime;
    private Date readTime;
    private Date receiveTime;
    
    // 关联对象
    private LifePlayer sender;
    private LifePlayer receiver;
}
