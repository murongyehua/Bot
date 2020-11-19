package com.bot.game.chain.menu.dungeon;

import com.bot.common.constant.GameConsts;
import com.bot.common.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.DungeonGroupDTO;
import com.bot.game.service.DungeonCommonHolder;
import com.bot.game.service.impl.dungeon.JoinGroupServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/11/5
 */
@Slf4j
public class PickPhantomPrinter extends Menu {

    private PlayerPhantom onePhantom;

    private String dungeon;

    private Integer index;

    PickPhantomPrinter(PlayerPhantom playerPhantom, String dungeon, Integer index) {
        this.onePhantom = playerPhantom;
        this.dungeon = dungeon;
        this.index = index;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = String.format(GameConsts.Battle.TITLE,
                onePhantom.getAppellation(), onePhantom.getName(), onePhantom.getLevel());
    }

    @Override
    public void getDescribe(String token) {
        try {
            DungeonGroupDTO group = DungeonCommonHolder.DUNGEON_GROUP.get(dungeon).get(index);
            if (group != null && group.getPlayers().size() < 2) {
                this.describe = String.format(GameConsts.Dungeon.PICK_PHANTOM, 2);
                PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
                PlayerPhantom phantomParam = new PlayerPhantom();
                phantomParam.setPlayerId(token);
                List<PlayerPhantom> playerPhantoms = playerPhantomMapper.selectBySelective(phantomParam);
                playerPhantoms.remove(onePhantom);
                for (int index = 0; index < playerPhantoms.size(); index++) {
                    List<PlayerPhantom> phantoms = new LinkedList<>();
                    phantoms.add(onePhantom);
                    phantoms.add(playerPhantoms.get(index));
                    this.playServiceMap.put(IndexUtil.getIndex(index + 1), new JoinGroupServiceImpl(dungeon, this.index, phantoms));
                }
            } else {
                this.describe = GameConsts.Dungeon.GROUP_FULL;
            }
        } catch (Exception e) {
            log.error("副本操作出现异常", e);
            this.describe = GameConsts.Dungeon.GROUP_FULL;
        }
    }





}
