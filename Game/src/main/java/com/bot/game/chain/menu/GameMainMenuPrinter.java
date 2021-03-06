package com.bot.game.chain.menu;

import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.BaseConsts;
import com.bot.common.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.chain.menu.message.MessageMenuPrinter;

import java.util.Map;

/**
 * 主菜单执行者
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
public class GameMainMenuPrinter extends Menu {

    private final String token;

    public GameMainMenuPrinter(Map<String, Object> mapperMap, String token) {
        this.token = token;
        Menu.mapperMap = mapperMap;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Menu.MAIN_MENU_NAME;
        this.menuChildrenMap.put(BaseConsts.Menu.ZERO, new SignMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.ONE, new PlayerInfoMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.TWO, new MyPhantomMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.THREE, new MyKnapsackMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.FOUR, new GetPhantomMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.FIVE, new ExploreMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.SIX, new MyFriendsMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.SEVEN, new RankListMenuPrinter());
        this.menuChildrenMap.put(BaseConsts.Menu.EIGHT, new MessageMenuPrinter(token));
        this.menuChildrenMap.put(BaseConsts.Menu.NINE, new HelpMenuPrinter());
    }

    @Override
    public void appendTurnBack(StringBuilder stringBuilder) {
        // 主菜单无需添加返回选项 这里不做任何处理
    }

    @Override
    public void getDescribe(String token) {
        this.describe = GameConsts.CommonTip.SEE_VERSION_TIP + StrUtil.CRLF + GameConsts.CommonTip.NOW_VERSION + StrUtil.CRLF + GameConsts.CommonTip.MENU_TIP + StrUtil.CRLF;
    }
}
