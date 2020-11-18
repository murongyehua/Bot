package com.bot.game.chain.menu;

import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.commom.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.chain.menu.dungeon.DungeonEnterMenuPrinter;
import com.bot.game.enums.ENArea;
import org.springframework.stereotype.Component;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Component("exploreMenuPrinter")
public class ExploreMenuPrinter extends Menu {

    ExploreMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.Explore.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        this.describe = GameConsts.Explore.TIP;
        this.menuChildrenMap.put(BaseConsts.Menu.ONE, new ExploreAllAreaPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.TWO, new WordBoosPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.THREE, new DungeonEnterMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.FOUR, new ShopMenuPrinter());
    }

}
