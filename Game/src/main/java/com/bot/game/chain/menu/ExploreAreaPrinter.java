package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.BaseConsts;
import com.bot.common.constant.GameConsts;
import com.bot.common.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.BaseMonster;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.BaseMonsterMapper;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.ExploreBuffDTO;
import com.bot.game.enums.ENArea;
import com.bot.game.service.impl.BattleServiceImpl;
import com.bot.game.service.impl.CommonPlayer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author murongyehua
 * @version 1.0 2020/10/18
 */
public class ExploreAreaPrinter extends Menu {

    private ENArea area;

    ExploreAreaPrinter(ENArea area) {
        this.area = area;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = area.getLabel();
    }

    @Override
    public void getDescribe(String token) {
        if (ENArea.PL_M.getValue().equals(area.getValue()) && !judgeLevel(token)) {
            this.describe = "此地过于凶险，你当前的实力无法涉足，请尝试探索其他区域";
            return;
        }
        BaseMonsterMapper baseMonsterMapper = (BaseMonsterMapper) mapperMap.get(GameConsts.MapperName.BASE_MONSTER);
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        BaseMonster param = new BaseMonster();
        param.setArea(area.getValue());
        List<BaseMonster> list = baseMonsterMapper.selectBySelective(param);
        ExploreBuffDTO exploreBuffDTO = CommonPlayer.exploreBuffMap.get(token);
        List<BaseMonster> finalList;
        if (exploreBuffDTO != null && System.currentTimeMillis() <= exploreBuffDTO.getOutTime().getTime()) {
            int levelRange = 100;
            switch (exploreBuffDTO.getEnGoodEffect()) {
                case WAN_1:
                    levelRange = 5;
                    break;
                case WAN_2:
                    levelRange = 3;
                    break;
                case WAN_3:
                    levelRange = 1;
                    default:
                        break;
            }
            Integer level = playerPhantomMapper.getMaxLevel(token);
            final Integer max = level + levelRange;
            final Integer min = level - levelRange;
            finalList = list.stream().filter(x -> {
                if (x.getLevel() >= min && x.getLevel() <= max) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
        }else {
            finalList = list;
        }
        if (CollectionUtil.isEmpty(finalList)) {
            this.describe = GameConsts.Explore.NO_MONSTER;
            return;
        }
        if (!CommonPlayer.addOrSubActionPoint(token, -1)) {
            this.describe = GameConsts.Explore.NO_ACTION_POINT;
            return;
        }
        BaseMonster baseMonster = finalList.get(RandomUtil.randomInt(finalList.size()));
        this.describe = String.format(GameConsts.Explore.MEET,
                baseMonster.getName(), baseMonster.getAttribute(), baseMonster.getLevel());
        PlayerPhantom phantomParam = new PlayerPhantom();
        phantomParam.setPlayerId(token);
        List<PlayerPhantom> playerPhantoms = playerPhantomMapper.selectBySelective(phantomParam);
        for (int index=0; index < playerPhantoms.size(); index++) {
            this.playServiceMap.put(IndexUtil.getIndex(index + 1), new BattleServiceImpl(baseMonster, playerPhantoms.get(index), false, false, false));
        }
    }

    @Override
    public void appendTurnBack(StringBuilder stringBuilder) {
        stringBuilder.append(BaseConsts.Menu.ZERO).append(StrUtil.DOT).append(StrUtil.SPACE).append(GameConsts.Explore.RUN);
    }

    private boolean judgeLevel(String token) {
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom phantomParam = new PlayerPhantom();
        phantomParam.setPlayerId(token);
        List<PlayerPhantom> playerPhantoms = playerPhantomMapper.selectBySelective(phantomParam);
        if (CollectionUtil.isEmpty(playerPhantoms)) {
            return false;
        }
        if (playerPhantoms.get(0).getLevel() < 20) {
            return false;
        }
        return true;
    }

}
