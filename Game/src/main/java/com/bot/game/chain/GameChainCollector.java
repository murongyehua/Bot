package com.bot.game.chain;

import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.BaseConsts;
import com.bot.common.constant.GameConsts;
import com.bot.common.loader.CommonTextLoader;
import com.bot.game.chain.menu.FindFriendPrinter;
import com.bot.game.chain.menu.GameMainMenuPrinter;
import com.bot.game.chain.menu.message.WriteMessageMenuPrinter;
import com.bot.game.service.Player;
import com.bot.game.service.impl.CommonPlayer;
import com.bot.game.service.impl.FindPlayerServiceImpl;
import com.bot.game.service.impl.message.SendMessageServiceImpl;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 菜单链路收集与调用者
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
@Component
public class GameChainCollector implements Collector{

    private static Map<String, List<Menu>> userChainMap = new HashMap<>();

    /**
     * 当前支持指令，若有值，则只能输入存在的指令，该指令只会生效一次
     */
    public static Map<String, List<String>> supportPoint = new HashMap<>();


    @Override
    public String buildCollector(String token, Map<String, Object> mapperMap) {
        if (StrUtil.isEmpty(token)) {
            return null;
        }
        List<Menu> chain = new ArrayList<>();
        Menu menu = new GameMainMenuPrinter(mapperMap, token);
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
        List<String> supports = supportPoint.get(token);
        if (supports != null) {
            if (!supports.contains(point)) {
                return GameConsts.CommonTip.ERROR_POINT;
            }
            supportPoint.remove(token);
        }
        Menu nowMenu = chain.get(chain.size() - 1);
        Menu targetMenu = nowMenu.menuChildrenMap.get(point);
        if (targetMenu == null) {
            if (BaseConsts.Menu.ZERO.equals(point)) {
                // 返回
                chain.remove(chain.size() - 1);
                Menu preMenu = chain.get(chain.size() -1);
                // 消息移除
                WriteMessageMenuPrinter.WRITE_MESSAGE.remove(token);
                return preMenu.print(token);
            }
            if (BaseConsts.Menu.DOUBLE_ZERO.equals(point)) {
                // 返回主菜单
                List<Menu> newChain = new LinkedList<>();
                newChain.add(chain.get(0));
                userChainMap.put(token, newChain);
                Menu preMenu = newChain.get(0);
                return preMenu.print(token);
            }
            // 非正确链路调用,尝试调用service
            Player player = nowMenu.playServiceMap.get(point);
            if (player != null) {
                return player.doPlay(token);
            }
            // service也没有 可能是添加好友
            if (FindFriendPrinter.waitAddFriend.contains(token)) {
                return FindPlayerServiceImpl.addFriend(token, point);
            }
            // 可能是发消息
            if (WriteMessageMenuPrinter.WRITE_MESSAGE.containsKey(token)) {
                return SendMessageServiceImpl.trySendMessage(token, point);
            }
            // Q查看最近一次战斗详情
            if (GameConsts.CommonTip.SEE_BATTLE_DETAIL.equals(point)) {
                String result = CommonPlayer.battleDetailMap.get(token);
                if (result != null) {
                    return CommonPlayer.battleDetailMap.get(token);
                }
            }
            // A查看历史版本详情
            if (GameConsts.CommonTip.SEE_VERSION_HISTORY.equals(point)) {
                return CommonTextLoader.gameHistory;
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
