package com.bot.game.chain.menu.dungeon;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.DungeonGroupDTO;
import com.bot.game.dto.DungeonSinglePlayerDTO;
import com.bot.game.dto.DungeonTryTimesDTO;
import com.bot.game.enums.ENDungeon;
import com.bot.game.enums.ENDungeonResult;
import com.bot.game.service.DungeonCommonHolder;
import com.bot.game.service.impl.dungeon.QuitGroupServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author murongyehua
 * @version 1.0 2020/11/5
 */
public class DungeonWaitMenuPrinter extends Menu {

    private ENDungeon enDungeon;

    DungeonWaitMenuPrinter(ENDungeon enDungeon) {
        this.enDungeon = enDungeon;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = String.format(GameConsts.Dungeon.DUNGEON_NAME, enDungeon.getLabel(), enDungeon.getSuggestLevel());
    }

    @Override
    public void getDescribe(String token) {
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom param = new PlayerPhantom();
        param.setPlayerId(token);
        List<PlayerPhantom> list = playerPhantomMapper.selectBySelective(param);
        if (CollectionUtil.isEmpty(list) || list.size() < 2) {
            this.describe = GameConsts.Dungeon.PHANTOM_NOT;
            return;
        }
        List<DungeonGroupDTO> groups = DungeonCommonHolder.DUNGEON_GROUP.get(enDungeon.getValue());
        List<DungeonGroupDTO> myGroup = groups.stream().filter(x -> {
            boolean isMyGroup = false;
            for (DungeonSinglePlayerDTO dungeonSinglePlayerDTO : x.getPlayers()) {
                if (dungeonSinglePlayerDTO.getPlayerId().equals(token)) {
                    isMyGroup = true;
                }
            }
            return isMyGroup;
        }).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(myGroup)) {
            // 已有队伍的几种情况
            if (myGroup.get(0).getPlayers().size() < 2) {
                this.describe =  GameConsts.Dungeon.GROUP_WAIT;
                // 添加可退出队伍的选项 退出时校验是否已满 已满不允许退出
                this.playServiceMap.put(BaseConsts.Menu.ONE, new QuitGroupServiceImpl(enDungeon.getValue()));
                return;
            }
            if (ENDungeonResult.WAIT.equals(myGroup.get(0).getResult())) {
                this.describe = GameConsts.Dungeon.GROUP_WAIT_FULL;
                return;
            }
            this.describe = GameConsts.Dungeon.DUNGEON_FINISH + myGroup.get(0).getResultMap().get(token);
            return;
        }
        // 校验挑战次数
        List<DungeonTryTimesDTO> times = DungeonCommonHolder.tryTimes.get(token);
        if (times != null) {
            Optional<DungeonTryTimesDTO> optional = times.stream().filter(x -> x.getDungeon().equals(enDungeon.getValue())).findFirst();
            if (optional.isPresent() && optional.get().getTimes() >= 1) {
                this.describe = GameConsts.Dungeon.REPEAT;
                return;
            }
        }
        // 没有加入队伍的情况
        List<DungeonGroupDTO> finalGroups = groups.stream().filter(x -> x.getPlayers().size() < 2).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(finalGroups)) {
            this.describe = GameConsts.Dungeon.NO_GROUP;
        }
        int index = 0;
        if (CollectionUtil.isNotEmpty(finalGroups)) {
            this.describe = GameConsts.Dungeon.WAIT_TIP;
            for (;index < groups.size(); index++) {
                this.menuChildrenMap.put(String.valueOf(index + 1), new JoinDungeonPrinter(enDungeon.getValue(), index));
            }
        }
        this.menuChildrenMap.put(String.valueOf(index + 1), new CreateGroupPrinter(enDungeon.getValue()));
    }
}
