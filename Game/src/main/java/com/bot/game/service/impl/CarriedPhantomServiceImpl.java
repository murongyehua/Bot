package com.bot.game.service.impl;

import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.GameConsts;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.enums.ENCarriedStatus;

/**
 * @author murongyehua
 * @version 1.0 2020/11/12
 */
public class CarriedPhantomServiceImpl extends CommonPlayer {

    private PlayerPhantom playerPhantom;

    public CarriedPhantomServiceImpl(PlayerPhantom playerPhantom) {
        this.playerPhantom = playerPhantom;
        this.title = ENCarriedStatus.getInvertByValue(playerPhantom.getCarried()).getLabel();
    }

    @Override
    public String doPlay(String token) {
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        playerPhantom.setCarried(ENCarriedStatus.getInvertByValue(playerPhantom.getCarried()).getValue());
        playerPhantomMapper.updateByPrimaryKey(playerPhantom);
        return GameConsts.MyPhantom.SUCCESS + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK;
    }

}
