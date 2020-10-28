package com.bot.game.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.menu.FindFriendPrinter;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.PlayerFriends;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.dao.mapper.PlayerFriendsMapper;

import java.util.Date;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/17
 */
public class FindPlayerServiceImpl extends CommonPlayer {

    public static String addFriend(String token, String targetName) {
        PlayerFriendsMapper playerFriendsMapper = (PlayerFriendsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_FRIENDS);
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setNickname(targetName);
        List<GamePlayer> list = gamePlayerMapper.selectBySelective(gamePlayer);
        if (CollectionUtil.isEmpty(list)) {
            return GameConsts.MyFriends.NOT_FOUND;
        }
        GamePlayer friend = list.get(0);
        PlayerFriends playerFriends = new PlayerFriends();
        playerFriends.setPlayerId(token);
        playerFriends.setFriendId(friend.getId());
        List<PlayerFriends> friends = playerFriendsMapper.selectBySelectvie(playerFriends);
        if (CollectionUtil.isNotEmpty(friends)) {
            return GameConsts.MyFriends.REPEAT;
        }
        playerFriends.setGetTime(new Date());
        playerFriends.setId(IdUtil.simpleUUID());
        playerFriendsMapper.insert(playerFriends);
        FindFriendPrinter.waitAddFriend.remove(token);
        return GameConsts.MyFriends.ADD_SUCCESS;
    }

}
