package com.bot.game.chain.menu.dungeon;

import com.bot.commom.constant.GameConsts;
import com.bot.commom.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.DungeonGroupDTO;
import com.bot.game.service.DungeonCommonHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/11/5
 */
@Slf4j
public class JoinDungeonPrinter extends Menu {

    private String dungeon;

    private Integer index;

    JoinDungeonPrinter(String dungeon, Integer index) {
        this.dungeon = dungeon;
        this.index = index;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        try{
            DungeonGroupDTO group = DungeonCommonHolder.DUNGEON_GROUP.get(dungeon).get(index);
            this.menuName = group.getName();
        }catch (Exception e) {
            // do nothing
        }
    }

    @Override
    public void getDescribe(String token) {
        try{
            DungeonGroupDTO group = DungeonCommonHolder.DUNGEON_GROUP.get(dungeon).get(index);
            if (group != null && group.getPlayers().size() < 2) {
                this.describe = String.format(GameConsts.Dungeon.PICK_PHANTOM, 1);
                PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
                PlayerPhantom phantomParam = new PlayerPhantom();
                phantomParam.setPlayerId(token);
                List<PlayerPhantom> playerPhantoms = playerPhantomMapper.selectBySelective(phantomParam);
                for (int index=0; index < playerPhantoms.size(); index++) {
                    this.menuChildrenMap.put(IndexUtil.getIndex(index + 1), new PickPhantomPrinter(playerPhantoms.get(index), dungeon, this.index));
                }
            }else {
                this.describe = GameConsts.Dungeon.GROUP_FULL;
            }
        }catch (Exception e) {
            log.error("副本处理出现异常", e);
            this.describe = GameConsts.Dungeon.GROUP_FULL;
        }
    }


}
