package com.bot.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.dao.entity.BaseMonster;
import com.bot.game.dao.entity.BaseSkill;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.BaseSkillMapper;
import com.bot.game.dto.BattleMonsterDTO;
import com.bot.game.dto.BattlePhantomDTO;
import com.bot.game.dto.BattleSkillDTO;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/10/18
 */
public class BattleServiceImpl extends CommonPlayer {

    private BaseMonster baseMonster;

    private PlayerPhantom playerPhantom;

    private int round = 0;

    private List<BattleSkillDTO> playerSkills;

    private List<BattleSkillDTO> targetSkills;

    private List<String> playerBuffs;

    private List<String> playerDeBuffs;

    private List<String> targetBuffs;

    private List<String> targetDeBuffs;

    public BattleServiceImpl(BaseMonster baseMonster, PlayerPhantom playerPhantom) {
        this.title = String.format(GameConsts.Battle.TITLE,
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel());
        this.baseMonster = baseMonster;
        this.playerPhantom = playerPhantom;
    }

    @Override
    public String doPlay(String token) {
        BattleMonsterDTO battleMonsterDTO = getBattleMonster();
        BattlePhantomDTO battlePhantomDTO = getBattlePhantom(playerPhantom);

        if (StrUtil.isNotEmpty(battleMonsterDTO.getSkills())) {
            targetSkills = this.getBattleSkill(battleMonsterDTO.getSkills());
        }
        if (StrUtil.isNotEmpty(battlePhantomDTO.getSkills())) {
            targetSkills = this.getBattleSkill(battlePhantomDTO.getSkills());
        }

        return null;
    }

    private BattleMonsterDTO getBattleMonster() {
        BattleMonsterDTO battleMonsterDTO = new BattleMonsterDTO();
        BeanUtil.copyProperties(baseMonster, battleMonsterDTO);
        int canAddPoint = baseMonster.getGrow() * baseMonster.getLevel();
        List<Integer> list = spiltNumber(canAddPoint);
        battleMonsterDTO.setFinalAttack((baseMonster.getAttack() + list.get(0)) * GameConsts.BaseFigure.ATTACK_POINT +
                baseMonster.getLevel() * GameConsts.BaseFigure.ATTACK_FOR_EVERY_LEVEL);
        battleMonsterDTO.setFinalSpeed((baseMonster.getSpeed() + list.get(1)) * GameConsts.BaseFigure.SPEED_POINT +
                baseMonster.getLevel() * GameConsts.BaseFigure.SPEED_FOR_EVERY_LEVEL);
        battleMonsterDTO.setFinalDefense((baseMonster.getPhysique() + list.get(2)) * GameConsts.BaseFigure.DEFENSE_POINT +
                baseMonster.getLevel() * GameConsts.BaseFigure.DEFENSE_FOR_EVERY_LEVEL);
        battleMonsterDTO.setFinalDefense(baseMonster.getPhysique() * GameConsts.BaseFigure.HP_POINT +
                baseMonster.getLevel() * GameConsts.BaseFigure.HP_FOR_EVERY_LEVEL);
        return battleMonsterDTO;
    }

    private String doBattle(BattleMonsterDTO battleMonsterDTO, BattlePhantomDTO battlePhantomDTO) {
        round++;
        // 对比速度
        return null;
    }

    private List<BattleSkillDTO> getBattleSkill(String skills) {
        List<BattleSkillDTO> list = new LinkedList<>();
        BaseSkillMapper baseSkillMapper = (BaseSkillMapper) mapperMap.get(GameConsts.MapperName.BASE_SKILL);
        List<BaseSkill> monsterSkill = baseSkillMapper.getByIds(Arrays.asList(skills.split(StrUtil.COMMA)));
        monsterSkill.forEach(x -> {
            BattleSkillDTO battleSkillDTO = new BattleSkillDTO();
            BeanUtil.copyProperties(x, battleSkillDTO);
            list.add(battleSkillDTO);
        });
        return list;
    }


}
