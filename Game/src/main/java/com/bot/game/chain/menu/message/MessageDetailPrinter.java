package com.bot.game.chain.menu.message;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.BaseConsts;
import com.bot.common.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.BaseGoods;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.GoodsBox;
import com.bot.game.dao.entity.Message;
import com.bot.game.dao.mapper.BaseGoodsMapper;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.dao.mapper.GoodsBoxMapper;
import com.bot.game.dao.mapper.MessageMapper;
import com.bot.game.enums.ENMessageStatus;
import com.bot.game.enums.ENMessageType;

import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/11/14
 */
public class MessageDetailPrinter extends Menu {

    private final Message message;

    private boolean hasAttach;

    private List<GoodsBox> goodsBoxes;

    MessageDetailPrinter(Message message) {
        this.message = message;
        this.hasAttach = false;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        String sendName;
        if (ENMessageType.MESSAGE.getValue().equals(message.getType())) {
            GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
            GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(message.getSender());
            sendName = String.format(GameConsts.Message.FRIEND, gamePlayer.getNickname());
        }else {
            sendName = GameConsts.Message.SYSTEM;
        }
        GoodsBoxMapper goodsBoxMapper = (GoodsBoxMapper) mapperMap.get(GameConsts.MapperName.GOODS_BOX);
        GoodsBox param = new GoodsBox();
        param.setMessageId(message.getId());
        List<GoodsBox> list = goodsBoxMapper.selectBySelective(param);
        String attach = StrUtil.EMPTY;
        if (CollectionUtil.isNotEmpty(list)) {
            attach = "(含附件)";
            this.hasAttach = true;
            this.goodsBoxes = list;
        }
        this.menuName = String.format(GameConsts.Message.DETAIL_TITLE, sendName) + attach;
    }

    @Override
    public void getDescribe(String token) {
        this.describe = String.format(GameConsts.Message.MESSAGE_CONTENT, message.getContent(), message.getSendTime());
        if (hasAttach) {
            this.describe += GameConsts.Message.ATTACH;
            StringBuilder stringBuilder = new StringBuilder();
            for (GoodsBox goodsBox : goodsBoxes) {
                if (ENMessageType.GOODS.getValue().equals(goodsBox.getType())) {
                    BaseGoodsMapper baseGoodsMapper = (BaseGoodsMapper) mapperMap.get(GameConsts.MapperName.BASE_GOODS);
                    BaseGoods baseGoods = baseGoodsMapper.selectByPrimaryKey(goodsBox.getGoodId());
                    stringBuilder.append(baseGoods.getName()).append("*").append(goodsBox.getNumber()).append(StrUtil.CRLF);
                }else {
                    stringBuilder.append(ENMessageType.MONEY.getLabel()).append("*").append(goodsBox.getNumber()).append(StrUtil.CRLF);
                }
            }
            this.describe += stringBuilder.toString();
            this.menuChildrenMap.put(BaseConsts.Menu.ONE, new GetMessageAttachPrinter(goodsBoxes));
        }
        message.setStatus(ENMessageStatus.READ.getValue());
        MessageMapper messageMapper = (MessageMapper) mapperMap.get(GameConsts.MapperName.MESSAGE);
        messageMapper.updateByPrimaryKey(message);
    }

}
