package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.common.constant.BaseConsts;
import org.springframework.stereotype.Component;

@Component("chatMenuPrinter")
public class ChatMenuPrinter extends Menu {

    ChatMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Chat.CHAT;
        this.describe = BaseConsts.Chat.TIP;
    }

}
