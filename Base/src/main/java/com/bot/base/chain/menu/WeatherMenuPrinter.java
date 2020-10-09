package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.commom.constant.BaseConsts;
import org.springframework.stereotype.Component;

/**
 * 天气菜单执行者
 * @author murongyehua
 * @version 1.0 2020/9/22
 */
@Component("weatherMenuPrinter")
public class WeatherMenuPrinter extends Menu{

    WeatherMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Menu.WEATHER_MENU_NAME;
        this.describe = BaseConsts.Weather.DESCRIBE;
    }

}
