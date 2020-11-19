package com.bot.game.chain.menu;

import com.bot.common.constant.GameConsts;
import com.bot.game.chain.Menu;

import java.util.LinkedList;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/17
 */
public class FindFriendPrinter extends Menu {

    public static List<String> waitAddFriend = new LinkedList<>();

    FindFriendPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.MyFriends.FIND_MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        waitAddFriend.add(token);
        this.describe = GameConsts.MyFriends.ADD_TIP;
    }

}
