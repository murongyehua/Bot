package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.commom.constant.BaseConsts;
import org.springframework.stereotype.Component;

/**
 * 主菜单执行者
 * @author liul
 * @version 1.0 2020/9/22
 */
@Component("mainMenuPrinter")
public class MainMenuPrinter extends Menu{

    public MainMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Menu.MAIN_MENU_NAME;
        this.menuChildrenMap.put(BaseConsts.Menu.ONE, new WeatherMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.TWO, new AnswerMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.THREE, new LuckMenuPrinter());
    }

    @Override
    public void appendTurnBack(StringBuilder stringBuilder) {
        // 主菜单无需添加返回选项 这里不做任何处理
    }
}
