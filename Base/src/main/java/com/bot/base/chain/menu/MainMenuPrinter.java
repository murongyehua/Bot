package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.common.constant.BaseConsts;
import org.springframework.stereotype.Component;

/**
 * 主菜单执行者
 * @author murongyehua
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
        this.menuChildrenMap.put(BaseConsts.Menu.FOUR, new SweetMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.FIVE, new CloudMusicMenuPrinter());
    }

    @Override
    public void appendTurnBack(StringBuilder stringBuilder) {
        // 主菜单无需添加返回选项 这里不做任何处理
    }
}
