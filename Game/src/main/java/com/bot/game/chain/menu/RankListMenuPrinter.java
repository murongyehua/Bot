package com.bot.game.chain.menu;

import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import org.springframework.stereotype.Component;


/**
 * @author liul
 * @version 1.0 2020/10/15
 */
@Component("rankListMenuPrinter")
public class RankListMenuPrinter extends Menu {

    RankListMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.RankList.MENU_NAME;
    }

}
