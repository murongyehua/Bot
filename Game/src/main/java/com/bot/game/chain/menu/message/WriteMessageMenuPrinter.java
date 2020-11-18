package com.bot.game.chain.menu.message;

import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dto.MessageDTO;
import com.bot.game.enums.ENWriteMessageStatus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author liul
 * @version 1.0 2020/11/16
 */
public class WriteMessageMenuPrinter extends Menu {

    public final static Map<String, MessageDTO> WRITE_MESSAGE = new HashMap<>();

    private final String targetId;

    public WriteMessageMenuPrinter(String targetId) {
        this.targetId = targetId;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.Message.WRITE_MESSAGE_MENU;
    }

    @Override
    public void getDescribe(String token) {
        MessageDTO message = new MessageDTO();
        message.setEnWriteMessageStatus(ENWriteMessageStatus.WAIT_CONTENT);
        message.setTargetId(targetId);
        message.setAttaches(new LinkedList<>());
        WRITE_MESSAGE.put(token, message);
        this.describe = GameConsts.Message.WRITE_MESSAGE_DESCRIBE;
    }

}
