package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.common.constant.BaseConsts;
import org.springframework.stereotype.Component;

/**
 * 音乐菜单执行者
 * @author murongyehua
 * @version 1.0 2020/10/10
 */
@Component("cloudMusicMenuPrinter")
public class CloudMusicMenuPrinter extends Menu {

    CloudMusicMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Menu.MUSIC;
        this.describe = BaseConsts.Music.DESCRIBE;
    }

}
