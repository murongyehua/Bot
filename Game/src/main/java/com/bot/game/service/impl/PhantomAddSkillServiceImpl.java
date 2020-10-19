package com.bot.game.service.impl;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.GameChainCollector;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.UseGoodsDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * @author liul
 * @version 1.0 2020/10/19
 */
public class PhantomAddSkillServiceImpl extends CommonPlayer {

    private UseGoodsDTO useGoodsDTO;

    public PhantomAddSkillServiceImpl(UseGoodsDTO useGoodsDTO) {
        this.useGoodsDTO = useGoodsDTO;
        this.title = useGoodsDTO.getPlayerPhantom().getName();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String doPlay(String token) {
        // 控制下个指令只能是【0】
        GameChainCollector.supportPoint.put(token, Collections.singletonList(BaseConsts.Menu.ZERO));
        if (StrUtil.isEmpty(useGoodsDTO.getPlayerPhantom().getSkills())) {
            useGoodsDTO.getPlayerPhantom().setSkills(useGoodsDTO.getTargetId());
        }else {
            useGoodsDTO.getPlayerPhantom().setSkills(useGoodsDTO.getPlayerPhantom().getSkills() + StrUtil.COMMA + useGoodsDTO.getTargetId());
        }
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        playerPhantomMapper.updateByPrimaryKey(useGoodsDTO.getPlayerPhantom());
        PlayerGoods playerGoods = new PlayerGoods();
        playerGoods.setId(useGoodsDTO.getPlayerGoodsId());
        playerGoods.setNumber(useGoodsDTO.getNumber());
        CommonPlayer.afterUseGoods(playerGoods);
        return GameConsts.CommonTip.PLAY_SUCCESS;
    }

}
