package com.bot.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.BaseConsts;
import com.bot.common.constant.GameConsts;
import com.bot.game.chain.GameChainCollector;
import com.bot.game.dao.entity.BaseGoods;
import com.bot.game.dao.entity.BaseMonster;
import com.bot.game.dao.entity.BaseSkill;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.BaseSkillMapper;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.BattleEffectDTO;
import com.bot.game.dto.BattleMonsterDTO;
import com.bot.game.dto.BattlePhantomDTO;
import com.bot.game.dto.BattleSkillDTO;
import com.bot.game.enums.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author murongyehua
 * @version 1.0 2020/10/18
 */
public class BattleServiceImpl extends CommonPlayer {

    private final BaseMonster baseMonster;

    private final PlayerPhantom playerPhantom;

    private final boolean isCompare;

    private final boolean isBoos;

    private boolean isDungeon;

    private int round = 0;

    private int allHurt = 0;

    private int startBoosHp;

    private final StringBuilder battleRecord = new StringBuilder();

    private String targetPlayerId;

    private int nowHp;


    public BattleServiceImpl(BaseMonster baseMonster, PlayerPhantom playerPhantom, boolean isCompare, boolean isBoos, boolean isDungeon) {
        this.title = String.format(GameConsts.Battle.TITLE,
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel());
        this.baseMonster = baseMonster;
        this.playerPhantom = playerPhantom;
        this.isCompare = isCompare;
        this.isBoos = isBoos;
        this.isDungeon = isDungeon;
    }

    public BattleServiceImpl(PlayerPhantom targetPhantom, PlayerPhantom playerPhantom, boolean isCompare, boolean isBoos) {
        BaseMonster baseMonster = new BaseMonster();
        BeanUtil.copyProperties(targetPhantom, baseMonster);
        this.title = String.format(GameConsts.Battle.TITLE,
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel());
        this.baseMonster = baseMonster;
        this.playerPhantom = playerPhantom;
        this.isCompare = isCompare;
        this.isBoos = isBoos;
        this.targetPlayerId = targetPhantom.getPlayerId();
    }

    public BattleServiceImpl(BaseMonster baseMonster, PlayerPhantom playerPhantom, boolean isDungeon, Integer nowHp) {
        this.title = String.format(GameConsts.Battle.TITLE,
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel());
        this.baseMonster = baseMonster;
        this.playerPhantom = playerPhantom;
        this.isCompare = false;
        this.isBoos = false;
        this.isDungeon = isDungeon;
        this.nowHp = nowHp;
    }

    @Override
    public String doPlay(String token) {
        // 控制下一次的指令只能输入【0】和【Q】指令
        List<String> points = new LinkedList<>();
        points.add(BaseConsts.Menu.ZERO);
        points.add(GameConsts.CommonTip.SEE_BATTLE_DETAIL);
        GameChainCollector.supportPoint.put(token, points);
        battleRecord.append(GameConsts.Battle.BATTLE_RECORD_FORMAT);
        BattleMonsterDTO battleMonsterDTO = getBattleMonster();
        BattlePhantomDTO playerDTO = getBattlePhantom();
        BattlePhantomDTO targetDTO = new BattlePhantomDTO();
        BeanUtil.copyProperties(battleMonsterDTO, targetDTO);
        playerDTO.setStop(false);
        targetDTO.setStop(false);
        if (StrUtil.isNotEmpty(targetDTO.getSkills())) {
            targetDTO.setSkillList(this.getBattleSkill(targetDTO.getSkills()));
        }
        if (StrUtil.isNotEmpty(playerDTO.getSkills())) {
            playerDTO.setSkillList(this.getBattleSkill(playerDTO.getSkills()));
        }
        String result = null;
        battleRecord.append(String.format(GameConsts.Battle.BATTLE_RECORD_START,
                playerDTO.getName(), playerDTO.getFinalHp(), targetDTO.getName(), targetDTO.getFinalHp()));
        if (isBoos) {
            startBoosHp = WorldBossServiceImpl.boos.getFinalHp();
        }
        this.dealWeapon(targetDTO, playerDTO);
        while (result == null) {
            result = this.doBattle(targetDTO, playerDTO);
        }
        battleRecord.append(GameConsts.Battle.END).append(StrUtil.CRLF);
        battleRecord.append(GameConsts.CommonTip.TURN_BACK);
        battleDetailMap.put(token, battleRecord.toString());
        return result;
    }

