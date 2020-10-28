package com.bot.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
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
import com.bot.game.enums.ENAttribute;
import com.bot.game.enums.ENEffectType;
import com.bot.game.enums.ENGoodEffect;
import com.bot.game.enums.ENSkillEffect;

import java.util.*;

/**
 * @author murongyehua
 * @version 1.0 2020/10/18
 */
public class BattleServiceImpl extends CommonPlayer {

    private BaseMonster baseMonster;

    private PlayerPhantom playerPhantom;

    private boolean isCompare;

    private boolean isBoos;

    private int round = 0;

    private int allHurt = 0;

    private int startBoosHp;

    private StringBuilder battleRecord = new StringBuilder();


    public BattleServiceImpl(BaseMonster baseMonster, PlayerPhantom playerPhantom, boolean isCompare, boolean isBoos) {
        this.title = String.format(GameConsts.Battle.TITLE,
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel());
        this.baseMonster = baseMonster;
        this.playerPhantom = playerPhantom;
        this.isCompare = isCompare;
        this.isBoos = isBoos;
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
        startBoosHp = WorldBossServiceImpl.boos.getFinalHp();
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
        Double figure = this.getFigure();
        Double doubleAttack = intAttack * figure;
        battleMonsterDTO.setFinalAttack(doubleAttack.intValue());
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
        return battleMonsterDTO;
    }

    private BattlePhantomDTO getBattlePhantom() {
        BattlePhantomDTO battlePhantomDTO = new BattlePhantomDTO();
        BeanUtil.copyProperties(playerPhantom, battlePhantomDTO);
        Double figure = this.getFigure();
        int intAttack = playerPhantom.getLevel() * GameConsts.BaseFigure.ATTACK_FOR_EVERY_LEVEL +
                playerPhantom.getAttack() * GameConsts.BaseFigure.ATTACK_POINT;
        Double doubleAttack = intAttack * figure;
        battlePhantomDTO.setFinalAttack(doubleAttack.intValue());
        battlePhantomDTO.setFinalSpeed(playerPhantom.getLevel() * GameConsts.BaseFigure.SPEED_FOR_EVERY_LEVEL +
                playerPhantom.getSpeed() + GameConsts.BaseFigure.SPEED_POINT);
        battlePhantomDTO.setFinalDefense(playerPhantom.getLevel() * GameConsts.BaseFigure.DEFENSE_FOR_EVERY_LEVEL +
                playerPhantom.getPhysique() * GameConsts.BaseFigure.DEFENSE_POINT);
        battlePhantomDTO.setFinalHp(battlePhantomDTO.getHp());
        return battlePhantomDTO;
    }

    private Double getFigure() {
        Double figure = GameConsts.BaseFigure.BASE_ONE_NUMBER;
        if (ENAttribute.isBuff(baseMonster.getAttribute(), playerPhantom.getAttribute())) {
            figure = GameConsts.BaseFigure.BASE_BUFF_FIGURE;
        }
        if (ENAttribute.isDeBuff(baseMonster.getAttribute(), playerPhantom.getAttribute())) {
            figure = GameConsts.BaseFigure.BASE_DE_BUFF_FIGURE;
        }
        return figure;
    }

