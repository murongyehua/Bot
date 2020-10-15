package com.bot.game.chain;

import java.util.Map;

/**
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
public interface Collector {

    /**
     * 构建调用链 在玩家进入时进行
     * @return
     */
    String buildCollector(String token, Map<String, Object> mapperMap);

    /**
     * 前往下一个或上一个菜单
     * @param token
     * @param point
     * @return
     */
    String toNextOrPrevious(String token, String point);

    /**
     * 玩家退出时调用
     * @param token
     * @return
     */
    void removeToken(String token);

    /**
     * 判断玩家是否在线
     * @param token
     * @return
     */
    boolean isOnLine(String token);

}