    private BattleMonsterDTO getBattleMonster() {
        BattleMonsterDTO battleMonsterDTO = new BattleMonsterDTO();
        BeanUtil.copyProperties(baseMonster, battleMonsterDTO);
        int canAddPoint = 0;
        if (!isCompare && !isBoos) {
            canAddPoint = baseMonster.getGrow() * baseMonster.getLevel();
        }
        List<Integer> list = spiltNumber(canAddPoint);
        int intAttack = (baseMonster.getAttack() + list.get(0)) * GameConsts.BaseFigure.ATTACK_POINT +
                baseMonster.getLevel() * GameConsts.BaseFigure.ATTACK_FOR_EVERY_LEVEL;
        Double figure = this.getFigure(baseMonster.getAttribute(), playerPhantom.getAttribute());
        double doubleAttack = intAttack * figure;
        battleMonsterDTO.setFinalAttack((int) doubleAttack);
        battleMonsterDTO.setFinalSpeed((baseMonster.getSpeed() + list.get(1)) * GameConsts.BaseFigure.SPEED_POINT +
                baseMonster.getLevel() * GameConsts.BaseFigure.SPEED_FOR_EVERY_LEVEL);
        battleMonsterDTO.setFinalDefense((baseMonster.getPhysique() + list.get(2)) * GameConsts.BaseFigure.DEFENSE_POINT +
                baseMonster.getLevel() * GameConsts.BaseFigure.DEFENSE_FOR_EVERY_LEVEL);
        if (!isBoos) {
            Integer hp = baseMonster.getPhysique() * GameConsts.BaseFigure.HP_POINT +
                    baseMonster.getLevel() * GameConsts.BaseFigure.HP_FOR_EVERY_LEVEL;
            battleMonsterDTO.setFinalHp(hp);
            battleMonsterDTO.setHp(hp);
        }
        if (isBoos) {
            battleMonsterDTO.setHp(WorldBossServiceImpl.boos.getFinalHp());
            battleMonsterDTO.setFinalHp(WorldBossServiceImpl.boos.getFinalHp());
        }
        if (isCompare) {
            battleMonsterDTO.setBattleWeaponDTO(getCurrentWeapon(targetPlayerId));
        }
        if (nowHp != 0) {
            battleMonsterDTO.setFinalHp(nowHp);
            battleMonsterDTO.setHp(nowHp);
        }
        return battleMonsterDTO;
    }

    private BattlePhantomDTO getBattlePhantom() {
        BattlePhantomDTO battlePhantomDTO = new BattlePhantomDTO();
        BeanUtil.copyProperties(playerPhantom, battlePhantomDTO);
        Double figure = this.getFigure(playerPhantom.getAttribute(), baseMonster.getAttribute());
        int intAttack = playerPhantom.getLevel() * GameConsts.BaseFigure.ATTACK_FOR_EVERY_LEVEL +
                playerPhantom.getAttack() * GameConsts.BaseFigure.ATTACK_POINT;
        double doubleAttack = intAttack * figure;
        battlePhantomDTO.setFinalAttack((int) doubleAttack);
        battlePhantomDTO.setFinalSpeed(playerPhantom.getLevel() * GameConsts.BaseFigure.SPEED_FOR_EVERY_LEVEL +
                playerPhantom.getSpeed() + GameConsts.BaseFigure.SPEED_POINT);
        battlePhantomDTO.setFinalDefense(playerPhantom.getLevel() * GameConsts.BaseFigure.DEFENSE_FOR_EVERY_LEVEL +
                playerPhantom.getPhysique() * GameConsts.BaseFigure.DEFENSE_POINT);
        battlePhantomDTO.setFinalHp(battlePhantomDTO.getHp());
        battlePhantomDTO.setBattleWeaponDTO(getCurrentWeapon(playerPhantom.getPlayerId()));
        return battlePhantomDTO;
    }

    private Double getFigure(String myAttribute, String targetAttribute) {
        Double figure = GameConsts.BaseFigure.BASE_ONE_NUMBER;
        if (ENAttribute.isBuff(myAttribute, targetAttribute)) {
            figure = GameConsts.BaseFigure.BASE_BUFF_FIGURE;
        }
        if (ENAttribute.isDeBuff(myAttribute, targetAttribute)) {
            figure = GameConsts.BaseFigure.BASE_DE_BUFF_FIGURE;
        }
        return figure;
    }

