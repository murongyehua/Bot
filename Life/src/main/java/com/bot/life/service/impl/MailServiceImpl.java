package com.bot.life.service.impl;

import com.bot.life.dao.entity.*;
import com.bot.life.dao.mapper.*;
import com.bot.life.service.InventoryService;
import com.bot.life.service.MailService;
import com.bot.life.service.PlayerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件服务实现
 * @author Assistant
 */
@Service
public class MailServiceImpl implements MailService {
    
    @Autowired
    private LifeMailMapper mailMapper;
    
    @Autowired
    private LifeFriendMapper friendMapper;
    
    @Autowired
    private LifeItemMapper itemMapper;
    
    @Autowired
    private LifePlayerItemMapper playerItemMapper;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private InventoryService inventoryService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String getMailMainMenu(LifePlayer player) {
        List<LifeMail> mails = mailMapper.selectByReceiverId(player.getId());
        int unreadCount = mailMapper.selectUnreadCountByReceiverId(player.getId());
        
        StringBuilder menu = new StringBuilder();
        menu.append("『邮件系统』\n\n");
        menu.append(String.format("未读邮件：%d封\n", unreadCount));
        menu.append(String.format("总邮件：%d封\n\n", mails.size()));
        
        if (mails.isEmpty()) {
            menu.append("暂无邮件\n\n");
        } else {
            menu.append("『邮件列表』\n");
            for (int i = 0; i < Math.min(mails.size(), 10); i++) {
                LifeMail mail = mails.get(i);
                String status = mail.getIsRead() == 0 ? "[未读]" : "[已读]";
                String attachment = mail.getHasAttachment() == 1 ? "[附件]" : "";
                String senderName = mail.getSender() != null ? mail.getSender().getNickname() : "系统";
                
                menu.append(String.format("%d. %s%s %s - %s\n", 
                           mail.getId(), status, attachment, senderName, mail.getTitle()));
            }
            
            if (mails.size() > 10) {
                menu.append(String.format("...还有%d封邮件\n", mails.size() - 10));
            }
            menu.append("\n");
        }
        
        menu.append("『操作指令』\n");
        menu.append("发送『读取邮件+邮件ID』查看邮件\n");
        menu.append("发送『领取附件+邮件ID』领取附件\n");
        menu.append("发送『发送邮件+好友昵称+标题+内容』发送邮件\n");
        menu.append("发送『删除邮件+邮件ID』删除邮件\n\n");
        menu.append("例如：读取邮件1");
        
        return menu.toString();
    }
    
    @Override
    public String sendMailToFriend(LifePlayer sender, String receiverNickname, String title, String content) {
        // 检查接收者是否存在
        LifePlayer receiver = playerService.getPlayerByNickname(receiverNickname);
        if (receiver == null) {
            return String.format("玩家『%s』不存在！", receiverNickname);
        }
        
        // 检查是否为好友
        LifeFriend friendship = friendMapper.selectByPlayerIdAndFriendId(sender.getId(), receiver.getId());
        if (friendship == null || friendship.getStatus() != 1) {
            return String.format("『%s』不是你的好友，无法发送邮件！", receiverNickname);
        }
        
        try {
            LifeMail mail = new LifeMail();
            mail.setSenderId(sender.getId());
            mail.setReceiverId(receiver.getId());
            mail.setTitle(title);
            mail.setContent(content);
            mail.setMailType(2); // 好友邮件
            mail.setHasAttachment(0);
            mail.setIsRead(0);
            mail.setIsReceived(0);
            mail.setSendTime(new Date());
            
            mailMapper.insert(mail);
            
            return String.format("『邮件发送成功！』\n\n已向『%s』发送邮件\n标题：%s", receiverNickname, title);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "邮件发送失败！";
        }
    }
    
    @Override
    public String sendMailWithItem(LifePlayer sender, String receiverNickname, String title, 
                                 String content, Long itemId, Integer quantity) {
        // 检查接收者是否存在
        LifePlayer receiver = playerService.getPlayerByNickname(receiverNickname);
        if (receiver == null) {
            return String.format("玩家『%s』不存在！", receiverNickname);
        }
        
        // 检查是否为好友
        LifeFriend friendship = friendMapper.selectByPlayerIdAndFriendId(sender.getId(), receiver.getId());
        if (friendship == null || friendship.getStatus() != 1) {
            return String.format("『%s』不是你的好友，无法发送邮件！", receiverNickname);
        }
        
        // 检查道具是否存在
        LifePlayerItem playerItem = playerItemMapper.selectByPlayerIdAndItemId(sender.getId(), itemId);
        if (playerItem == null || playerItem.getQuantity() < quantity) {
            return "你没有足够的该道具！";
        }
        
        LifeItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            return "道具不存在！";
        }
        
