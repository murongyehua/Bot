package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerAppellation;
import com.bot.game.dao.mapper.PlayerAppellationMapper;
import com.bot.game.service.impl.ChangeAppellationServiceImpl;

import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
public class ChangeAppellationPrinter extends Menu {

    ChangeAppellationPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.LittlePrinter.CHANGE_APPELLATION;
    }

    @Override
    public void getDescribe(String token) {
        PlayerAppellationMapper playerAppellationMapper = (PlayerAppellationMapper) mapperMap.get(GameConsts.MapperName.PLAYER_APPELLATION);
        PlayerAppellation queryParam = new PlayerAppellation();
        queryParam.setPlayerId(token);
        List<PlayerAppellation> list = playerAppellationMapper.selectBySelective(queryParam);
        if (CollectionUtil.isEmpty(list)) {
            this.describe = GameConsts.CommonTip.APPELLATION_EMPTY;
        }else {
            this.describe = GameConsts.CommonTip.APPELLATION_CHANGE;
            for (int index=0; index < list.size(); index ++) {
                this.playServiceMap.put(String.valueOf(index + 1), new ChangeAppellationServiceImpl(list.get(index).getAppellation()));
            }
        }
    }

}