    private String doBattle(BattlePhantomDTO targetDTO, BattlePhantomDTO playerDTO) {
        round++;
        if (targetDTO.getFinalHp() <= 0 || playerDTO.getFinalHp() <= 0) {
            return this.getResult(playerDTO, targetDTO);
        }
        battleRecord.append(String.format(GameConsts.Battle.BATTLE_RECORD_ROUND, round));
        // 对比速度
        if (targetDTO.getFinalSpeed() > playerDTO.getFinalSpeed()) {
            doSingleBattle(targetDTO, playerDTO);
            doSingleBattle(playerDTO, targetDTO);
        }else {
            doSingleBattle(playerDTO, targetDTO);
            doSingleBattle(targetDTO, playerDTO);
        }
        battleRecord.append(String.format(GameConsts.Battle.BATTLE_RECORD_ROUND_RESULT,
                playerDTO.getName(), playerDTO.getFinalHp(), targetDTO.getName(), targetDTO.getFinalHp()));
        roundEnd(targetDTO, playerDTO);
        // 返回null 以继续
        return null;
    }

    private List<BattleSkillDTO> getBattleSkill(String skills) {
        List<BattleSkillDTO> list = new LinkedList<>();
        BaseSkillMapper baseSkillMapper = (BaseSkillMapper) mapperMap.get(GameConsts.MapperName.BASE_SKILL);
        List<BaseSkill> monsterSkill = baseSkillMapper.getByIds(Arrays.asList(skills.split(StrUtil.COMMA)));
        monsterSkill.forEach(x -> {
            BattleSkillDTO battleSkillDTO = new BattleSkillDTO();
            BeanUtil.copyProperties(x, battleSkillDTO);
            battleSkillDTO.setNowWaitRound(0);
            list.add(battleSkillDTO);
        });
        return list;
    }

    private void roundEnd(BattlePhantomDTO targetDTO, BattlePhantomDTO playerDTO) {
        playerDTO.getSkillList().forEach(skill -> {
            if (skill.getNowWaitRound() != 0) {
                skill.setNowWaitRound(skill.getNowWaitRound() - 1);
            }
        });
        targetDTO.getSkillList().forEach(skill -> {
            if (skill.getNowWaitRound() != 0) {
                skill.setNowWaitRound(skill.getNowWaitRound() - 1);
            }
        });
    }


    private void useSkill(BattlePhantomDTO nowAttackPhantom, BattlePhantomDTO another, BattleSkillDTO skill) {
        battleRecord.append(String.format(GameConsts.Battle.BATTLE_RECORD_PHANTOM, nowAttackPhantom.getName(), skill.getName()));
        ENSkillEffect enSkillEffect = ENSkillEffect.getByValue(skill.getEffect());
        if (enSkillEffect.getBuffStatus() == null) {
            this.normalAttack(nowAttackPhantom, another, skill);
            return;
        }
        BattleEffectDTO battleEffectDTO = new BattleEffectDTO();
        battleEffectDTO.setEffect(enSkillEffect);
        battleEffectDTO.setLiveRound(skill.getRound());
        if (GameConsts.Battle.BUFF.equals(enSkillEffect.getBuffStatus())) {
            boolean hasEffect = false;
            for (BattleEffectDTO battleEffect : nowAttackPhantom.getBuffs()) {
                if (battleEffect.getEffect().equals(battleEffectDTO.getEffect())) {
                    battleEffect.setLiveRound(battleEffectDTO.getLiveRound());
                    hasEffect = true;
                    break;
                }
            }
            if (!hasEffect) {
                nowAttackPhantom.getBuffs().add(battleEffectDTO);
            }
        }else {
            boolean hasEffect = false;
            for (BattleEffectDTO battleEffect : another.getDeBuffs()) {
                if (battleEffect.getEffect().equals(battleEffectDTO.getEffect())) {
                    battleEffect.setLiveRound(battleEffectDTO.getLiveRound());
                    hasEffect = true;
                    break;
                }
            }
            boolean isCanNotAddDeBuff = false;
            List<ENSkillEffect> enSkillEffects = another.getBuffs().stream().map(BattleEffectDTO::getEffect).collect(Collectors.toList());
            if (enSkillEffects.contains(ENSkillEffect.W05)) {
                battleRecord.append("解灵囊生效，此回合").append(another.getName()).append("将免疫所有负面效果").append(StrUtil.CRLF);
                isCanNotAddDeBuff = true;
            }
            if (!hasEffect && !isCanNotAddDeBuff) {
                another.getDeBuffs().add(battleEffectDTO);
            }
        }
        this.normalAttack(nowAttackPhantom, another, null);
    }