        try {
            // 创建附件数据
            Map<String, Object> attachment = new HashMap<>();
            attachment.put("type", "item");
            attachment.put("itemId", itemId);
            attachment.put("quantity", quantity);
            attachment.put("itemName", item.getName());
            
            String attachmentJson = objectMapper.writeValueAsString(attachment);
            
            // 创建邮件
            LifeMail mail = new LifeMail();
            mail.setSenderId(sender.getId());
            mail.setReceiverId(receiver.getId());
            mail.setTitle(title);
            mail.setContent(content);
            mail.setMailType(2); // 好友邮件
            mail.setHasAttachment(1);
            mail.setAttachmentData(attachmentJson);
            mail.setIsRead(0);
            mail.setIsReceived(0);
            mail.setSendTime(new Date());
            
            mailMapper.insert(mail);
            
            // 扣除发送者道具
            playerItem.setQuantity(playerItem.getQuantity() - quantity);
            playerItem.setUpdateTime(new Date());
            playerItemMapper.updateByPrimaryKey(playerItem);
            
            return String.format("『邮件发送成功！』\n\n已向『%s』发送邮件\n标题：%s\n附件：%s x%d", 
                               receiverNickname, title, item.getName(), quantity);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "邮件发送失败！";
        }
    }
    
    @Override
    public String readMail(LifePlayer player, Long mailId) {
        LifeMail mail = mailMapper.selectByPrimaryKey(mailId);
        if (mail == null) {
            return "邮件不存在！";
        }
        
        if (!mail.getReceiverId().equals(player.getId())) {
            return "这不是你的邮件！";
        }
        
        // 标记为已读
        if (mail.getIsRead() == 0) {
            mail.setIsRead(1);
            mail.setReadTime(new Date());
            mailMapper.updateByPrimaryKey(mail);
        }
        
        StringBuilder mailContent = new StringBuilder();
        mailContent.append("『邮件详情』\n\n");
        
        // 发送者信息
        if (mail.getMailType() == 1) {
            mailContent.append("发送者：系统\n");
        } else {
            LifePlayer sender = playerService.getPlayerById(mail.getSenderId());
            mailContent.append(String.format("发送者：%s\n", sender != null ? sender.getNickname() : "未知"));
        }
        
        mailContent.append(String.format("标题：%s\n", mail.getTitle()));
        mailContent.append(String.format("时间：%s\n\n", formatDate(mail.getSendTime())));
        mailContent.append("『邮件内容』\n");
        mailContent.append(mail.getContent()).append("\n\n");
        
        // 附件信息
        if (mail.getHasAttachment() == 1) {
            try {
                Map<String, Object> attachment = objectMapper.readValue(
                    mail.getAttachmentData(), new TypeReference<Map<String, Object>>() {});
                
                mailContent.append("『附件』\n");
                if ("item".equals(attachment.get("type"))) {
                    mailContent.append(String.format("%s x%s", 
                                     attachment.get("itemName"), attachment.get("quantity")));
                }
                
                if (mail.getIsReceived() == 0) {
                    mailContent.append("\n\n发送『领取附件").append(mailId).append("』领取附件");
                } else {
                    mailContent.append("\n（已领取）");
                }
                
            } catch (Exception e) {
                mailContent.append("附件数据异常");
            }
        }
        
        return mailContent.toString();
    }
    
    @Override
    public String receiveMailAttachment(LifePlayer player, Long mailId) {
        LifeMail mail = mailMapper.selectByPrimaryKey(mailId);
        if (mail == null) {
            return "邮件不存在！";
        }
        
        if (!mail.getReceiverId().equals(player.getId())) {
            return "这不是你的邮件！";
        }
        
        if (mail.getHasAttachment() == 0) {
            return "该邮件没有附件！";
        }
        
        if (mail.getIsReceived() == 1) {
            return "附件已经领取过了！";
        }
        
        try {
            Map<String, Object> attachment = objectMapper.readValue(
                mail.getAttachmentData(), new TypeReference<Map<String, Object>>() {});
            
            if ("item".equals(attachment.get("type"))) {
                Long itemId = Long.valueOf(attachment.get("itemId").toString());
                Integer quantity = Integer.valueOf(attachment.get("quantity").toString());
                String itemName = attachment.get("itemName").toString();
                
                // 添加道具到背包
                boolean success = inventoryService.addItem(player.getId(), itemId, quantity);
                
                if (success) {
                    // 标记附件已领取
                    mail.setIsReceived(1);
                    mail.setReceiveTime(new Date());
                    mailMapper.updateByPrimaryKey(mail);
                    
                    return String.format("『附件领取成功！』\n\n获得道具：%s x%d", itemName, quantity);
                } else {
                    return "领取失败，请重试！";
                }
            }
            
            return "未知的附件类型！";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "附件领取失败！";
        }
    }
    
    @Override
    public String deleteMail(LifePlayer player, Long mailId) {
        LifeMail mail = mailMapper.selectByPrimaryKey(mailId);
        if (mail == null) {
            return "邮件不存在！";
        }
        
        if (!mail.getReceiverId().equals(player.getId())) {
            return "这不是你的邮件！";
        }
        
        // 检查是否有未领取的附件
        if (mail.getHasAttachment() == 1 && mail.getIsReceived() == 0) {
            return "邮件有未领取的附件，无法删除！请先领取附件。";
        }
        
        try {
            mailMapper.deleteByPrimaryKey(mailId);
            return "『邮件删除成功！』";
        } catch (Exception e) {
            e.printStackTrace();
            return "邮件删除失败！";
        }
    }
    
    @Override
    public boolean sendSystemMail(Long receiverId, String title, String content, String attachmentData) {
        try {
            LifeMail mail = new LifeMail();
            mail.setSenderId(0L); // 系统邮件发送者ID为0
            mail.setReceiverId(receiverId);
            mail.setTitle(title);
            mail.setContent(content);
            mail.setMailType(1); // 系统邮件
            mail.setHasAttachment(attachmentData != null && !attachmentData.isEmpty() ? 1 : 0);
            mail.setAttachmentData(attachmentData);
            mail.setIsRead(0);
            mail.setIsReceived(0);
            mail.setSendTime(new Date());
            
            mailMapper.insert(mail);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private String formatDate(Date date) {
        if (date == null) {
            return "未知";
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd HH:mm");
        return sdf.format(date);
    }
}
