package com.bot.game.chain.menu;

import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import org.springframework.stereotype.Component;

/**
 * @author liul
 * @version 1.0 2020/10/15
 */
@Component("friendCompareMenuPrinter")
public class FriendCompareMenuPrinter extends Menu {

    FriendCompareMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.FriendCompare.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        this.describe = "即将上线，敬请期待!";
    }

}
