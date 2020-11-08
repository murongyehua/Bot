package com.bot.game.service.impl;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.dao.entity.BaseSkill;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.GoodsDetailDTO;
import com.bot.game.enums.ENGoodEffect;
import org.apache.tomcat.util.buf.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/11/8
 */
public class ForgetSkillServiceImpl extends CommonPlayer{

    private final PlayerPhantom playerPhantom;

    private final BaseSkill baseSkill;

    private final GoodsDetailDTO goodsDetailDTO;

    public ForgetSkillServiceImpl(PlayerPhantom playerPhantom, BaseSkill baseSkill, GoodsDetailDTO goodsDetailDTO) {
        this.playerPhantom = playerPhantom;
        this.baseSkill = baseSkill;
        this.goodsDetailDTO = goodsDetailDTO;
        this.title = baseSkill.getName();
    }

    @Override
    public String doPlay(String token) {
        // 校验
        PlayerGoods playerGoods = checkGoodsNumber(token, ENGoodEffect.WAN_5);
        if (playerGoods == null) {
            return GameConsts.MyKnapsack.EMPTY + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK;
        }
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        String skills = playerPhantom.getSkills();
        String[] skillArray = skills.split(StrUtil.COMMA);
        List<String> skillList = new ArrayList<>(Arrays.asList(skillArray));
        skillList.remove(baseSkill.getId());
        playerPhantom.setSkills(StringUtils.join(skillList, StrUtil.C_COMMA));
        playerPhantomMapper.updateByPrimaryKey(playerPhantom);
        CommonPlayer.afterUseGoods(playerGoods);
        int nowNumber = goodsDetailDTO.getNumber() - 1;
        goodsDetailDTO.setNumber(Math.max(nowNumber, 0));
        return GameConsts.MyKnapsack.SKILL_FORGET_SUCCESS;
    }

}
