package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.commom.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.PlayerFriends;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.dao.mapper.PlayerFriendsMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Component("myFriendsMenuPrinter")
public class MyFriendsMenuPrinter extends Menu {

    MyFriendsMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.MyFriends.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        PlayerFriendsMapper playerFriendsMapper = (PlayerFriendsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_FRIENDS);
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        PlayerFriends param = new PlayerFriends();
        param.setPlayerId(token);
        List<PlayerFriends> list = playerFriendsMapper.selectBySelective(param);
        int index = 1;
        if (CollectionUtil.isEmpty(list)) {
            this.describe = GameConsts.MyFriends.EMPTY;
        }else {
            List<String> ids = list.stream().map(PlayerFriends::getFriendId).collect(Collectors.toList());
            List<GamePlayer> friends = gamePlayerMapper.getByIds(ids);
            for (GamePlayer friend : friends) {
                this.menuChildrenMap.put(IndexUtil.getIndex(index), new FriendDetailPrinter(friend));
                index++;
            }
            this.describe = GameConsts.MyFriends.CHOOSE_TIP;
        }
        // 先重置，避免不必要的问题
        FindFriendPrinter.waitAddFriend.remove(token);
        this.menuChildrenMap.put(IndexUtil.getIndex(index), new FindFriendPrinter());
    }
}
