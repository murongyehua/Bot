package com.bot.game.chain.menu;

import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.mapper.GamePlayerMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Component("rankListMenuPrinter")
public class RankListMenuPrinter extends Menu {

    RankListMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.RankList.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        List<GamePlayer> list = gamePlayerMapper.getBySoulPowerDesc();
        StringBuilder stringBuilder = new StringBuilder();
        if (list.size() >= GameConsts.RankList.SHOW_NUMBER) {
            for (int index=0;index < GameConsts.RankList.SHOW_NUMBER;index++) {
                GamePlayer gamePlayer = list.get(index);
                stringBuilder.append(this.getRankContent(gamePlayer, index)).append(StrUtil.CRLF);
            }
        }else {
            for (int index=0;index < list.size();index++) {
                GamePlayer gamePlayer = list.get(index);
                stringBuilder.append(this.getRankContent(gamePlayer, index)).append(StrUtil.CRLF);
            }
        }
        List<String> ids = list.stream().map(GamePlayer::getId).collect(Collectors.toList());
        stringBuilder.append(String.format(GameConsts.RankList.MY_POSITION, ids.indexOf(token) + 1));
        this.describe = stringBuilder.toString();
    }

    private String getRankContent(GamePlayer gamePlayer, int index) {
        if (StrUtil.isNotEmpty(gamePlayer.getAppellation())) {
            return String.format(GameConsts.RankList.ELEMENT_WITH_APPELLATION, String.valueOf(index + 1),
                    gamePlayer.getAppellation(), gamePlayer.getNickname(), gamePlayer.getSoulPower());
        }else {
            return String.format(GameConsts.RankList.ELEMENT_APPELLATION,
                    String.valueOf(index + 1), gamePlayer.getNickname(), gamePlayer.getSoulPower());
        }
    }
}
