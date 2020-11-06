package com.bot.game.chain.menu;

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
        ENArea[] enAreas = ENArea.values();
        for (int index=0; index < enAreas.length; index++) {
            this.menuChildrenMap.put(IndexUtil.getIndex(index + 1), new ExploreAreaPrinter(ENArea.getByValue(enAreas[index].getValue())));
        }
        this.menuChildrenMap.put(IndexUtil.getIndex(enAreas.length + 1), new WordBoosPrinter());
        this.menuChildrenMap.put(IndexUtil.getIndex(enAreas.length + 2), new DungeonEnterMenuPrinter());
    }

}
