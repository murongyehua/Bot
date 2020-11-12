package com.bot.game.service.impl;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.GameChainCollector;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.GoodsDetailDTO;
import com.bot.game.dto.UseGoodsDTO;
import com.bot.game.enums.ENGoodEffect;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * @author murongyehua
 * @version 1.0 2020/10/19
 */
public class PhantomAddSkillServiceImpl extends CommonPlayer {

    private UseGoodsDTO useGoodsDTO;

    private GoodsDetailDTO goodsDetailDTO;

    public PhantomAddSkillServiceImpl(UseGoodsDTO useGoodsDTO, GoodsDetailDTO goodsDetailDTO) {
        this.useGoodsDTO = useGoodsDTO;
        this.goodsDetailDTO = goodsDetailDTO;
        this.title = useGoodsDTO.getPlayerPhantom().getName();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String doPlay(String token) {
        // 控制下个指令只能是【0】
        GameChainCollector.supportPoint.put(token, Collections.singletonList(BaseConsts.Menu.ZERO));
        // 校验
        PlayerGoods playerGoods = checkGoodsNumber(token, ENGoodEffect.SKILL, goodsDetailDTO.getTargetId());
        if (playerGoods == null) {
            return GameConsts.MyKnapsack.EMPTY + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK;
        }
        if (StrUtil.isEmpty(useGoodsDTO.getPlayerPhantom().getSkills())) {
            useGoodsDTO.getPlayerPhantom().setSkills(useGoodsDTO.getTargetId());
        }else {
            useGoodsDTO.getPlayerPhantom().setSkills(useGoodsDTO.getPlayerPhantom().getSkills() + StrUtil.COMMA + useGoodsDTO.getTargetId());
        }
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        playerPhantomMapper.updateByPrimaryKey(useGoodsDTO.getPlayerPhantom());
        // 扣除
        CommonPlayer.afterUseGoods(playerGoods);
        int nowNumber = goodsDetailDTO.getNumber() - 1;
        goodsDetailDTO.setNumber(Math.max(nowNumber, 0));
        return GameConsts.CommonTip.PLAY_SUCCESS;
    }

}
