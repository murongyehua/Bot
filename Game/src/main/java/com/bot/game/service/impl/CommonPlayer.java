package com.bot.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.commom.exception.BotException;
import com.bot.game.dao.entity.BaseGoods;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.BaseGoodsMapper;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.BattlePhantomDTO;
import com.bot.game.enums.ENGoodEffect;
import com.bot.game.service.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author liul
 * @version 1.0 2020/10/15
 */
@Service
public class CommonPlayer implements Player {

    public String title;

    public static Map<String, Object> mapperMap;

    public static Map<String, String> battleDetail;

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

    public static BattlePhantomDTO getBattlePhantom(PlayerPhantom playerPhantom) {
        BattlePhantomDTO battlePhantomDTO = new BattlePhantomDTO();
        BeanUtil.copyProperties(playerPhantom, battlePhantomDTO);
        battlePhantomDTO.setFinalAttack(playerPhantom.getLevel() * GameConsts.BaseFigure.ATTACK_FOR_EVERY_LEVEL +
                playerPhantom.getAttack() * GameConsts.BaseFigure.ATTACK_POINT);
        battlePhantomDTO.setFinalSpeed(playerPhantom.getLevel() * GameConsts.BaseFigure.SPEED_FOR_EVERY_LEVEL +
                playerPhantom.getSpeed() + GameConsts.BaseFigure.SPEED_POINT);
        battlePhantomDTO.setFinalDefense(playerPhantom.getLevel() * GameConsts.BaseFigure.DEFENSE_FOR_EVERY_LEVEL +
                playerPhantom.getPhysique() * GameConsts.BaseFigure.DEFENSE_POINT);
        return battlePhantomDTO;
    }

    static Integer getInitHp(PlayerPhantom playerPhantom) {
        return playerPhantom.getLevel() * GameConsts.BaseFigure.HP_FOR_EVERY_LEVEL +
                playerPhantom.getPhysique() * GameConsts.BaseFigure.HP_POINT;
    }

    public static void afterAddGrow(PlayerPhantom playerPhantom) {
        int waitAdd = playerPhantom.getLevel() - 1;
        List<Integer> list = spiltNumber(waitAdd);
        playerPhantom.setAttack(playerPhantom.getAttack() + list.get(0));
        playerPhantom.setSpeed(playerPhantom.getSpeed() + list.get(1));
        playerPhantom.setPhysique(playerPhantom.getPhysique() + list.get(3));
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

}
