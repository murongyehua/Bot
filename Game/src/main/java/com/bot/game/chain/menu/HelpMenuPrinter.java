package com.bot.game.chain.menu;

import com.bot.common.constant.GameConsts;
import com.bot.game.chain.Menu;

/**
 * @author murongyehua
 * @version 1.0 2020/10/20
 */
public class HelpMenuPrinter extends Menu {

    HelpMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.Help.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        this.describe = GameConsts.Help.HELP;
    }
}
