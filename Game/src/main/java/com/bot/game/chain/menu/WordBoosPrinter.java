package com.bot.game.chain.menu;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.service.impl.WorldBossServiceImpl;

import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/28
 */
public class WordBoosPrinter extends Menu {

    WordBoosPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.WorldBoss.TITLE;
    }

    @Override
    public void getDescribe(String token) {
        this.describe = String.format(GameConsts.WorldBoss.BOOS,
                WorldBossServiceImpl.boos.getName(), WorldBossServiceImpl.boos.getLevel(), WorldBossServiceImpl.boos.getAttribute(),
                WorldBossServiceImpl.boos.getFinalHp(), WorldBossServiceImpl.boos.getHp()) + StrUtil.CRLF + GameConsts.WorldBoss.PICK;
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom phantomParam = new PlayerPhantom();
        phantomParam.setPlayerId(token);
        List<PlayerPhantom> playerPhantoms = playerPhantomMapper.selectBySelective(phantomParam);
        for (int index=0; index < playerPhantoms.size(); index++) {
            this.playServiceMap.put(String.valueOf(index + 1), new WorldBossServiceImpl(playerPhantoms.get(index)));
        }
    }

}
