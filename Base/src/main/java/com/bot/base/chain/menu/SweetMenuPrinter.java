package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.common.constant.BaseConsts;
import org.springframework.stereotype.Component;

/**
 * 情话菜单执行者
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
        this.menuName = BaseConsts.Menu.SWEET;
        this.describe = BaseConsts.Sweet.DESCRIBE;
    }
}
