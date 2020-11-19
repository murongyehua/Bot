package com.bot.game.chain.menu;

import cn.hutool.core.util.ReflectUtil;
import com.bot.common.constant.GameConsts;
import com.bot.common.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dto.GoodsDetailDTO;
import com.bot.game.enums.ENPhantomAttribute;
import com.bot.game.enums.ENRarity;
import com.bot.game.service.impl.ResetAttributeServiceImpl;

/**
 * @author murongyehua
 * @version 1.0 2020/10/29
 */
public class UseResetAttributePrinter extends Menu {

    private final PlayerPhantom playerPhantom;

    private final GoodsDetailDTO goodsDetailDTO;

    UseResetAttributePrinter(PlayerPhantom playerPhantom, GoodsDetailDTO goodsDetailDTO) {
        this.playerPhantom = playerPhantom;
        this.goodsDetailDTO = goodsDetailDTO;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = String.format(GameConsts.PhantomDetail.MENU_NAME_NO_CARRIED, ENRarity.getLabelByValue(playerPhantom.getRarity()),
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel(), playerPhantom.getAttribute());
    }

    @Override
    public void getDescribe(String token) {
        this.playServiceMap.clear();
        this.describe = GameConsts.MyKnapsack.CHOOSE_ATTRIBUTE;
        int index = 1;
        for (ENPhantomAttribute enPhantomAttribute : ENPhantomAttribute.values()) {
            int number = (Integer) ReflectUtil.getFieldValue(playerPhantom, enPhantomAttribute.getValue());
            if (number > 1) {
                this.playServiceMap.put(IndexUtil.getIndex(index), new ResetAttributeServiceImpl(playerPhantom, enPhantomAttribute, goodsDetailDTO));
                index++;
            }
        }

    }

}
