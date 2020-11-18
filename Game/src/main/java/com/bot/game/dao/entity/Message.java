package com.bot.game.dao.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * bot_message
 * @author 
 */
@Data
public class Message implements Serializable {
    private String id;

    private String receiver;

    private String sender;

    private String content;

    private String type;

    private String status;

    private String sendTime;

    private static final long serialVersionUID = 1L;
}