    private void normalAttack(BattlePhantomDTO nowAttackPhantom, BattlePhantomDTO another, BattleSkillDTO skill) {
        BattlePhantomDTO tempPhantom = new BattlePhantomDTO();
        BeanUtil.copyProperties(nowAttackPhantom, tempPhantom);
        BattlePhantomDTO tempAnother = new BattlePhantomDTO();
        BeanUtil.copyProperties(another, tempAnother);
        this.buffDone(tempPhantom, tempAnother, skill, ENEffectType.PRE, 0);
        if (tempPhantom.getStop()) {
            battleRecord.append(tempPhantom.getName()).append("此回合停止行动，攻击无效").append(StrUtil.CRLF);
            nowAttackPhantom.setStop(false);
            nowAttackPhantom.setBuffs(tempPhantom.getBuffs());
            nowAttackPhantom.setDeBuffs(tempPhantom.getDeBuffs());
            another.setStop(tempAnother.getStop());
            another.setBuffs(tempAnother.getBuffs());
            another.setDeBuffs(tempAnother.getDeBuffs());
            return;
        }
        this.finalAttack(tempPhantom, tempAnother);
        nowAttackPhantom.setFinalHp(tempPhantom.getFinalHp());
        nowAttackPhantom.setBuffs(tempPhantom.getBuffs());
        nowAttackPhantom.setDeBuffs(tempPhantom.getDeBuffs());
        another.setFinalHp(tempAnother.getFinalHp());
        another.setStop(tempAnother.getStop());
        another.setBuffs(tempAnother.getBuffs());
        another.setDeBuffs(tempAnother.getDeBuffs());
    }


    private void finalAttack(BattlePhantomDTO tempPhantom, BattlePhantomDTO tempAnother) {
        int hurt = tempPhantom.getFinalAttack() - tempAnother.getFinalDefense();
        if (hurt < 1) {
            hurt = 1;
        }
        List<ENSkillEffect> enSkillEffects = tempAnother.getBuffs().stream().map(BattleEffectDTO::getEffect).collect(Collectors.toList());
        if (enSkillEffects.contains(ENSkillEffect.W04)) {
            battleRecord.append("醒世符生效，此回合").append(tempAnother.getName()).append("免疫伤害(不含DOT)").append(StrUtil.CRLF);
            hurt = 0;
        }
        tempAnother.setFinalHp(tempAnother.getFinalHp() - hurt);
        battleRecord.append(tempPhantom.getName()).append("此回合的攻击造成了").append(hurt).append("点伤害").append(StrUtil.CRLF);
        this.buffDone(tempAnother, tempPhantom, null, ENEffectType.DEFENSE, hurt);
        this.buffDone(tempPhantom, tempAnother, null, ENEffectType.END, hurt);
    }


    private void doSingleBattle(BattlePhantomDTO nowAttackPhantom, BattlePhantomDTO another) {
        if (nowAttackPhantom.getFinalHp() <= 0 || another.getFinalHp() <= 0) {
            return;
        }
        for (BattleSkillDTO skill : nowAttackPhantom.getSkillList()) {
            List<ENSkillEffect> enSkillEffects = nowAttackPhantom.getDeBuffs().stream().map(BattleEffectDTO::getEffect).collect(Collectors.toList());
            if (enSkillEffects.contains(ENSkillEffect.W06)) {
                battleRecord.append("禁灵镜生效，此回合").append(nowAttackPhantom.getName()).append("不能使用技能").append(StrUtil.CRLF);
                break;
            }
            if (skill.getNowWaitRound() == 0) {
                this.useSkill(nowAttackPhantom, another, skill);
                skill.setNowWaitRound(skill.getWaitRound());
                return;
            }
        }
        battleRecord.append(String.format(GameConsts.Battle.BATTLE_RECORD_PHANTOM, nowAttackPhantom.getName(), GameConsts.Battle.ATTACK));
        this.normalAttack(nowAttackPhantom, another, null);
    }

