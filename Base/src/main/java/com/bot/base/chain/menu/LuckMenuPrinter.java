package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.commom.constant.BaseConsts;
import org.springframework.stereotype.Component;

/**
 * 运势占卜菜单执行者
 * @author liul
 * @version 1.0 2020/9/27
 */
@Component("luckMenuPrinter")
public class LuckMenuPrinter extends Menu {

    LuckMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Menu.LUCK_NAME;
        this.describe = BaseConsts.Luck.DESCRIBE;
    }
}
