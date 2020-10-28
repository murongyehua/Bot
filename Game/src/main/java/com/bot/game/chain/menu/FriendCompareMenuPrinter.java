package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.PlayerFriends;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.dao.mapper.PlayerFriendsMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Component("friendCompareMenuPrinter")
public class FriendCompareMenuPrinter extends Menu {

    FriendCompareMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.FriendCompare.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        PlayerFriendsMapper playerFriendsMapper = (PlayerFriendsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_FRIENDS);
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        PlayerFriends param = new PlayerFriends();
        param.setPlayerId(token);
        List<PlayerFriends> list = playerFriendsMapper.selectBySelectvie(param);

        if (CollectionUtil.isEmpty(list)) {
            this.describe = GameConsts.FriendCompare.EMPTY;
            return;
        }
        final List<GamePlayer> friends= new LinkedList<>();
        list.forEach(playerFriend -> friends.add(gamePlayerMapper.selectByPrimaryKey(playerFriend.getFriendId())));
        for (int index=0; index < friends.size(); index ++) {
            this.menuChildrenMap.put(String.valueOf(index + 1), new CompareDetailPrinter(friends.get(index)));
        }
    }

}
