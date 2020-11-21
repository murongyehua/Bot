package com.bot.game.service.impl.message;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.GameConsts;
import com.bot.game.chain.menu.message.WriteMessageMenuPrinter;
import com.bot.game.dao.entity.BaseGoods;
import com.bot.game.dao.entity.GoodsBox;
import com.bot.game.dao.entity.Message;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.mapper.BaseGoodsMapper;
import com.bot.game.dao.mapper.GoodsBoxMapper;
import com.bot.game.dao.mapper.MessageMapper;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.dto.AttachDTO;
import com.bot.game.dto.MessageDTO;
import com.bot.game.enums.ENAppellation;
import com.bot.game.enums.ENMessageStatus;
import com.bot.game.enums.ENMessageType;
import com.bot.game.enums.ENWriteMessageStatus;
import com.bot.game.service.impl.CommonPlayer;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liul
 * @version 1.0 2020/11/17
 */
public class SendMessageServiceImpl extends CommonPlayer {

    private static final String ATTACH_SPILT;

    static {
        ATTACH_SPILT = "\\*";
    }

    public static String trySendMessage(String token, String content) {
        MessageDTO message = WriteMessageMenuPrinter.WRITE_MESSAGE.get(token);
        switch (message.getEnWriteMessageStatus()) {
            case WAIT_CONTENT:
                message.setContent(content);
                message.setEnWriteMessageStatus(ENWriteMessageStatus.WAIT_ASK_NEED_ATTACH);
                return GameConsts.Message.ASK_NEED_ATTACH;
            case WAIT_ASK_NEED_ATTACH:
                if (ObjectUtil.equal(content, GameConsts.Message.YES)) {
                    // 选择
                    message.setEnWriteMessageStatus(ENWriteMessageStatus.WAIT_ATTACH);
                    return GameConsts.Message.WAIT_ATTACH;
                } else if (ObjectUtil.equal(content, GameConsts.Message.NO)) {
                    // 移除
                    WriteMessageMenuPrinter.WRITE_MESSAGE.remove(token);
                    // 发送
                    return doSendMessage(message, token, ENMessageType.MESSAGE);
                } else {
                    return GameConsts.Message.NOT_YES_NO;
                }
            case WAIT_ATTACH:
                String result = addAttach(message, content, token);
                if (StrUtil.isNotEmpty(result)) {
                    return result;
                }
                message.setEnWriteMessageStatus(ENWriteMessageStatus.WAIT_ASK_NEED_ATTACH);
                return GameConsts.Message.ASK_NEED_ATTACH;
            default:
                return StrUtil.EMPTY;
        }
    }

    public static String doSendMessage(MessageDTO message, String token, ENMessageType enMessageType) {
        // 真正发送的方法
        Message sendMessage = new Message();
        String messageId = IdUtil.simpleUUID();
        sendMessage.setId(messageId);
        sendMessage.setStatus(ENMessageStatus.NOT_READ.getValue());
        sendMessage.setReceiver(message.getTargetId());
        sendMessage.setContent(message.getContent());
        sendMessage.setSender(token);
        sendMessage.setType(enMessageType.getValue());
        sendMessage.setSendTime(DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN));
        MessageMapper messageMapper = (MessageMapper) mapperMap.get(GameConsts.MapperName.MESSAGE);
        messageMapper.insert(sendMessage);
        GoodsBoxMapper goodsBoxMapper = (GoodsBoxMapper) mapperMap.get(GameConsts.MapperName.GOODS_BOX);
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        message.getAttaches().forEach(x -> {
            GoodsBox goodsBox = new GoodsBox();
            goodsBox.setId(IdUtil.simpleUUID());
            goodsBox.setStatus(ENMessageStatus.NOT_READ.getValue());
            goodsBox.setMessageId(messageId);
            goodsBox.setGoodId(x.getGoodId());
            goodsBox.setNumber(x.getNumber());
            goodsBox.setPlayerId(message.getTargetId());
            goodsBox.setType(ENMessageType.GOODS.getValue());
            goodsBoxMapper.insert(goodsBox);
            if (!"sys".equals(token)) {
                PlayerGoods param = new PlayerGoods();
                param.setPlayerId(token);
                param.setGoodId(x.getGoodId());
                CommonPlayer.afterUseGoods(playerGoodsMapper.selectBySelective(param).get(0), x.getNumber());
            }
        });
        String temp = StrUtil.EMPTY;
        if (!CommonPlayer.isAppellationExist(ENAppellation.A11, token)) {
            CommonPlayer.addAppellation(ENAppellation.A11, token);
            temp = "恭喜你，获得了[" + ENAppellation.A11 + "]的称号!!";
        }
        return temp + GameConsts.Message.SEND_SUCCESS;
    }

    private static String addAttach(MessageDTO message, String content, String token) {
        // 校验格式
        List<String> contents = Arrays.asList(content.split(ATTACH_SPILT));
        if (contents.size() != 2 || StrUtil.isEmpty(contents.get(0)) || StrUtil.isEmpty(contents.get(1))) {
            return GameConsts.Message.ERROR_ATTACH;
        }
        String name = contents.get(0);
        int number;
        try {
            number = Integer.parseInt(contents.get(1));
        }catch (Exception e) {
            return GameConsts.Message.NUMBER_FORMAT_ERROR;
        }
        // 校验数量
        if (number == 0) {
            return GameConsts.Message.NUMBER_ZERO;
        }
        BaseGoodsMapper baseGoodsMapper = (BaseGoodsMapper) mapperMap.get(GameConsts.MapperName.BASE_GOODS);
        BaseGoods param = new BaseGoods();
        param.setName(name);
        List<BaseGoods> baseGoodsList = baseGoodsMapper.selectBySelective(param);
        if (CollectionUtil.isEmpty(baseGoodsList)) {
            return GameConsts.Message.ERROR_GOODS_NAME;
        }
        BaseGoods goods = baseGoodsList.get(0);
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        PlayerGoods playerGoods = new PlayerGoods();
        playerGoods.setGoodId(goods.getId());
        playerGoods.setPlayerId(token);
        List<PlayerGoods> playerGoodsList = playerGoodsMapper.selectBySelective(playerGoods);
        if (CollectionUtil.isEmpty(playerGoodsList)) {
            return GameConsts.Message.NOT_HAVE;
        }
        PlayerGoods targetGoods = playerGoodsList.get(0);
        if (targetGoods.getNumber() < number) {
            return GameConsts.Message.NUMBER_NOT;
        }
        // 校验是否已添加
        List<AttachDTO> hasList = message.getAttaches().stream().filter(x -> x.getGoodId().equals(goods.getId())).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(hasList)) {
            return GameConsts.Message.HAS_ADD_ATTACH;
        }
        // 添加
        AttachDTO attachDTO = new AttachDTO();
        attachDTO.setGoodId(goods.getId());
        attachDTO.setNumber(number);
        message.getAttaches().add(attachDTO);
        return StrUtil.EMPTY;
    }

}
