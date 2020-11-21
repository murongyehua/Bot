package com.bot.game.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.bot.common.constant.GameConsts;
import com.bot.common.exception.BotException;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.*;
import com.bot.game.dto.BattleWeaponDTO;
import com.bot.game.dto.ExploreBuffDTO;
import com.bot.game.enums.ENAppellation;
import com.bot.game.enums.ENGoodEffect;
import com.bot.game.enums.ENRarity;
import com.bot.game.enums.ENWeaponEffect;
import com.bot.game.service.Player;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Service
public class CommonPlayer implements Player {

    public String title;

    public static Map<String, Object> mapperMap;

    public static Map<String, String> battleDetailMap = new HashMap<>();

    public static Map<String, ExploreBuffDTO> exploreBuffMap = new HashMap<>();

    public static Integer nowSale;

    @Override
    public String doPlay(String token) {
        throw new BotException("子类实现");
    }

    /**
     * 是否可唤灵
     * @param token
     * @return
     */
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

    /**
     * 计算和更新战灵力
     * @param token
     */
    public static void computeAndUpdateSoulPower(String token) {
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerWeaponMapper playerWeaponMapper = (PlayerWeaponMapper) mapperMap.get(GameConsts.MapperName.PLAYER_WEAPON);
        PlayerPhantom param = new PlayerPhantom();
        param.setPlayerId(token);
        List<PlayerPhantom> list = playerPhantomMapper.selectAllCarried(param);
        int power = 0;
        // 幻灵提供的战力
        for (PlayerPhantom playerPhantom : list) {
            power += Objects.requireNonNull(ENRarity.getByValue(playerPhantom.getRarity())).getPower();
            power += playerPhantom.getAttack() * GameConsts.BaseFigure.POWER_ATTACK;
            power += playerPhantom.getSpeed() * GameConsts.BaseFigure.POWER_SPEED;
            power += playerPhantom.getPhysique() * GameConsts.BaseFigure.POWER_PHYSIQUE;
        }
        // 法宝提供的战力
        PlayerWeapon weapon = new PlayerWeapon();
        weapon.setPlayerId(token);
        List<PlayerWeapon> playerWeapons = playerWeaponMapper.selectBySelective(weapon);
        for (PlayerWeapon playerWeapon : playerWeapons) {
            int level = playerWeapon.getLevel();
            // 拥有法宝即可获取的战力
            power += GameConsts.BaseFigure.WEAPON_ONE;
            // 每升一级增加的战力
            power += GameConsts.BaseFigure.WEAPON_LEVEL * ( level - 1);
            if (level == 5) {
                // 法宝满级额外提供的战力
                power += GameConsts.BaseFigure.WEAPON_MAX;
            }
        }
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        gamePlayer.setSoulPower(power);
        gamePlayerMapper.updateByPrimaryKey(gamePlayer);
    }

