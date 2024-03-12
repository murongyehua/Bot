package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.common.constant.BaseConsts;
import org.springframework.stereotype.Component;

@Component("gameMenuPrinter")
public class GameMenuPrinter extends Menu {

    GameMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Menu.GAME;
        this.describe = BaseConsts.Menu.GAME_TIP;
    }
}