    private String getResult(BattlePhantomDTO playerDto, BattlePhantomDTO targetDto) {
        if (isDungeon) {
            // 返回Boos生命值和参战幻灵的生命值
            return targetDto.getFinalHp() + StrUtil.UNDERLINE + playerDto.getFinalHp();
        }
        if (isBoos) {
            if (targetDto.getFinalHp() < 0) {
                WorldBossServiceImpl.boos.setFinalHp(0);
            }else {
                WorldBossServiceImpl.boos.setFinalHp(targetDto.getFinalHp());
            }
            int endBoosHp = WorldBossServiceImpl.boos.getFinalHp();
            allHurt = startBoosHp - endBoosHp;
            StringBuilder stringBuilder = new StringBuilder();
            this.doGetBoosGoods(stringBuilder, playerDto);
            return stringBuilder.toString();
        }
        if (targetDto.getFinalHp() <= 0) {
            if (isCompare) {
                return GameConsts.Battle.SUCCESS + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK;
            }
            return GameConsts.Battle.SUCCESS + StrUtil.CRLF +
                    this.afterBattleSuccessResult(playerDto, targetDto, targetDto.getArea()) + GameConsts.CommonTip.TURN_BACK;
        }else {
            return GameConsts.Battle.FAIL + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK;
        }
    }

    private void buffDone(BattlePhantomDTO tempPhantom, BattlePhantomDTO another, BattleSkillDTO skill, ENEffectType enEffectType, Integer hurt) {
        if (skill == null && CollectionUtil.isEmpty(tempPhantom.getBuffs()) && CollectionUtil.isEmpty(tempPhantom.getDeBuffs())) {
            return;
        }
        String skillValue;
        if (skill != null) {
            skillValue = skill.getEffect();
            this.finalBuffDone(tempPhantom, another, ENSkillEffect.getByValue(skillValue), hurt);
        }else if (CollectionUtil.isNotEmpty(tempPhantom.getBuffs())){
            List<BattleEffectDTO> needRemoveList = new ArrayList<>();
            for (BattleEffectDTO effectDTO : tempPhantom.getBuffs()) {
                if (effectDTO.getEffect().getEnEffectType().equals(enEffectType)) {
                    this.finalBuffDone(tempPhantom, another, effectDTO.getEffect(), hurt);
                    effectDTO.setLiveRound(effectDTO.getLiveRound() - 1);
                    if (effectDTO.getLiveRound() < 1) {
                        needRemoveList.add(effectDTO);
                    }
                }
            }
            tempPhantom.getBuffs().removeAll(needRemoveList);
        }

        if (CollectionUtil.isNotEmpty(tempPhantom.getDeBuffs())) {
            List<BattleEffectDTO> needRemoveList = new ArrayList<>();
            for (BattleEffectDTO effectDTO : tempPhantom.getDeBuffs()) {
                if (effectDTO.getEffect().getEnEffectType().equals(enEffectType)) {
                    this.finalBuffDone(tempPhantom, another, effectDTO.getEffect(), hurt);
                    effectDTO.setLiveRound(effectDTO.getLiveRound() - 1);
                    if (effectDTO.getLiveRound() < 1) {
                        needRemoveList.add(effectDTO);
                    }
                }
            }
            tempPhantom.getDeBuffs().removeAll(needRemoveList);
        }
    }

