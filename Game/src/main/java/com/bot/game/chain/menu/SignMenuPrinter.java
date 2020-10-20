package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.BaseGoods;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.enums.ENArea;
import com.bot.game.service.impl.CommonPlayer;

import java.util.*;

/**
 * @author liul
 * @version 1.0 2020/10/20
 */
public class SignMenuPrinter extends Menu {

    public static Map<String, Date> signMap = new HashMap<>();

    SignMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.Sign.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        if (signMap.get(token) != null && DateUtil.isSameDay(signMap.get(token), new Date())) {
            this.describe =  GameConsts.Sign.SIGN_REPEAT;
            return;
        }
        String result = this.randomGetGoods(token);
        if (result == null) {
            this.describe = GameConsts.Sign.SIGN_NOTHING;
        }
        this.describe = String.format(GameConsts.Sign.SIGN_SUCCESS, result);
    }

    private String randomGetGoods(String token) {
        List<ENArea> list = Arrays.asList(ENArea.values());
        BaseGoods baseGoods = CommonPlayer.getResultGoods(list.get(RandomUtil.randomInt(list.size())).getValue());
        if (baseGoods == null) {
            return null;
        }
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        PlayerGoods param = new PlayerGoods();
        param.setGoodId(baseGoods.getId());
        param.setPlayerId(token);
        List<PlayerGoods> playerGoodsList = playerGoodsMapper.selectBySelective(param);
        if (CollectionUtil.isNotEmpty(playerGoodsList)) {
            PlayerGoods playerGoods = playerGoodsList.get(0);
            playerGoods.setNumber(playerGoods.getNumber() + 1);
            playerGoodsMapper.updateByPrimaryKey(playerGoods);
            return baseGoods.getName();
        }
        param.setId(IdUtil.simpleUUID());
        param.setNumber(1);
        playerGoodsMapper.insert(param);
        return baseGoods.getName();
    }

}
