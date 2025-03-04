package com.bot.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.GameConsts;
import com.bot.game.dao.entity.BasePhantom;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.BasePhantomMapper;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.enums.ENAppellation;
import com.bot.game.enums.ENCarriedStatus;
import com.bot.game.enums.ENRarity;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/17
 */
public class GetPhantomServiceImpl extends CommonPlayer {

    private PlayerGoods playerGoods;

    private final static Integer[] BEST_NUMBER = {30,29,28,27,3,4,5,6};
    private final static Integer[] GREAT_NUMBER = {7,8,9,23,24,25,26};
    private final static Integer[] GOOD_NUMBER = {10,11,12,13,14,15};

    public GetPhantomServiceImpl(String title, PlayerGoods playerGoods ) {
        this.title = title;
        this.playerGoods = playerGoods;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String doPlay(String token) {
        PlayerGoods playerGoods = CommonPlayer.isCanGetPhantom(token);
        if (playerGoods == null) {
            return GameConsts.GetPhantom.CAN_GET_TIME;
        }
        BasePhantomMapper basePhantomMapper = (BasePhantomMapper) mapperMap.get(GameConsts.MapperName.BASE_PHANTOM);
        BasePhantom basePhantom = new BasePhantom();
        basePhantom.setRarity(this.getRarity().getValue());
        List<BasePhantom> list = basePhantomMapper.selectBySelective(basePhantom);
        BasePhantom result = list.get(RandomUtil.randomInt(list.size()));
        return this.getPhantomResult(result, token);
    }

    private ENRarity getRarity() {
        int number1 = RandomUtil.randomInt(1,11);
        int number2 = RandomUtil.randomInt(1,11);
        int number3 = RandomUtil.randomInt(1,11);
        int finalNumber = number1 + number2 + number3;
        if (Arrays.asList(BEST_NUMBER).contains(finalNumber)) {
            return ENRarity.BEST;
        }
        if (Arrays.asList(GREAT_NUMBER).contains(finalNumber)) {
            return ENRarity.GREAT;
        }
        if (Arrays.asList(GOOD_NUMBER).contains(finalNumber)) {
            return ENRarity.GOOD;
        }
        return ENRarity.NORMAL;
    }

    private String getPhantomResult(BasePhantom basePhantom, String token) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(GameConsts.GetPhantom.GET_1, ENRarity.getLabelByValue(basePhantom.getRarity()),
                basePhantom.getAppellation(), basePhantom.getName())).append(StrUtil.CRLF);
        String[] lines = basePhantom.getLine().split("\\|\\|");
        stringBuilder.append(lines[RandomUtil.randomInt(lines.length)]).append(StrUtil.CRLF);
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom playerPhantom = new PlayerPhantom();
        playerPhantom.setPlayerId(token);
        playerPhantom.setName(basePhantom.getName());
        List<PlayerPhantom> list = playerPhantomMapper.selectAllCarried(playerPhantom);
        if (CollectionUtil.isNotEmpty(list)) {
            // 成长 +1
            int addNumber = 1;
            PlayerPhantom hasPhantom = list.get(0);
            ENRarity rarity =  ENRarity.getByValue(hasPhantom.getRarity());
            if (rarity != null && hasPhantom.getGrow() >= rarity.getMaxGrow()) {
                // 已达上限
                addNumber = 0;
            }
            hasPhantom.setGrow(hasPhantom.getGrow() + addNumber);
            hasPhantom.setGrowLevel(hasPhantom.getGrowLevel() == null ? "1" : String.valueOf(Integer.parseInt(hasPhantom.getGrowLevel()) + addNumber));
            playerPhantomMapper.updateByPrimaryKey(hasPhantom);
            CommonPlayer.afterAddGrow(hasPhantom, null);
            CommonPlayer.computeAndUpdateSoulPower(token);
            stringBuilder.append(GameConsts.GetPhantom.REPEAT).append(StrUtil.CRLF);
        }else {
            // 存入
            PlayerPhantom param = new PlayerPhantom();
            param.setPlayerId(token);
            List<PlayerPhantom> allPhantom = playerPhantomMapper.selectAllCarried(param);
            PlayerPhantom newPhantom = new PlayerPhantom();
            BeanUtil.copyProperties(basePhantom, newPhantom);
            newPhantom.setPlayerId(token);
            newPhantom.setId(IdUtil.simpleUUID());
            newPhantom.setLevel(1);
            newPhantom.setHp(CommonPlayer.getInitHp(newPhantom));
            newPhantom.setExp(0);
            newPhantom.setCarried(ENCarriedStatus.LOCK.getValue());
            playerPhantomMapper.insert(newPhantom);
            if (CollectionUtil.isEmpty(allPhantom)) {
                // 获得称号
                CommonPlayer.addAppellation(ENAppellation.A01, token);
                stringBuilder.append(String.format(GameConsts.CommonTip.GET_APPELLATION, ENAppellation.A01.getAppellation())).append(StrUtil.CRLF);
            }
            if (ENRarity.BEST.getValue().equals(basePhantom.getRarity())) {
                // 获得称号
                CommonPlayer.addAppellation(ENAppellation.A02, token);
                stringBuilder.append(String.format(GameConsts.CommonTip.GET_APPELLATION, ENAppellation.A02.getAppellation())).append(StrUtil.CRLF);
            }
            if (allPhantom.size() + 1 == GameConsts.BaseFigure.HAS_PHANTOM) {
                // 获得称号
                CommonPlayer.addAppellation(ENAppellation.A04, token);
                stringBuilder.append(String.format(GameConsts.CommonTip.GET_APPELLATION, ENAppellation.A04.getAppellation())).append(StrUtil.CRLF);
            }
            stringBuilder.append(GameConsts.GetPhantom.GET_2).append(StrUtil.CRLF);
            stringBuilder.append(GameConsts.CommonTip.TURN_BACK_ORCONTINU);
        }
        CommonPlayer.computeAndUpdateSoulPower(token);
        // 扣除唤灵符
        this.subTimes();
        return stringBuilder.toString();
    }

    private void subTimes() {
        PlayerGoodsMapper playerGoodsMapper = (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        int times = playerGoods.getNumber() - 1;
        if (times == 0) {
            // 删除记录
            playerGoodsMapper.deleteByPrimaryKey(playerGoods.getId());
            return;
        }
        // 减少次数
        playerGoods.setNumber(times);
        playerGoodsMapper.updateByPrimaryKey(playerGoods);
    }

}
