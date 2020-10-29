package com.bot.game.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.dao.entity.BaseGoods;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.BaseGoodsMapper;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.GoodsDetailDTO;
import com.bot.game.enums.ENGoodEffect;
import com.bot.game.enums.ENPhantomAttribute;

import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/29
 */
public class ResetAttributeServiceImpl extends CommonPlayer {

    private PlayerPhantom playerPhantom;

    private ENPhantomAttribute enPhantomAttribute;

    private GoodsDetailDTO goodsDetailDTO;

    public ResetAttributeServiceImpl(PlayerPhantom playerPhantom, ENPhantomAttribute enPhantomAttribute, GoodsDetailDTO goodsDetailDTO) {
        this.playerPhantom = playerPhantom;
        this.enPhantomAttribute = enPhantomAttribute;
        this.goodsDetailDTO = goodsDetailDTO;
        this.title = String.format(GameConsts.MyKnapsack.TITLE, enPhantomAttribute.getLabel(), ReflectUtil.getFieldValue(playerPhantom, enPhantomAttribute.getValue()));
    }

    @Override
    public String doPlay(String token) {
        // 校验
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        BaseGoodsMapper baseGoodsMapper = (BaseGoodsMapper) mapperMap.get(GameConsts.MapperName.BASE_GOODS);
        BaseGoods baseGoods = new BaseGoods();
        baseGoods.setEffect(ENGoodEffect.WAN_4.getValue());
        List<BaseGoods> baseGoodsList = baseGoodsMapper.selectBySelective(baseGoods);
        PlayerGoods playerGoods = new PlayerGoods();
        playerGoods.setPlayerId(token);
        playerGoods.setGoodId(baseGoodsList.get(0).getId());
        List<PlayerGoods> list = playerGoodsMapper.selectBySelective(playerGoods);
        if (CollectionUtil.isEmpty(list) || list.get(0).getNumber() == 0) {
            return GameConsts.MyKnapsack.EMPTY + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK;
        }
        List<ENPhantomAttribute> enPhantomAttributes = ENPhantomAttribute.getWithOutOne(enPhantomAttribute);
        int subNumber = (Integer) ReflectUtil.getFieldValue(playerPhantom, enPhantomAttribute.getValue());
        ReflectUtil.setFieldValue(playerPhantom, enPhantomAttribute.getValue(), subNumber - 1);
        ENPhantomAttribute phantomAttribute = enPhantomAttributes.get(RandomUtil.randomInt(enPhantomAttributes.size()));
        int resetNumber = (Integer) ReflectUtil.getFieldValue(playerPhantom, phantomAttribute.getValue());
        ReflectUtil.setFieldValue(playerPhantom, phantomAttribute.getValue(), resetNumber + 1);
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        playerPhantomMapper.updateByPrimaryKey(playerPhantom);
        // 扣除
        CommonPlayer.afterUseGoods(list.get(0));
        int nowNumber = goodsDetailDTO.getNumber() - 1;
        goodsDetailDTO.setNumber(nowNumber < 0 ? 0 : nowNumber);
        return GameConsts.MyKnapsack.BUFF_USE + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK;
    }

}
