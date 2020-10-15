package com.bot.game.chain.menu;

import com.bot.commom.constant.BaseConsts;
import com.bot.game.chain.Menu;
import org.springframework.stereotype.Component;

/**
 * 主菜单执行者
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
@Component("gameMainMenuPrinter")
public class GameMainMenuPrinter extends Menu {

    public GameMainMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Menu.MAIN_MENU_NAME;
        this.menuChildrenMap.put(BaseConsts.Menu.ONE, new PlayerInfoMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.TWO, new MyPhantomMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.THREE, new MyKnapsackMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.FOUR, new GetPhantomMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.FIVE, new ExploreMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.SIX, new MyFriendsMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.SEVEN, new FriendCompareMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.EIGHT, new RankListMenuPrinter());
    }

    @Override
    public void appendTurnBack(StringBuilder stringBuilder) {
        // 主菜单无需添加返回选项 这里不做任何处理
    }
}