    /**
     * 触发
     * @param phantom
     * @param another
     * @param enSkillEffect
     * @param hurt
     */
    private void finalBuffDone(BattlePhantomDTO phantom, BattlePhantomDTO another, ENSkillEffect enSkillEffect, Integer hurt) {
        switch (enSkillEffect) {
            case A01:
                phantom.setFinalAttack(phantom.getFinalAttack() * 2);
                break;
            case A02:
                double tempA02 = phantom.getFinalAttack() * 1.5;
                phantom.setFinalAttack((int) tempA02);
                break;
            case A03:
                double tempA03 = phantom.getFinalDefense() * 0.7;
                phantom.setFinalDefense((int) tempA03);
                phantom.setFinalAttack(phantom.getFinalAttack() * 3);
                break;
            case A04:
                if (RandomUtil.randomInt(101) <= GameConsts.BaseFigure.HALF) {
                    phantom.setFinalAttack(phantom.getFinalAttack() * 3);
                    battleRecord.append("判定成功，").append(phantom.getName()).append("此回合将造成三倍伤害");
                }
                break;
            case B01:
                double tempB01 = phantom.getFinalHp() * 0.05;
                phantom.setFinalHp(phantom.getFinalHp() - (int) tempB01);
                another.setFinalHp(another.getFinalHp() - (int) tempB01);
                battleRecord.append("技能扣除了").append(phantom.getName()).append((int) tempB01).append("点生命值，并且给").append(another.getName()).append("造成了等额伤害").append(StrUtil.CRLF);
                break;
            case B02:
                double tempB02 = phantom.getFinalHp() * 0.10;
                phantom.setFinalHp(phantom.getFinalHp() - (int) tempB02);
                another.setFinalHp(another.getFinalHp() - (int) tempB02);
                battleRecord.append("技能扣除了").append(phantom.getName()).append((int) tempB02).append("点生命值，并且给").append(another.getName()).append("造成了等额伤害").append(StrUtil.CRLF);
                break;
            case C01:
                double tempC01 = phantom.getFinalAttack() * 0.1;
                phantom.setFinalAttack(phantom.getFinalAttack() - (int) tempC01);
                battleRecord.append(phantom.getName()).append("的攻击力降低了").append((int) tempC01).append("点").append(StrUtil.CRLF);
                break;
            case C02:
                double tempC02 = phantom.getFinalAttack() * 0.2;
                phantom.setFinalAttack(phantom.getFinalAttack() - (int) tempC02);
                battleRecord.append(phantom.getName()).append("的攻击力降低了").append((int) tempC02).append("点").append(StrUtil.CRLF);
                break;
            case C03:
                double tempC03 = phantom.getFinalDefense() * 0.1;
                phantom.setFinalDefense(phantom.getFinalDefense() - (int) tempC03);
                battleRecord.append(phantom.getName()).append("的防御力降低了").append((int) tempC03).append("点").append(StrUtil.CRLF);
                break;
            case C04:
                double tempC04 = phantom.getFinalDefense() * 0.2;
                phantom.setFinalDefense(phantom.getFinalDefense() - (int) tempC04);
                battleRecord.append(phantom.getName()).append("的防御力降低了").append((int) tempC04).append("点").append(StrUtil.CRLF);
                break;
            case C05:
                phantom.setStop(true);
                break;
            case C06:
                double tempC06 = phantom.getHp() * 0.02;
                if (tempC06 > another.getFinalAttack() * 3.5) {
                    tempC06 = another.getFinalAttack() * 3.5;
                }
                phantom.setFinalHp(phantom.getFinalHp() - (int) tempC06);
                battleRecord.append("DOT触发,").append(phantom.getName()).append("扣除血量").append((int) tempC06).append("点").append(StrUtil.CRLF);
                break;
            case C07:
                double tempC07 = phantom.getFinalHp() * 0.05;
                if (tempC07 > another.getFinalAttack() * 3.5) {
                    tempC07 = another.getFinalAttack() * 3.5;
                }
                phantom.setFinalHp(phantom.getFinalHp() - (int) tempC07);
                battleRecord.append("DOT触发,").append(phantom.getName()).append("扣除血量").append((int) tempC07).append("点").append(StrUtil.CRLF);
                break;
            case C08:
                double tempC08 = hurt * 0.3;
                another.setFinalHp(another.getFinalHp() - (int) tempC08);
                battleRecord.append(another.getName()).append("受到反弹伤害").append((int) tempC08).append("点").append(StrUtil.CRLF);
                break;
            case C09:
                if (phantom.getFinalHp() > 0) {
                    double tempC09 = (phantom.getHp() - phantom.getFinalHp()) * 0.1;
                    phantom.setFinalHp(phantom.getFinalHp() + (int) tempC09);
                    battleRecord.append(phantom.getName()).append("回复生命值").append((int) tempC09).append("点").append(StrUtil.CRLF);
                }
                break;
            case C10:
                double tempC10 = phantom.getFinalSpeed() * 0.05;
                phantom.setFinalSpeed(phantom.getFinalSpeed() - (int) tempC10);
                break;
            case D01:
                if (another.getBuffs().size() > 0) {
                    BattleEffectDTO battleEffectDTO = another.getBuffs().get(RandomUtil.randomInt(another.getBuffs().size()));
                    phantom.getBuffs().add(battleEffectDTO);
                    another.getBuffs().remove(battleEffectDTO);
                    battleRecord.append(phantom.getName()).append("成功盗取了").append(another.getName()).append("的增益效果").append(StrUtil.CRLF);
                }else {
                    battleRecord.append(another.getName()).append("没有可供盗取的增益效果").append(StrUtil.CRLF);
                }
                break;
            case D02:
                if (phantom.getDeBuffs().size() > 0) {
                    phantom.getDeBuffs().remove(RandomUtil.randomInt(phantom.getDeBuffs().size()));
                    battleRecord.append(phantom.getName()).append("成功移除了一个负面效果").append(StrUtil.CRLF);
                }else {
                    battleRecord.append(phantom.getName()).append("没有需要移除的负面效果").append(StrUtil.CRLF);
                }
                break;
            case U01:
                double tempU01 = phantom.getFinalAttack() * 0.1;
                phantom.setFinalAttack(phantom.getFinalAttack() + (int) tempU01);
                battleRecord.append(phantom.getName()).append("的攻击力增加了").append((int) tempU01).append("点").append(StrUtil.CRLF);
                break;
            case U02:
                double tempU02 = phantom.getFinalSpeed();
                phantom.setFinalSpeed(phantom.getFinalSpeed() + (int) tempU02);
                battleRecord.append(phantom.getName()).append("的速度增加了").append((int) tempU02).append("点").append(StrUtil.CRLF);
                break;
            case U03:
                double tempU03 = phantom.getFinalDefense() * 0.3;
                phantom.setFinalDefense(phantom.getFinalDefense() + (int) tempU03);
                battleRecord.append(phantom.getName()).append("的防御增加了").append((int) tempU03).append("点").append(StrUtil.CRLF);
                break;
                default:
                    break;
        }
    }

