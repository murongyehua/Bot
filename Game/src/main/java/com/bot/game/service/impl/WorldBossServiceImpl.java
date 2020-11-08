package com.bot.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.dao.entity.BaseMonster;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dto.BattlePhantomDTO;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

/**
 * @author murongyehua
 * @version 1.0 2020/10/28
 */
@Slf4j
public class WorldBossServiceImpl extends CommonPlayer {

    public volatile static BattlePhantomDTO boos = new BattlePhantomDTO();

    public static List<BattlePhantomDTO> allBoos = new LinkedList<>();

    public static Map<String, Integer> joinTimes = new HashMap<>();

    private PlayerPhantom playerPhantom;

    public WorldBossServiceImpl(PlayerPhantom playerPhantom) {
        this.playerPhantom = playerPhantom;
        this.title = String.format(GameConsts.Battle.TITLE,
                playerPhantom.getAppellation(), playerPhantom.getName(), playerPhantom.getLevel());;
    }

    @Override
    public String doPlay(String token) {
        if (judgeNotInTime()) {
            return GameConsts.WorldBoss.NOT_IN_TIME;
        }
        if (boos.getFinalHp() <= 0) {
            return GameConsts.WorldBoss.FINISH;
        }
        Integer times = joinTimes.get(token);
        if (times != null && times.equals(GameConsts.WorldBoss.MAX_TIME)) {
            return GameConsts.WorldBoss.OVER_TIMES;
        }
        if (times == null) {
            joinTimes.put(token, 1);
        }else {
            joinTimes.put(token, times + 1);
        }
        BaseMonster baseMonster = new BaseMonster();
        BeanUtil.copyProperties(boos, baseMonster);
        BattleServiceImpl battleService = new BattleServiceImpl(baseMonster, playerPhantom, false, true, false);
        return battleService.doPlay(token);
    }

    public static boolean judgeNotInTime() {
        Date now = new Date();
        Date amStartDate = DateUtil.parse(DateUtil.format(now, DatePattern.PURE_DATE_PATTERN) + "110000", DatePattern.PURE_DATETIME_PATTERN);
        Date amEndDate = DateUtil.parse(DateUtil.format(now, DatePattern.PURE_DATE_PATTERN) + "130000", DatePattern.PURE_DATETIME_PATTERN);
        Date pmStartDate = DateUtil.parse(DateUtil.format(now, DatePattern.PURE_DATE_PATTERN) + "170000", DatePattern.PURE_DATETIME_PATTERN);
        Date pmEndDate = DateUtil.parse(DateUtil.format(now, DatePattern.PURE_DATE_PATTERN) + "190000", DatePattern.PURE_DATETIME_PATTERN);
        if (DateUtil.isIn(now, amStartDate, amEndDate) || DateUtil.isIn(now, pmStartDate, pmEndDate)) {
            return false;
        }
        return true;
    }

}
