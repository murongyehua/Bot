package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.common.constant.BaseConsts;
import org.springframework.stereotype.Component;

@Component("photoMenuPrinter")
public class PhotoMenuPrinter extends Menu {

    PhotoMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Picture.PICTURE;
        this.describe = BaseConsts.Picture.TIP;
    }
}
