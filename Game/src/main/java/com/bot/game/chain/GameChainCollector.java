package com.bot.game.chain;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.menu.FindFriendPrinter;
import com.bot.game.chain.menu.GameMainMenuPrinter;
import com.bot.game.service.Player;
import com.bot.game.service.impl.CommonPlayer;
import com.bot.game.service.impl.FindPlayerServiceImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单链路收集与调用者
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
@Component
public class GameChainCollector implements Collector{

    private static Map<String, List<Menu>> userChainMap = new HashMap<>();


    @Override
    public String buildCollector(String token, Map<String, Object> mapperMap) {
        if (StrUtil.isEmpty(token)) {
            return null;
        }
        List<Menu> chain = new ArrayList<>();
        Menu menu = new GameMainMenuPrinter(mapperMap);
        CommonPlayer.mapperMap = mapperMap;
        chain.add(menu);
        userChainMap.put(token, chain);
        return menu.print(token);
    }

    @Override
    public String toNextOrPrevious(String token, String point) {
        List<Menu> chain = userChainMap.get(token);
        if (chain == null) {
            return null;
        }
        if (BaseConsts.Menu.ZERO.equals(point)) {
            // 返回
            chain.remove(chain.size() - 1);
            Menu targetMenu = chain.get(chain.size() -1);
            return targetMenu.print(token);
        }
        Menu nowMenu = chain.get(chain.size() - 1);
        Menu targetMenu = nowMenu.menuChildrenMap.get(point);
        if (targetMenu == null) {
            // 非正确链路调用,尝试调用service
            Player player = nowMenu.playServiceMap.get(point);
            if (player != null) {
                return player.doPlay(token);
            }
            // service也没有 可能是添加好友
            if (FindFriendPrinter.waitAddFriend.contains(token)) {
                return FindPlayerServiceImpl.addFriend(token, point);
            }
            return GameConsts.CommonTip.UN_KNOW_POINT;
        }
        chain.add(targetMenu);
        return targetMenu.print(token);
    }

    @Override
    public void removeToken(String token) {
        userChainMap.remove(token);
    }

    @Override
    public boolean isOnLine(String token) {
        return userChainMap.containsKey(token);
    }
}
