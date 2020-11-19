package com.bot.game.chain.menu;

import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.GameConsts;
import com.bot.common.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.BaseSkill;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.BaseSkillMapper;
import com.bot.game.dto.GoodsDetailDTO;
import com.bot.game.enums.ENRarity;
import com.bot.game.service.impl.ForgetSkillServiceImpl;

import java.util.Arrays;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/11/8
 */
public class ForgetSkillPrinter extends Menu {

    private final PlayerPhantom playerPhantom;

    private final GoodsDetailDTO goodsDetailDTO;


    ForgetSkillPrinter(PlayerPhantom playerPhantom, GoodsDetailDTO goodsDetailDTO) {
        this.playerPhantom = playerPhantom;
        this.goodsDetailDTO = goodsDetailDTO;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = String.format(GameConsts.PhantomDetail.MENU_NAME_NO_CARRIED, ENRarity.getLabelByValue(playerPhantom.getRarity()),
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel(), playerPhantom.getAttribute());
    }

    @Override
    public void getDescribe(String token) {
        this.playServiceMap.clear();
        if (StrUtil.isEmpty(playerPhantom.getSkills())) {
            this.describe = GameConsts.MyKnapsack.NO_SKILL;
            return;
        }
        this.describe = GameConsts.MyKnapsack.CHOOSE_SKILL;
        BaseSkillMapper baseSkillMapper = (BaseSkillMapper) mapperMap.get(GameConsts.MapperName.BASE_SKILL);
        String[] skillIds = playerPhantom.getSkills().split(StrUtil.COMMA);
        List<BaseSkill> baseSkills = baseSkillMapper.getByIds(Arrays.asList(skillIds));
        int index = 1;
        for (BaseSkill baseSkill : baseSkills) {
            this.playServiceMap.put(IndexUtil.getIndex(index), new ForgetSkillServiceImpl(playerPhantom, baseSkill, goodsDetailDTO));
            index++;
        }

    }

}
