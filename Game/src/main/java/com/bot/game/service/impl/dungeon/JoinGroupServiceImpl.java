package com.bot.game.service.impl.dungeon;

import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.GameChainCollector;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dto.DungeonGroupDTO;
import com.bot.game.dto.DungeonSinglePlayerDTO;
import com.bot.game.service.DungeonCommonHolder;
import com.bot.game.service.impl.CommonPlayer;

import java.util.Collections;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/11/5
 */
public class JoinGroupServiceImpl extends CommonPlayer {

    private String dungeon;

    private Integer index;

    private List<PlayerPhantom> playerPhantoms;

    public JoinGroupServiceImpl(String dungeon, Integer index, List<PlayerPhantom> playerPhantoms) {
        this.dungeon = dungeon;
        this.index = index;
        this.playerPhantoms = playerPhantoms;
        PlayerPhantom playerPhantom = playerPhantoms.get(1);
        this.title = String.format(GameConsts.Battle.TITLE,
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel());
    }

    @Override
    public String doPlay(String token) {
        // 控制下个指令只能是【00】
        GameChainCollector.supportPoint.put(token, Collections.singletonList(BaseConsts.Menu.DOUBLE_ZERO));
        try {
            DungeonGroupDTO dungeonGroupDTO = DungeonCommonHolder.DUNGEON_GROUP.get(dungeon).get(index);
            DungeonSinglePlayerDTO dungeonSinglePlayerDTO = new DungeonSinglePlayerDTO();
            dungeonSinglePlayerDTO.setPlayerId(token);
            dungeonSinglePlayerDTO.setPhantoms(playerPhantoms);
            dungeonGroupDTO.getPlayers().add(dungeonSinglePlayerDTO);
            return GameConsts.Dungeon.JOIN_SUCCESS;
        } catch (Exception e) {
            // do nothing
        }
        return GameConsts.Dungeon.GROUP_FULL;
    }



}
