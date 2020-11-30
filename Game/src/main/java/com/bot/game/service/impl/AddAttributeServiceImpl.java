package com.bot.game.service.impl;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.GameConsts;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.GoodsDetailDTO;
import com.bot.game.enums.ENGoodEffect;
import com.bot.game.enums.ENPhantomAttribute;

/**
 * @author murongyehua
 * @version 1.0 2020/11/29
 */
public class AddAttributeServiceImpl extends CommonPlayer {

    private final PlayerPhantom playerPhantom;

    private final ENPhantomAttribute enPhantomAttribute;

    private final GoodsDetailDTO goodsDetailDTO;

    public AddAttributeServiceImpl(PlayerPhantom playerPhantom, ENPhantomAttribute enPhantomAttribute, GoodsDetailDTO goodsDetailDTO) {
        this.playerPhantom = playerPhantom;
        this.enPhantomAttribute = enPhantomAttribute;
        this.goodsDetailDTO = goodsDetailDTO;
        this.title = String.format(GameConsts.MyKnapsack.TITLE, enPhantomAttribute.getLabel(), ReflectUtil.getFieldValue(playerPhantom, enPhantomAttribute.getValue()));
    }

    @Override
    public String doPlay(String token) {
        // 校验
        PlayerGoods playerGoods = checkGoodsNumber(token, ENGoodEffect.WAN_8, null);
        if (playerGoods == null) {
            return GameConsts.MyKnapsack.EMPTY + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK;
        }
        int subNumber = (Integer) ReflectUtil.getFieldValue(playerPhantom, enPhantomAttribute.getValue());
        ReflectUtil.setFieldValue(playerPhantom, enPhantomAttribute.getValue(), subNumber + 1);
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        playerPhantomMapper.updateByPrimaryKey(playerPhantom);
        // 扣除
        CommonPlayer.afterUseGoods(playerGoods, 1);
        int nowNumber = goodsDetailDTO.getNumber() - 1;
        goodsDetailDTO.setNumber(Math.max(nowNumber, 0));
        return GameConsts.MyKnapsack.BUFF_USE + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK;
    }

}