    /**
     * 战斗胜利后获取物品
     * @param playerDto
     * @param targetDto
     * @param area
     * @return
     */
    private String afterBattleSuccessResult(BattlePhantomDTO playerDto, BattlePhantomDTO targetDto, String area) {
        StringBuilder stringBuilder = new StringBuilder();
        int exp;
        int playerLevel = playerDto.getLevel();
        int targetLevel = targetDto.getLevel();
        if (playerLevel >= targetLevel) {
            int num = playerLevel - targetLevel;
            if (num <= 3) {
                exp = 6;
            }else if (num == 4) {
                exp = 4;
            }else if (num == 5) {
                exp = 3;
            }else{
                exp = 1;
            }
        }else {
            int num = targetLevel - playerLevel;
            exp = 6 + num;
        }
        if (this.isBoos) {
            exp = 0;
        }
        if (exp !=0 && GameConsts.CommonTip.MAX_LEVEL.equals(playerDto.getLevel())) {
            exp = 0;
            stringBuilder.append(GameConsts.Battle.EXP_MAX).append(StrUtil.CRLF);
        }
        if (exp != 0) {
            stringBuilder.append(String.format(GameConsts.Battle.GET_RESULT_EXP, exp)).append(StrUtil.CRLF);
        }
        int afterAddExp = playerDto.getExp() + exp;
        if (afterAddExp >= GameConsts.BaseFigure.UP_LEVEL_NEED_EXP + GameConsts.BaseFigure.MAX_EXP_GROW * playerDto.getLevel()) {
            playerDto.setLevel(playerDto.getLevel() + 1);
            playerDto.setExp(afterAddExp - GameConsts.BaseFigure.UP_LEVEL_NEED_EXP);
            CommonPlayer.afterAddGrow(playerDto, playerDto.getGrow());
            CommonPlayer.computeAndUpdateSoulPower(playerDto.getPlayerId());
        }else {
            playerDto.setExp(afterAddExp);
        }
        PlayerPhantom playerPhantom = new PlayerPhantom();
        BeanUtil.copyProperties(playerDto, playerPhantom);
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        playerPhantomMapper.updateByPrimaryKey(playerPhantom);
        if (this.isBoos) {
            this.doGetBoosGoods(stringBuilder, playerDto);
            return stringBuilder.toString();
        }
        BaseGoods baseGoods = getResultGoods(area);
        if (baseGoods == null) {
            stringBuilder.append(GameConsts.Battle.GET_RESULT_GOOD_EMTPY).append(StrUtil.CRLF);
        }else {
            stringBuilder.append(String.format(GameConsts.Battle.GET_RESULT_GOOD, baseGoods.getName(),
                    ENGoodEffect.getByValue(baseGoods.getEffect()).getLabel())).append(StrUtil.CRLF);
            CommonPlayer.addPlayerGoods(baseGoods.getId(), playerDto.getPlayerId(), 1);
        }
        int getMoney = RandomUtil.randomInt(GameConsts.Money.EXPLORE_MIN, GameConsts.Money.EXPLORE_MAX);
        CommonPlayer.addOrSubMoney(playerDto.getPlayerId(), getMoney);
        stringBuilder.append(String.format("获得灵石*%s", getMoney)).append(StrUtil.CRLF);
        return stringBuilder.toString();
    }

