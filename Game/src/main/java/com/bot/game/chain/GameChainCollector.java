package com.bot.game.chain;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.game.chain.menu.GameMainMenuPrinter;
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
    public String buildCollector(String token) {
        if (StrUtil.isEmpty(token)) {
            return null;
        }
        List<Menu> chain = new ArrayList<>();
        Menu menu = new GameMainMenuPrinter();
        chain.add(menu);
        userChainMap.put(token, chain);
        return menu.print();
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
            return targetMenu.print();
        }
        Menu nowMenu = chain.get(chain.size() - 1);
        Menu targetMenu = nowMenu.menuChildrenMap.get(point);
        if (targetMenu == null) {
            // 非正确链路调用
            return null;
        }
        chain.add(targetMenu);
        return targetMenu.print();
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
