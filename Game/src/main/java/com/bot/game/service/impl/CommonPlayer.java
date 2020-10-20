package com.bot.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.commom.exception.BotException;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.*;
import com.bot.game.dto.ExploreBuffDTO;
import com.bot.game.enums.ENAppellation;
import com.bot.game.enums.ENGoodEffect;
import com.bot.game.enums.ENRarity;
import com.bot.game.service.Player;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * @author liul
 * @version 1.0 2020/10/15
 */
@Service
public class CommonPlayer implements Player {

    public String title;

    public static Map<String, Object> mapperMap;

    public static Map<String, String> battleDetailMap = new HashMap<>();

    public static Map<String, ExploreBuffDTO> exploreBuffMap = new HashMap<>();

    @Override
    public String doPlay(String token) {
        throw new BotException("子类实现");
    }

    public static PlayerGoods isCanGetPhantom(String token) {
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        BaseGoodsMapper baseGoodsMapper = (BaseGoodsMapper) mapperMap.get(GameConsts.MapperName.BASE_GOODS);
        PlayerGoods playerGoods = new PlayerGoods();
        BaseGoods baseGoods = new BaseGoods();
        baseGoods.setEffect(ENGoodEffect.GET_PHANTOM.getValue());
        playerGoods.setPlayerId(token);
        playerGoods.setGoodId(baseGoodsMapper.selectBySelective(baseGoods).get(0).getId());
        List<PlayerGoods> list = playerGoodsMapper.selectBySelective(playerGoods);
        if (CollectionUtil.isEmpty(list)) {
            // 没有唤灵符
            return null;
        }
        return list.get(0);
    }

    static Integer getInitHp(PlayerPhantom playerPhantom) {
        return playerPhantom.getLevel() * GameConsts.BaseFigure.HP_FOR_EVERY_LEVEL +
                playerPhantom.getPhysique() * GameConsts.BaseFigure.HP_POINT;
    }

    public static void computeAndUpdateSoulPower(String token) {
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom param = new PlayerPhantom();
        param.setPlayerId(token);
        List<PlayerPhantom> list = playerPhantomMapper.selectBySelective(param);
        int power = 0;
        for (PlayerPhantom playerPhantom : list) {
            power += Objects.requireNonNull(ENRarity.getByValue(playerPhantom.getRarity())).getPower();
            power += playerPhantom.getAttack() * GameConsts.BaseFigure.POWER_ATTACK;
            power += playerPhantom.getSpeed() * GameConsts.BaseFigure.POWER_SPEED;
            power += playerPhantom.getPhysique() * GameConsts.BaseFigure.POWER_PHYSIQUE;
        }
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        gamePlayer.setSoulPower(power);
        gamePlayerMapper.updateByPrimaryKey(gamePlayer);
    }

    public static void afterAddGrow(PlayerPhantom playerPhantom, Integer needAddGrow) {
        int waitAdd = playerPhantom.getLevel() - 1;
        if (needAddGrow != null) {
            waitAdd = needAddGrow;
        }
        List<Integer> list = spiltNumber(waitAdd);
        playerPhantom.setAttack(playerPhantom.getAttack() + list.get(0));
        playerPhantom.setSpeed(playerPhantom.getSpeed() + list.get(1));
        playerPhantom.setPhysique(playerPhantom.getPhysique() + list.get(2));
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        playerPhantomMapper.updateByPrimaryKey(playerPhantom);
    }

    public static List<Integer> spiltNumber(int number) {
        int attack = 0;
        int speed = 0;
        int physique = 0;
        for (int i=0; i < number; i++) {
            int randomNum = RandomUtil.randomInt(3);
            switch (randomNum) {
                case 0:
                    attack++;
                    break;
                case 1:
                    speed++;
                    break;
                case 2:
                    physique++;
                    break;
                default:
                    break;
            }
        }
        List<Integer> list = new ArrayList<>();
        list.add(attack);
        list.add(speed);
        list.add(physique);
        return list;
    }

    public static void addPlayerGoods(String goodsId, String token) {
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        PlayerGoods param = new PlayerGoods();
        param.setPlayerId(token);
        param.setGoodId(goodsId);
        List<PlayerGoods> list = playerGoodsMapper.selectBySelective(param);
        if (CollectionUtil.isNotEmpty(list)) {
            PlayerGoods playerGoods = list.get(0);
            playerGoods.setNumber(playerGoods.getNumber() + 1);
            playerGoodsMapper.updateByPrimaryKey(playerGoods);
            return;
        }
        param.setId(IdUtil.simpleUUID());
        param.setNumber(1);
        playerGoodsMapper.insert(param);
    }

    public static void afterUseGoods(PlayerGoods playerGoods) {
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        if (playerGoods.getNumber() == 1) {
            playerGoodsMapper.deleteByPrimaryKey(playerGoods.getId());
        }else {
            playerGoods.setNumber(playerGoods.getNumber() - 1);
            playerGoodsMapper.updateByPrimaryKeySelective(playerGoods);
        }
    }

    public static void addAppellation(ENAppellation enAppellation, String token) {
        PlayerAppellationMapper playerAppellationMapper = (PlayerAppellationMapper) mapperMap.get(GameConsts.MapperName.PLAYER_APPELLATION);
        PlayerAppellation param = new PlayerAppellation();
        param.setPlayerId(token);
        param.setAppellation(enAppellation.getAppellation());
        List<PlayerAppellation> list = playerAppellationMapper.selectBySelective(param);
        if (CollectionUtil.isEmpty(list)) {
            param.setId(IdUtil.simpleUUID());
            param.setGetTime(new Date());
            playerAppellationMapper.insert(param);
        }
    }

}
