package com.bot.game.chain.menu;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.BaseMonster;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.BaseMonsterMapper;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.enums.ENArea;
import com.bot.game.service.impl.BattleServiceImpl;

import java.util.List;

/**
 * @author liul
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
        BaseMonsterMapper baseMonsterMapper = (BaseMonsterMapper) mapperMap.get(GameConsts.MapperName.BASE_MONSTER);
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        BaseMonster param = new BaseMonster();
        param.setArea(area.getValue());
        List<BaseMonster> list = baseMonsterMapper.selectBySelective(param);
        BaseMonster baseMonster = list.get(RandomUtil.randomInt(list.size()));
        this.describe = String.format(GameConsts.Explore.MEET,
                baseMonster.getName(), baseMonster.getAttribute(), baseMonster.getLevel());
        PlayerPhantom phantomParam = new PlayerPhantom();
        phantomParam.setPlayerId(token);
        List<PlayerPhantom> playerPhantoms = playerPhantomMapper.selectBySelective(phantomParam);
        for (int index=0; index < playerPhantoms.size(); index++) {
            this.playServiceMap.put(String.valueOf(index + 1), new BattleServiceImpl(baseMonster, playerPhantoms.get(index)));
        }
    }

    @Override
    public void appendTurnBack(StringBuilder stringBuilder) {
        stringBuilder.append(BaseConsts.Menu.ZERO).append(StrUtil.DOT).append(StrUtil.SPACE).append(GameConsts.Explore.RUN);
    }

}