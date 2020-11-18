package com.bot.game.chain.menu;

import com.bot.commom.constant.GameConsts;
import com.bot.commom.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.enums.ENArea;

/**
 * @author liul
 * @version 1.0 2020/11/16
 */
public class ExploreAllAreaPrinter extends Menu {

    ExploreAllAreaPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.Explore.EXPLORER_ALL_AREA;
    }

    @Override
    public void getDescribe(String token) {
        ENArea[] enAreas = ENArea.values();
        for (int index=0; index < enAreas.length; index++) {
            this.menuChildrenMap.put(IndexUtil.getIndex(index + 1), new ExploreAreaPrinter(ENArea.getByValue(enAreas[index].getValue())));
        }
    }

}
