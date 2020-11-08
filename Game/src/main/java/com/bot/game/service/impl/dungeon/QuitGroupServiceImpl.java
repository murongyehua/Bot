package com.bot.game.service.impl.dungeon;

import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.GameChainCollector;
import com.bot.game.dto.DungeonGroupDTO;
import com.bot.game.dto.DungeonSinglePlayerDTO;
import com.bot.game.service.DungeonCommonHolder;
import com.bot.game.service.impl.CommonPlayer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liul
 * @version 1.0 2020/11/5
 */
public class QuitGroupServiceImpl extends CommonPlayer {

    private String dungeon;

    public QuitGroupServiceImpl(String dungeon) {
        this.dungeon = dungeon;
        this.title = GameConsts.Dungeon.QUIT_GROUP;
    }

    @Override
    public String doPlay(String token) {
        // 控制下个指令只能是【00】
        GameChainCollector.supportPoint.put(token, Collections.singletonList(BaseConsts.Menu.DOUBLE_ZERO));
        List<DungeonGroupDTO> groups = DungeonCommonHolder.dungeonGroup.get(dungeon);
        List<DungeonGroupDTO> myGroups = groups.stream().filter(x -> {
            for (DungeonSinglePlayerDTO player : x.getPlayers()) {
                if (player.getPlayerId().equals(token)) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
        DungeonGroupDTO myGroup = myGroups.get(0);
        if (myGroup.getPlayers().size() >= 2) {
            return GameConsts.Dungeon.QUIT_FULL;
        }
        Integer index = null;
        for (DungeonSinglePlayerDTO player : myGroup.getPlayers()) {
            if (player.getPlayerId().equals(token)) {
                index = myGroup.getPlayers().indexOf(player);
            }
        }
        if (index != null) {
            myGroup.getPlayers().remove(index.intValue());
            if (myGroup.getPlayers().size() == 0) {
                groups.remove(myGroup);
            }
        }
        return GameConsts.Dungeon.QUIT_SUCCESS;
    }
}
