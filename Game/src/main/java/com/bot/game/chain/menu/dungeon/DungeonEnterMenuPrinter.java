package com.bot.game.chain.menu.dungeon;

import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.enums.ENDungeon;

/**
 * @author murongyehua
 * @version 1.0 2020/11/5
 */
public class DungeonEnterMenuPrinter extends Menu {

    public DungeonEnterMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.Dungeon.MENU_TITLE;
    }

    @Override
    public void getDescribe(String token) {
        this.describe = GameConsts.Dungeon.DESCRIBE;
        ENDungeon[] enDungeons = ENDungeon.values();
        for (int index=0; index < enDungeons.length; index++) {
            this.menuChildrenMap.put(String.valueOf(index + 1), new DungeonWaitMenuPrinter(enDungeons[index]));
        }
    }

}
