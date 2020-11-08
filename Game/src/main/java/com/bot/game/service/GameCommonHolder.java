package com.bot.game.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.commom.util.ThreadPoolManager;
import com.bot.game.dao.entity.BaseMonster;
import com.bot.game.dao.entity.Game;
import com.bot.game.dao.mapper.BaseMonsterMapper;
import com.bot.game.dao.mapper.GameMapper;
import com.bot.game.dto.BattlePhantomDTO;
import com.bot.game.enums.ENCamp;
import com.bot.game.service.impl.WorldBossServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Slf4j
@Component
public class GameCommonHolder {

    public final static List<Game> GAMES = new LinkedList<>();

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private BaseMonsterMapper baseMonsterMapper;

    @PostConstruct
    public void initGame() {
        log.info("开始加载游戏信息..");
        List<Game> games = gameMapper.selectAll();
        GAMES.addAll(games);
        log.info("游戏信息加载完成");
        ThreadPoolManager.addBaseTask(() -> {
            while (true) {
                initAllBoos();
                try {
                    if (WorldBossServiceImpl.judgeNotInTime()) {
                        WorldBossServiceImpl.joinTimes = new LinkedHashMap<>();
                        if (WorldBossServiceImpl.boos.getFinalHp() == null || WorldBossServiceImpl.boos.getFinalHp() == 0) {
                            log.info("初始化boos信息..");
                            WorldBossServiceImpl.boos = WorldBossServiceImpl.allBoos.get(RandomUtil.randomInt(WorldBossServiceImpl.allBoos.size()));
                            log.info("初始化boos信息完成");
                        }
                    }
                    // 每1分钟尝试一次初始化
                    Thread.sleep(60 * 1000);
                } catch (Exception e) {
                    log.error("任务异常", e);
                }
            }
        });
    }

    private void initAllBoos() {
        WorldBossServiceImpl.allBoos = new LinkedList<>();
        BaseMonster param = new BaseMonster();
        param.setArea(ENCamp.BOOS.getValue());
        List<BaseMonster> list = baseMonsterMapper.selectBySelective(param);
        for (BaseMonster baseMonster : list) {
            BattlePhantomDTO battlePhantomDTO = new BattlePhantomDTO();
            BeanUtil.copyProperties(baseMonster, battlePhantomDTO);
            battlePhantomDTO.setHp(GameConsts.WorldBoss.INIT_HP);
            battlePhantomDTO.setFinalHp(GameConsts.WorldBoss.INIT_HP);
            WorldBossServiceImpl.allBoos.add(battlePhantomDTO);
        }
    }

}
