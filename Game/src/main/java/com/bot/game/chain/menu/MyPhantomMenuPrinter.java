package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Component("myPhantomMenuPrinter")
public class MyPhantomMenuPrinter extends Menu {

    MyPhantomMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.MyPhantom.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom param = new PlayerPhantom();
        param.setPlayerId(token);
        List<PlayerPhantom> list = playerPhantomMapper.selectBySelective(param);
        if (CollectionUtil.isEmpty(list)) {
            this.describe = GameConsts.CommonTip.PHANTOM_EMPTY;
        }else {
            this.describe = GameConsts.CommonTip.PHANTOM_LOOK;
            for (int index=0; index < list.size(); index++) {
                this.menuChildrenMap.put(String.valueOf(index + 1), new PhantomDetailMenuPrinter(list.get(index)));
            }
        }

    }
}
