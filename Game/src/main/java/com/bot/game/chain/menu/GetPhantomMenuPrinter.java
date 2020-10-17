package com.bot.game.chain.menu;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.service.impl.CommonPlayer;
import com.bot.game.service.impl.GetPhantomServiceImpl;
import org.springframework.stereotype.Component;

/**
 * @author liul
 * @version 1.0 2020/10/15
 */
@Component("getPhantomMenuPrinter")
public class GetPhantomMenuPrinter extends Menu {

    GetPhantomMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.GetPhantom.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        PlayerGoods playerGoods = CommonPlayer.isCanGetPhantom(token);
        if (playerGoods == null) {
            this.describe = GameConsts.GetPhantom.CAN_GET_TIME;
            return;
        }
        this.describe = String.format(GameConsts.GetPhantom.SUCCESS, playerGoods.getNumber()) + StrUtil.CRLF +
                GameConsts.GetPhantom.WAIT_GET_1 + StrUtil.CRLF + GameConsts.GetPhantom.WAIT_GET_2;
        this.playServiceMap.put(BaseConsts.Menu.ONE, new GetPhantomServiceImpl(GameConsts.GetPhantom.WAIT_1, playerGoods));
        this.playServiceMap.put(BaseConsts.Menu.TWO, new GetPhantomServiceImpl(GameConsts.GetPhantom.WAIT_2, playerGoods));
        this.playServiceMap.put(BaseConsts.Menu.THREE, new GetPhantomServiceImpl(GameConsts.GetPhantom.WAIT_3, playerGoods));
        this.playServiceMap.put(BaseConsts.Menu.FOUR, new GetPhantomServiceImpl(GameConsts.GetPhantom.WAIT_4, playerGoods));
    }
}
