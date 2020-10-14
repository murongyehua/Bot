package com.bot.game.chain;

/**
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
public interface Collector {

    /**
     * 构建调用链 在用户登录时/退出菜单时进行
     * @return
     */
    String buildCollector(String token);

    /**
     * 前往下一个或上一个菜单
     * @param token
     * @param point
     * @return
     */
    String toNextOrPrevious(String token, String point);
}
