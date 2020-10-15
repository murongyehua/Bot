package com.bot.game.chain.menu;

import com.bot.commom.constant.BaseConsts;
import com.bot.game.chain.Menu;
import org.springframework.stereotype.Component;

/**
 * 天气菜单执行者
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
@Component("game")
public class GameDemo extends Menu {

    GameDemo() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Menu.WEATHER_MENU_NAME;
        this.describe = BaseConsts.Weather.DESCRIBE;
        this.menuChildrenMap.put("1", new PlayerInfoMenuPrinter());
    }

}
