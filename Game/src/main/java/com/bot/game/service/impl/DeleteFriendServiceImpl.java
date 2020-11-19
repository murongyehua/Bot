package com.bot.game.service.impl;

import com.bot.common.constant.GameConsts;
import com.bot.game.dao.entity.PlayerFriends;
import com.bot.game.dao.mapper.PlayerFriendsMapper;

/**
 * @author liul
 * @version 1.0 2020/11/17
 */
public class DeleteFriendServiceImpl extends CommonPlayer {

    private final String targetId;

    public DeleteFriendServiceImpl(String targetId) {
        this.targetId = targetId;
        this.title = "删掉";
    }

    @Override
    public String doPlay(String token) {
        PlayerFriendsMapper playerFriendsMapper = (PlayerFriendsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_FRIENDS);
        PlayerFriends param = new PlayerFriends();
        param.setPlayerId(token);
        param.setFriendId(targetId);
        playerFriendsMapper.deleteBySelective(param);
        return GameConsts.CommonTip.PLAY_SUCCESS;
    }

}
