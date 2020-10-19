package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.BaseSkill;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.BaseSkillMapper;
import com.bot.game.enums.ENArea;
import com.bot.game.enums.ENCamp;
import com.bot.game.enums.ENRarity;
import com.bot.game.enums.ENSkillEffect;

import java.util.Arrays;
import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/10/16
 */
public class PhantomDetailMenuPrinter extends Menu {

    private PlayerPhantom playerPhantom;

    PhantomDetailMenuPrinter(PlayerPhantom playerPhantom) {
        this.playerPhantom = playerPhantom;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = String.format(GameConsts.PhantomDetail.MENU_NAME,
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel());
    }

    @Override
    public void getDescribe(String token) {
        this.describe = String.format(GameConsts.PhantomDetail.DESCRIBE, playerPhantom.getName(),
                playerPhantom.getLevel(), ENRarity.getLabelByValue(playerPhantom.getRarity()),
                playerPhantom.getAppellation(), playerPhantom.getAttribute(), ENCamp.getLabelByValue(playerPhantom.getCamp()),
                ENArea.getLabelByValue(playerPhantom.getArea()), playerPhantom.getSpeed(), playerPhantom.getAttack(),
                playerPhantom.getPhysique(), playerPhantom.getGrow(), playerPhantom.getDescribe(), playerPhantom.getHp(), playerPhantom.getExp(), this.getSkillDescribe(playerPhantom.getSkills()));
    }

    private String getSkillDescribe(String skills) {
        if (StrUtil.isEmpty(skills)) {
            return GameConsts.CommonTip.EMPTY;
        }
        List<String> skillList = Arrays.asList(skills.split(StrUtil.COMMA));
        BaseSkillMapper baseSkillMapper = (BaseSkillMapper) mapperMap.get(GameConsts.MapperName.BASE_SKILL);
        List<BaseSkill> list = baseSkillMapper.getByIds(skillList);
        if (CollectionUtil.isEmpty(list)) {
            return GameConsts.CommonTip.EMPTY;
        }
        StringBuilder stringBuilder = new StringBuilder();
        list.forEach(x -> {
            stringBuilder.append(StrUtil.CRLF).append(String.format(GameConsts.CommonTip.SKILL_DESCRIBE, x.getName()));
            if (StrUtil.isNotEmpty(x.getEffect())) {
                stringBuilder.append(ENSkillEffect.getLabelByValue(x.getEffect())).append(StrUtil.COMMA);
            }
            if (StrUtil.isNotEmpty(x.getDebuff())) {
                stringBuilder.append(ENSkillEffect.getLabelByValue(x.getDebuff())).append(StrUtil.COMMA);
            }
            if (x.getRound() != 0) {
                stringBuilder.append(String.format(GameConsts.CommonTip.SKILL_ROUND, x.getRound())).append(StrUtil.COMMA);
            }
            stringBuilder.append(String.format(GameConsts.CommonTip.SKILL_WAIT_ROUND, x.getWaitRound()));
        });
        return stringBuilder.toString();
    }

}
