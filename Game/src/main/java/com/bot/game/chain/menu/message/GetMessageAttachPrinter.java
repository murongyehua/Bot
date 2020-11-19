package com.bot.game.chain.menu.message;

import com.bot.common.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.GoodsBox;
import com.bot.game.dao.mapper.GoodsBoxMapper;
import com.bot.game.enums.ENMessageStatus;
import com.bot.game.enums.ENMessageType;
import com.bot.game.service.impl.CommonPlayer;

import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/11/14
 */
public class GetMessageAttachPrinter extends Menu {

    private final List<GoodsBox> goodsBoxes;

    GetMessageAttachPrinter(List<GoodsBox> goodsBoxes) {
        this.goodsBoxes = goodsBoxes;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.Message.GET_ATTACH;
    }

    @Override
    public void getDescribe(String token) {
        if (ENMessageStatus.READ.getValue().equals(goodsBoxes.get(0).getStatus())) {
            this.describe = GameConsts.Message.ATTACH_REPEAT;
            return;
        }
        for (GoodsBox goodsBox : goodsBoxes) {
            if (ENMessageType.MONEY.getValue().equals(goodsBox.getType())) {
                CommonPlayer.addOrSubMoney(token, goodsBox.getNumber());
            }else {
                CommonPlayer.addPlayerGoods(goodsBox.getGoodId(), token, goodsBox.getNumber());
            }
            GoodsBoxMapper goodsBoxMapper = (GoodsBoxMapper) mapperMap.get(GameConsts.MapperName.GOODS_BOX);
            goodsBox.setStatus(ENMessageStatus.READ.getValue());
            goodsBoxMapper.updateByPrimaryKey(goodsBox);
        }
        this.describe = "接收成功，物品已放入背包";
    }


}
