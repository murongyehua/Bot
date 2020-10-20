package com.bot.game.chain;

/**
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
public interface MenuPrinter {

    /**
     * 初始化菜单 各菜单执行者自己实现
     */
    void initMenu();

    /**
     * 获取菜单内容 各菜单自己实现
     * @param token
     */
    void getDescribe(String token);

    /**
     * 有必要的话，重新初始化
     */
    void reInitMenu(String token);

}
