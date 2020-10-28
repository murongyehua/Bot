package com.bot.game.chain.menu;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Component("playerInfoMenuPrinter")
public class PlayerInfoMenuPrinter extends Menu {

    PlayerInfoMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.PlayerInfo.MENU_NAME;
        this.menuChildrenMap.put(BaseConsts.Menu.ONE, new ChangeAppellationPrinter());
    }

    @Override
    public void getDescribe(String token) {
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        PlayerPhantom param = new PlayerPhantom();
        param.setPlayerId(token);
        List<PlayerPhantom> playerPhantoms = playerPhantomMapper.selectBySelective(param);
        this.describe = String.format(GameConsts.PlayerInfo.DESCRIBE, gamePlayer.getNickname(),
                StrUtil.isEmpty(gamePlayer.getAppellation()) ? GameConsts.CommonTip.EMPTY : gamePlayer.getAppellation(),
                playerPhantoms.size(),
                gamePlayer.getSoulPower());
    }

}
