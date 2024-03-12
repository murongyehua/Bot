package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.common.constant.BaseConsts;
import org.springframework.stereotype.Component;

@Component("constellationMenuPrinter")
public class ConstellationMenuPrinter extends Menu {

    ConstellationMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Constellation.CONSTELLATION;
        this.describe = BaseConsts.Constellation.TIP;
    }
}