    /**
     * 幻灵成长增加之后调用，按等级随机增长属性
     * @param playerPhantom
     * @param needAddGrow
     */
    public static void afterAddGrow(PlayerPhantom playerPhantom, Integer needAddGrow) {
        int waitAdd = playerPhantom.getLevel() - 1;
        if (needAddGrow != null) {
            waitAdd = needAddGrow;
        }
        List<Integer> list = spiltNumber(waitAdd);
        playerPhantom.setAttack(playerPhantom.getAttack() + list.get(0));
        playerPhantom.setSpeed(playerPhantom.getSpeed() + list.get(1));
        playerPhantom.setPhysique(playerPhantom.getPhysique() + list.get(2));
        playerPhantom.setHp(getInitHp(playerPhantom));
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

    /**
     * 获得物品的时候调用
     * @param goodsId
     * @param token
     * @param number
     */
    public static void addPlayerGoods(String goodsId, String token, int number) {
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        PlayerGoods param = new PlayerGoods();
        param.setPlayerId(token);
        param.setGoodId(goodsId);
        List<PlayerGoods> list = playerGoodsMapper.selectBySelective(param);
        if (CollectionUtil.isNotEmpty(list)) {
            PlayerGoods playerGoods = list.get(0);
            playerGoods.setNumber(playerGoods.getNumber() + number);
            playerGoodsMapper.updateByPrimaryKey(playerGoods);
            return;
        }
        param.setId(IdUtil.simpleUUID());
        param.setNumber(number);
        playerGoodsMapper.insert(param);
    }

    /**
     * 使用物品之后调用，用于减少物品数量或移除物品
     * @param playerGoods
     */
    public static void afterUseGoods(PlayerGoods playerGoods, int number) {
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        if (playerGoods.getNumber() == number) {
            playerGoodsMapper.deleteByPrimaryKey(playerGoods.getId());
        }else {
            playerGoods.setNumber(playerGoods.getNumber() - number);
            playerGoodsMapper.updateByPrimaryKeySelective(playerGoods);
        }
    }

    /**
     * 出售物品后调用，直接删除该物品
     * @param playerGoodsId
     */
    public static void afterSaleGoods(String playerGoodsId) {
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        playerGoodsMapper.deleteByPrimaryKey(playerGoodsId);
    }

    /**
     * 添加称号
     * @param enAppellation
     * @param token
     */
    public static void addAppellation(ENAppellation enAppellation, String token) {
        if (!isAppellationExist(enAppellation, token)) {
            PlayerAppellationMapper playerAppellationMapper = (PlayerAppellationMapper) mapperMap.get(GameConsts.MapperName.PLAYER_APPELLATION);
            PlayerAppellation param = new PlayerAppellation();
            param.setId(IdUtil.simpleUUID());
            param.setGetTime(new Date());
            param.setAppellation(enAppellation.getAppellation());
            param.setPlayerId(token);
            playerAppellationMapper.insert(param);
        }
    }

    /**
     * 称号是否已拥有
     * @param enAppellation
     * @param token
     * @return
     */
    public static boolean isAppellationExist(ENAppellation enAppellation, String token) {
        PlayerAppellationMapper playerAppellationMapper = (PlayerAppellationMapper) mapperMap.get(GameConsts.MapperName.PLAYER_APPELLATION);
        PlayerAppellation param = new PlayerAppellation();
        param.setPlayerId(token);
        param.setAppellation(enAppellation.getAppellation());
        List<PlayerAppellation> list = playerAppellationMapper.selectBySelective(param);
        return CollectionUtil.isNotEmpty(list);
    }

    /**
     * 按区域获取物品
     * @param area
     * @return
     */
    public static BaseGoods getResultGoods(String area) {
        BaseGoodsMapper baseGoodsMapper = (BaseGoodsMapper) mapperMap.get(GameConsts.MapperName.BASE_GOODS);
        BaseGoods param = new BaseGoods();
        param.setOrigin(area);
        List<BaseGoods> list = baseGoodsMapper.selectBySelective(param);
        List<BaseGoods> tempList = list.stream().filter(baseGoods -> {
            if (Integer.parseInt(baseGoods.getWeight()) != 0) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        List<String> weights = tempList.stream().map(BaseGoods::getWeight).distinct().collect(Collectors.toList());
        String needWeight = null;
        for (String weight : weights) {
            int number = RandomUtil.randomInt(0, 100);
            int range = Integer.parseInt(weight);
            if ( number < range) {
                needWeight = weight;
                break;
            }
        }
        if (needWeight == null) {
            return null;
        }
        String finalWeight = needWeight;
        List<BaseGoods> finalList = tempList.stream().filter(baseGoods -> {
            if (finalWeight.equals(baseGoods.getWeight())) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        return finalList.get(RandomUtil.randomInt(finalList.size()));
    }

    /**
     * 获取世界Boos奖励
     * @param hurt
     * @return
     */
    public static BaseGoods getBoosGoods(int hurt) {
        BaseGoodsMapper baseGoodsMapper = (BaseGoodsMapper) mapperMap.get(GameConsts.MapperName.BASE_GOODS);
        BaseGoods param = new BaseGoods();
        if (hurt < 300) {
            param.setEffect(ENGoodEffect.WAN_2.getValue());
        }else if (hurt < 1000) {
            param.setEffect(ENGoodEffect.WAN_3.getValue());
        }else if (hurt < 5500) {
            param.setEffect(ENGoodEffect.GET_PHANTOM.getValue());
        }else {
            param.setEffect(ENGoodEffect.GET_PHANTOM.getValue());
        }
        List<BaseGoods> list = baseGoodsMapper.selectBySelective(param);
        return list.get(0);
    }

    /**
     * 获取Boos灵石
     * @param hurt
     * @return
     */
    public static Integer getBoosMoney(int hurt) {
        if (hurt < 300) {
            return GameConsts.Money.WORLD_BOOS_1;
        }else if (hurt < 1000) {
            return GameConsts.Money.WORLD_BOOS_2;
        }else if (hurt < 5500) {
            return GameConsts.Money.WORLD_BOOS_3;
        }else {
            return GameConsts.Money.WORLD_BOOS_4;
        }
    }

    /**
     * 获取玩家当前佩戴的法宝
     * @param token
     * @return
     */
    public static BattleWeaponDTO getCurrentWeapon(String token) {
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        PlayerWeaponMapper playerWeaponMapper = (PlayerWeaponMapper) mapperMap.get(GameConsts.MapperName.PLAYER_WEAPON);
        BaseWeaponMapper baseWeaponMapper = (BaseWeaponMapper) mapperMap.get(GameConsts.MapperName.BASE_WEAPON);
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        PlayerWeapon playerWeapon = playerWeaponMapper.selectByPrimaryKey(gamePlayer.getPlayerWeaponId());
        if (playerWeapon == null) {
            return null;
        }
        BaseWeapon baseWeapon = baseWeaponMapper.selectByPrimaryKey(playerWeapon.getWeaponId());
        BattleWeaponDTO battleWeaponDTO = new BattleWeaponDTO();
        battleWeaponDTO.setEnWeaponEffect(ENWeaponEffect.getByValue(baseWeapon.getEffect()));
        battleWeaponDTO.setLevel(playerWeapon.getLevel());
        return battleWeaponDTO;
    }

    /**
     * 添加或减少灵石 当减少灵石后为负数时，返回false
     * @param token
     * @param moneyNumber
     * @return
     */
    public static boolean addOrSubMoney(String token, Integer moneyNumber) {
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        if (gamePlayer.getMoney() + moneyNumber < 0) {
            return false;
        }
        gamePlayer.setMoney(gamePlayer.getMoney() + moneyNumber);
        gamePlayerMapper.updateByPrimaryKeySelective(gamePlayer);
        if (gamePlayer.getMoney() > GameConsts.BaseFigure.HAS_MONEY_1) {
            if (!isAppellationExist(ENAppellation.A05, token)) {
                addAppellation(ENAppellation.A05, token);
            }
        }
        if (gamePlayer.getMoney() > GameConsts.BaseFigure.HAS_MONEY_2) {
            if (!isAppellationExist(ENAppellation.A06, token)) {
                addAppellation(ENAppellation.A06, token);
            }
        }
        if (gamePlayer.getMoney() > GameConsts.BaseFigure.HAS_MONEY_3) {
            if (!isAppellationExist(ENAppellation.A07, token)) {
                addAppellation(ENAppellation.A07, token);
            }
        }
        return true;
    }

    /**
     * 获得法宝，如果已经拥有，则法宝灵气+1
     * @param token
     * @param baseWeapon
     */
    public static void addWeapon(String token, BaseWeapon baseWeapon) {
        PlayerWeaponMapper playerWeaponMapper = (PlayerWeaponMapper) mapperMap.get(GameConsts.MapperName.PLAYER_WEAPON);
        PlayerWeapon param = new PlayerWeapon();
        param.setPlayerId(token);
        param.setWeaponId(baseWeapon.getId());
        List<PlayerWeapon> list = playerWeaponMapper.selectBySelective(param);
        if (CollectionUtil.isNotEmpty(list)) {
            PlayerWeapon playerWeapon = list.get(0);
            if (playerWeapon.getLevel() < 5) {
                playerWeapon.setLevel(playerWeapon.getLevel() + 1);
                playerWeaponMapper.updateByPrimaryKey(playerWeapon);
            }
            return;
        }
        param.setLevel(1);
        param.setId(IdUtil.simpleUUID());
        playerWeaponMapper.insert(param);
        // 查询个数
        int number = playerWeaponMapper.countByToken(token);
        if (number >= GameConsts.BaseFigure.HAS_WEAPON) {
            if (!isAppellationExist(ENAppellation.A10, token)) {
                addAppellation(ENAppellation.A10, token);
            }
        }
    }

    /**
     * 获取当前灵石数量
     * @param token
     * @return
     */
    public static Integer getMoney(String token) {
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        return gamePlayer.getMoney();
    }

    /**
     * 物品使用前校验数量
     * @param goodEffect
     * @return
     */
    public static PlayerGoods checkGoodsNumber(String token, ENGoodEffect goodEffect, String targetId) {
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        BaseGoodsMapper baseGoodsMapper = (BaseGoodsMapper) mapperMap.get(GameConsts.MapperName.BASE_GOODS);
        BaseGoods baseGoods = new BaseGoods();
        baseGoods.setEffect(goodEffect.getValue());
        baseGoods.setTargetId(targetId);
        List<BaseGoods> baseGoodsList = baseGoodsMapper.selectBySelective(baseGoods);
        PlayerGoods playerGoods = new PlayerGoods();
        playerGoods.setPlayerId(token);
        playerGoods.setGoodId(baseGoodsList.get(0).getId());
        List<PlayerGoods> list = playerGoodsMapper.selectBySelective(playerGoods);
        if (CollectionUtil.isEmpty(list) || list.get(0).getNumber() == 0) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 增加或扣除行动点
     * @param token
     * @param number
     * @return
     */
    public static boolean addOrSubActionPoint(String token, Integer number) {
        GamePlayerMapper gamePlayerMapper = (GamePlayerMapper) mapperMap.get(GameConsts.MapperName.GAME_PLAYER);
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        int finalNumber = gamePlayer.getActionPoint() + number;
        if (finalNumber >= 0 && finalNumber <= 100) {
            gamePlayer.setActionPoint(finalNumber);
            gamePlayerMapper.updateByPrimaryKey(gamePlayer);
            return true;
        }
        return false;
    }

}