    private String doBattle(BattlePhantomDTO targetDTO, BattlePhantomDTO playerDTO) {
        roundStart(targetDTO, playerDTO);
        if (targetDTO.getFinalHp() <= 0 || playerDTO.getFinalHp() <= 0) {
            return this.getResult(playerDTO, targetDTO);
        }
        battleRecord.append(String.format(GameConsts.Battle.BATTLE_RECORD_ROUND, round));
        // 对比速度
        if (targetDTO.getSpeed() > playerPhantom.getSpeed()) {
            doSingleBattle(targetDTO, playerDTO);
            doSingleBattle(playerDTO, targetDTO);
        }else {
            doSingleBattle(playerDTO, targetDTO);
            doSingleBattle(targetDTO, playerDTO);
        }
        battleRecord.append(String.format(GameConsts.Battle.BATTLE_RECORD_ROUND_RESULT,
                playerDTO.getName(), playerDTO.getFinalHp(), targetDTO.getName(), targetDTO.getFinalHp()));
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

    private void roundStart(BattlePhantomDTO targetDTO, BattlePhantomDTO playerDTO) {
        round++;
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
            if (!hasEffect) {
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
        this.finalAttack(tempPhantom, tempAnother);
        nowAttackPhantom.setFinalHp(tempPhantom.getFinalHp());
        another.setFinalHp(tempAnother.getFinalHp());
        another.setStop(tempAnother.getStop());
    }

    private void finalAttack(BattlePhantomDTO tempPhantom, BattlePhantomDTO tempAnother) {
        int hurt = tempPhantom.getFinalAttack() - tempAnother.getFinalDefense();
        if (hurt < 1) {
            hurt = 1;
        }
        tempAnother.setFinalHp(tempAnother.getFinalHp() - hurt);
        this.buffDone(tempAnother, tempPhantom, null, ENEffectType.DEFENSE, hurt);
        this.buffDone(tempPhantom, tempAnother, null, ENEffectType.END, hurt);
    }


    private void doSingleBattle(BattlePhantomDTO nowAttackPhantom, BattlePhantomDTO another) {
        if (nowAttackPhantom.getFinalHp() <= 0 || another.getFinalHp() <= 0) {
            return;
        }
        if (nowAttackPhantom.getStop()) {
            nowAttackPhantom.setStop(false);
            return;
        }
        for (BattleSkillDTO skill : nowAttackPhantom.getSkillList()) {
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
        if (targetDto.getFinalHp() <= 0) {
            if (isCompare) {
                return GameConsts.Battle.SUCCESS + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK;
            }
            return GameConsts.Battle.SUCCESS + StrUtil.CRLF +
                    this.afterBattleSuccessResult(playerDto, targetDto, targetDto.getArea()) + GameConsts.CommonTip.TURN_BACK;
        }else {
            if (isBoos) {
                if (targetDto.getFinalHp() < 0) {
                    WorldBossServiceImpl.boos.setFinalHp(0);
                }else {
                    WorldBossServiceImpl.boos.setFinalHp(targetDto.getFinalHp());
                }
                int endBoosHp = WorldBossServiceImpl.boos.getFinalHp();
                allHurt = endBoosHp - startBoosHp;
                StringBuilder stringBuilder = new StringBuilder();
                this.doGetBoosGoods(stringBuilder, playerDto);
                return stringBuilder.toString();
            }
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
            case B01:
                double tempB01 = phantom.getFinalHp() * 0.05;
                phantom.setFinalHp(phantom.getFinalHp() - (int) tempB01);
                another.setFinalHp(another.getFinalHp() - (int) tempB01);
                break;
            case B02:
                double tempB02 = phantom.getFinalHp() * 0.10;
                phantom.setFinalHp(phantom.getFinalHp() - (int) tempB02);
                another.setFinalHp(another.getFinalHp() - (int) tempB02);
                break;
            case C01:
                double tempC01 = another.getFinalAttack() * 0.1;
                another.setFinalAttack(another.getFinalAttack() - (int) tempC01);
                break;
            case C02:
                double tempC02 = another.getFinalAttack() * 0.2;
                another.setFinalAttack(another.getFinalAttack() - (int) tempC02);
                break;
            case C03:
                double tempC03 = another.getFinalDefense() * 0.1;
                another.setFinalDefense(another.getFinalDefense() - (int) tempC03);
                break;
            case C04:
                double tempC04 = another.getFinalDefense() * 0.2;
                another.setFinalDefense(another.getFinalDefense() - (int) tempC04);
                break;
            case C05:
                another.setStop(true);
                break;
            case C06:
                double tempC06 = another.getHp() * 0.02;
                another.setFinalHp(another.getFinalHp() - (int) tempC06);
                break;
            case C07:
                double tempC07 = another.getFinalHp() * 0.05;
                another.setFinalHp(another.getFinalHp() - (int) tempC07);
                break;
            case C08:
                double tempC08 = hurt * 0.3;
                another.setFinalHp(another.getFinalHp() - (int) tempC08);
                break;
            case C09:
                if (phantom.getFinalHp() > 0) {
                    double tempC09 = (phantom.getHp() - phantom.getFinalHp()) * 0.1;
                    phantom.setFinalHp(phantom.getFinalHp() + (int) tempC09);
                }
                break;
            case C10:
                double tempC10 = another.getFinalSpeed() * 0.05;
                another.setFinalSpeed(another.getFinalSpeed() - (int) tempC10);
                break;
            case D01:

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
        return stringBuilder.toString();
    }

    private void doGetBoosGoods(StringBuilder stringBuilder, BattlePhantomDTO playerDto) {
        BaseGoods baseGoods = getBoosGoods(this.allHurt);
        int number = 1;
        if (this.allHurt > 3000) {
            number = 2;
        }
        CommonPlayer.addPlayerGoods(baseGoods.getId(), playerDto.getPlayerId(), number);
        stringBuilder.append(String.format(GameConsts.Battle.BOOS_RESULT, this.allHurt, baseGoods.getName(),
                ENGoodEffect.getByValue(baseGoods.getEffect()).getLabel(), number)).append(StrUtil.CRLF);
    }
}
