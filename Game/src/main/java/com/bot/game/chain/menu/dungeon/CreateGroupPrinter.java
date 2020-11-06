package com.bot.game.chain.menu.dungeon;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.commom.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.DungeonGroupDTO;
import com.bot.game.enums.ENDungeonResult;
import com.bot.game.service.DungeonCommonHolder;

import java.util.LinkedList;
import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/11/5
 */
public class CreateGroupPrinter extends Menu {

    private String dungeon;

    CreateGroupPrinter(String dungeon) {
        this.dungeon = dungeon;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.Dungeon.CREATE_GROUP;
    }

    @Override
    public void getDescribe(String token) {
        this.describe = String.format(GameConsts.Dungeon.PICK_PHANTOM, 1);
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom phantomParam = new PlayerPhantom();
        phantomParam.setPlayerId(token);
        List<PlayerPhantom> playerPhantoms = playerPhantomMapper.selectBySelective(phantomParam);
        for (int index=0; index < playerPhantoms.size(); index++) {
            this.menuChildrenMap.put(IndexUtil.getIndex(index + 1), new PickPhantomPrinter(playerPhantoms.get(index), dungeon, index));
        }
        // 初始化队伍
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        DungeonGroupDTO dungeonGroupDTO = new DungeonGroupDTO();
        dungeonGroupDTO.setName(gamePlayerMapper.selectByPrimaryKey(token).getNickname() + "的探索小队");
        dungeonGroupDTO.setPlayers(new LinkedList<>());
        dungeonGroupDTO.setResult(ENDungeonResult.WAIT);
        DungeonCommonHolder.dungeonGroup.get(dungeon).add(dungeonGroupDTO);
    }

    @Override
    protected void appendTurnBack(StringBuilder stringBuilder) {
        // 队伍已创建，不能返回
    }

}