    /**
     * 执行世界Boos奖励
     * @param stringBuilder
     * @param playerDto
     */
    private void doGetBoosGoods(StringBuilder stringBuilder, BattlePhantomDTO playerDto) {
        BaseGoods baseGoods = getBoosGoods(this.allHurt);
        int number = 1;
        CommonPlayer.addPlayerGoods(baseGoods.getId(), playerDto.getPlayerId(), number);
        int money = CommonPlayer.getBoosMoney(this.allHurt);
        if (this.allHurt > 5500) {
            if (!CommonPlayer.isAppellationExist(ENAppellation.A08, playerDto.getPlayerId())) {
                CommonPlayer.addAppellation(ENAppellation.A08, playerDto.getPlayerId());
                stringBuilder.append("恭喜你，获得了[").append(ENAppellation.A08.getAppellation()).append("]的称号!!");
            }
        }
        CommonPlayer.addOrSubMoney(playerDto.getPlayerId(), money);
        stringBuilder.append(String.format(GameConsts.Battle.BOOS_RESULT, this.allHurt, baseGoods.getName(),
                ENGoodEffect.getByValue(baseGoods.getEffect()).getLabel(), money)).append(StrUtil.CRLF);
    }

    /**
     * 执行法宝效果
     * @param targetDTO
     * @param playerDTO
     */
    private void dealWeapon(BattlePhantomDTO targetDTO, BattlePhantomDTO playerDTO) {
        if (ObjectUtil.isNotEmpty(targetDTO.getBattleWeaponDTO())) {
            this.finalWeapon(targetDTO, playerDTO);
        }
        if (ObjectUtil.isNotEmpty(playerDTO.getBattleWeaponDTO())) {
            this.finalWeapon(playerDTO, targetDTO);
        }
    }

    private void finalWeapon(BattlePhantomDTO phantom, BattlePhantomDTO another) {
        int level = phantom.getBattleWeaponDTO().getLevel();
        switch (phantom.getBattleWeaponDTO().getEnWeaponEffect()) {
            case W01:
                double tempW01 = phantom.getFinalAttack() * 0.01 * level;
                phantom.setFinalAttack(phantom.getFinalAttack() + (int) tempW01);
                battleRecord.append("番天印生效，本次战斗").append(phantom.getName()).append("的攻击力提升了").append(StrUtil.CRLF);
                break;
            case W02:
                double tempW02 = phantom.getFinalDefense() * 0.01 * level;
                phantom.setFinalDefense(phantom.getFinalDefense() + (int) tempW02);
                battleRecord.append("护灵甲生效，本次战斗").append(phantom.getName()).append("的防御力提升了").append(StrUtil.CRLF);
                break;
            case W03:
                double tempW03 = phantom.getFinalHp() * 0.01 * level;
                phantom.setFinalHp(phantom.getFinalHp() + (int) tempW03);
                battleRecord.append("玄王佩生效，本次战斗").append(phantom.getName()).append("的血量值提升了").append(StrUtil.CRLF);
                break;
            case W04:
                BattleEffectDTO tempW04 = new BattleEffectDTO();
                tempW04.setLiveRound(ENWeaponEffect.W04.getLevelNumber()[level]);
                tempW04.setEffect(ENSkillEffect.W04);
                phantom.getBuffs().add(tempW04);
                break;
            case W05:
                BattleEffectDTO tempW05 = new BattleEffectDTO();
                tempW05.setLiveRound(ENWeaponEffect.W05.getLevelNumber()[level]);
                tempW05.setEffect(ENSkillEffect.W05);
                phantom.getBuffs().add(tempW05);
                break;
            case W06:
                BattleEffectDTO tempW06 = new BattleEffectDTO();
                tempW06.setLiveRound(ENWeaponEffect.W06.getLevelNumber()[level]);
                tempW06.setEffect(ENSkillEffect.W06);
                another.getDeBuffs().add(tempW06);
                break;
            case W07:
                double tempW07 = phantom.getFinalSpeed() * 0.01 * level;
                phantom.setFinalSpeed(phantom.getFinalSpeed() + (int) tempW07);
                battleRecord.append("极影斗篷生效，本次战斗").append(phantom.getName()).append("的速度提升了").append(StrUtil.CRLF);
                break;
                default:
                    break;
        }
    }


}
