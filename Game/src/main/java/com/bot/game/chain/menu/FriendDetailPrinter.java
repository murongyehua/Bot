package com.bot.game.chain.menu;

import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.BaseConsts;
import com.bot.game.chain.Menu;
import com.bot.game.chain.menu.message.WriteMessageMenuPrinter;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.service.impl.DeleteFriendServiceImpl;

/**
 * @author liul
 * @version 1.0 2020/11/16
 */
public class FriendDetailPrinter extends Menu {

    private final GamePlayer friend;

    FriendDetailPrinter(GamePlayer friend) {
        this.friend = friend;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = StrUtil.isNotEmpty(friend.getAppellation()) ?
                String.format("[%s]%s", friend.getAppellation(), friend.getNickname()) : friend.getNickname();
    }

    @Override
    public void getDescribe(String token) {
        this.menuChildrenMap.put(BaseConsts.Menu.ONE, new CompareDetailPrinter(friend));
        this.menuChildrenMap.put(BaseConsts.Menu.TWO, new WriteMessageMenuPrinter(friend.getId()));
        this.playServiceMap.put(BaseConsts.Menu.THREE, new DeleteFriendServiceImpl(friend.getId()));
    }

}
