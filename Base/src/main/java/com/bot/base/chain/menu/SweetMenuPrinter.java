package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.commom.constant.BaseConsts;
import org.springframework.stereotype.Component;

/**
 * @author murongyehua
 * @version 1.0 2020/10/9
 */
@Component("sweetMenuPrinter")
public class SweetMenuPrinter extends Menu {

    SweetMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Menu.WEATHER_MENU_NAME;
        this.describe = BaseConsts.Weather.DESCRIBE;
    }
}
