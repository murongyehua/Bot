package com.bot.game.chain.menu.message;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.common.constant.GameConsts;
import com.bot.common.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.Message;
import com.bot.game.dao.mapper.MessageMapper;
import com.bot.game.enums.ENMessageStatus;

import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/11/14
 */
public class MessageMenuPrinter extends Menu {

    public MessageMenuPrinter(String token) {
        this.getDescribe(token);
    }

    @Override
    public void initMenu() {
        // do nothing
    }

    @Override
    public void getDescribe(String token) {
        this.menuChildrenMap.clear();
        MessageMapper messageMapper = (MessageMapper) mapperMap.get(GameConsts.MapperName.MESSAGE);
        Message param = new Message();
        param.setReceiver(token);
        param.setStatus(ENMessageStatus.NOT_READ.getValue());
        List<Message> messageList = messageMapper.selectBySelective(param);
        this.menuName = String.format(GameConsts.Message.MENU_NAME + "(%s条未读)", messageList.size());
        if (CollectionUtil.isEmpty(messageList)) {
            this.describe = GameConsts.Message.EMPTY;
            this.menuName = GameConsts.Message.MENU_NAME;
            return;
        }
        this.describe = GameConsts.Message.READ_TIP;
        int index = 1;
        for (Message message : messageList) {
            this.menuChildrenMap.put(IndexUtil.getIndex(index), new MessageDetailPrinter(message));
            index++;
        }
    }

}
