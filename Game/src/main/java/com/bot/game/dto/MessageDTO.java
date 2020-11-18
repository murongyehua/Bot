package com.bot.game.dto;

import com.bot.game.enums.ENWriteMessageStatus;
import lombok.Data;

import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/11/17
 */
@Data
public class MessageDTO {

    private String targetId;

    private String content;

    private ENWriteMessageStatus enWriteMessageStatus;

    private List<AttachDTO> attaches;

}
