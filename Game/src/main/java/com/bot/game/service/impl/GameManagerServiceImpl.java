package com.bot.game.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.bot.common.constant.BaseConsts;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.dao.mapper.GoodsBoxMapper;
import com.bot.game.dao.mapper.MessageMapper;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.dto.AttachDTO;
import com.bot.game.dto.CompensateDTO;
import com.bot.game.dto.MessageDTO;
import com.bot.game.enums.ENMessageType;
import com.bot.game.service.GameManageService;
import com.bot.game.service.impl.message.SendMessageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/11/2
 */
@Service
public class GameManagerServiceImpl implements GameManageService {

    @Autowired
    private PlayerGoodsMapper playerGoodsMapper;

    @Autowired
    private GamePlayerMapper gamePlayerMapper;

    @Override
    public String compensate(CompensateDTO compensate) {
        List<GamePlayer> players = gamePlayerMapper.getBySoulPowerDesc();
        for (GamePlayer gamePlayer : players) {
            MessageDTO message = new MessageDTO();
            message.setContent(compensate.getContent());
            message.setTargetId(gamePlayer.getId());
            AttachDTO attachDTO = new AttachDTO();
            attachDTO.setGoodId(compensate.getGoodsId());
            attachDTO.setNumber(compensate.getNumber());
            message.setAttaches(Collections.singletonList(attachDTO));

            SendMessageServiceImpl.doSendMessage(message, "sys", ENMessageType.SYSTEM);
        }
        return BaseConsts.SystemManager.SUCCESS;
    }

    @Override
    public String compensateMoney(Integer money) {
        List<GamePlayer> list = gamePlayerMapper.getBySoulPowerDesc();
        list.forEach(x -> CommonPlayer.addOrSubMoney(x.getId(), money));
        return BaseConsts.SystemManager.SUCCESS;
    }
}
