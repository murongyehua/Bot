package com.bot.life.service;

import com.bot.life.dao.entity.LifePlayer;

/**
 * 邮件服务接口
 * @author Assistant
 */
public interface MailService {
    
    /**
     * 获取邮件主界面
     * @param player 玩家信息
     * @return 邮件界面描述
     */
    String getMailMainMenu(LifePlayer player);
    
    /**
     * 发送邮件给好友
     * @param sender 发送者
     * @param receiverNickname 接收者昵称
     * @param title 邮件标题
     * @param content 邮件内容
     * @return 发送结果
     */
    String sendMailToFriend(LifePlayer sender, String receiverNickname, String title, String content);
    
    /**
     * 发送带道具的邮件
     * @param sender 发送者
     * @param receiverNickname 接收者昵称
     * @param title 邮件标题
     * @param content 邮件内容
     * @param itemId 道具ID
     * @param quantity 道具数量
     * @return 发送结果
     */
    String sendMailWithItem(LifePlayer sender, String receiverNickname, String title, 
                          String content, Long itemId, Integer quantity);
    
    /**
     * 读取邮件
     * @param player 玩家信息
     * @param mailId 邮件ID
     * @return 邮件内容
     */
    String readMail(LifePlayer player, Long mailId);
    
    /**
     * 领取邮件附件
     * @param player 玩家信息
     * @param mailId 邮件ID
     * @return 领取结果
     */
    String receiveMailAttachment(LifePlayer player, Long mailId);
    
    /**
     * 删除邮件
     * @param player 玩家信息
     * @param mailId 邮件ID
     * @return 删除结果
     */
    String deleteMail(LifePlayer player, Long mailId);
    
    /**
     * 发送系统邮件
     * @param receiverId 接收者ID
     * @param title 标题
     * @param content 内容
     * @param attachmentData 附件数据
     * @return 是否成功
     */
    boolean sendSystemMail(Long receiverId, String title, String content, String attachmentData);
}
