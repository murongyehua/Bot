package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.service.impl.FriendCompareServiceImpl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author murongyehua
 * @version 1.0 2020/10/27
 */
public class CompareDetailPrinter extends Menu {

    private GamePlayer friend;

    CompareDetailPrinter(GamePlayer gamePlayer) {
        this.friend = gamePlayer;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = StrUtil.isEmpty(friend.getAppellation())
                ? String.format(GameConsts.FriendCompare.TITLE_2, friend.getNickname(), friend.getSoulPower())
                : String.format(GameConsts.FriendCompare.TITLE_1, friend.getAppellation(), friend.getNickname(), friend.getSoulPower());
    }

    @Override
    public void getDescribe(String token) {
        if (friend.getSoulPower() <= 1) {
            this.describe = GameConsts.FriendCompare.FRIEND_EMPTY;
            return;
        }
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom param = new PlayerPhantom();
        param.setPlayerId(token);
        List<PlayerPhantom> list = playerPhantomMapper.selectBySelective(param);
        if (CollectionUtil.isEmpty(list)) {
            this.describe = GameConsts.FriendCompare.PLAYER_EMPTY;
            return;
        }
        PlayerPhantom paramFriend = new PlayerPhantom();
        paramFriend.setPlayerId(friend.getId());
        List<PlayerPhantom> listFriend = playerPhantomMapper.selectBySelective(paramFriend);
        List<PlayerPhantom> newList = listFriend.stream().sorted(Comparator.comparing(PlayerPhantom::getLevel).reversed()).collect(Collectors.toList());
        this.describe = GameConsts.FriendCompare.PICK;
        for (int index=0; index < list.size(); index ++) {
            this.playServiceMap.put(String.valueOf(index + 1), new FriendCompareServiceImpl(list.get(index), newList.get(0)));
        }
    }


}
