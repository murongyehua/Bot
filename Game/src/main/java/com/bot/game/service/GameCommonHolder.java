package com.bot.game.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.bot.common.constant.GameConsts;
import com.bot.common.util.ThreadPoolManager;
import com.bot.game.dao.entity.BaseMonster;
import com.bot.game.dao.entity.Game;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.mapper.BaseMonsterMapper;
import com.bot.game.dao.mapper.GameMapper;
import com.bot.game.dao.mapper.GamePlayerMapper;
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

    @Autowired
    private GamePlayerMapper gamePlayerMapper;

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
                        log.info("初始化boos信息..");
                        WorldBossServiceImpl.boos = WorldBossServiceImpl.allBoos.get(RandomUtil.randomInt(WorldBossServiceImpl.allBoos.size()));
                        log.info("初始化boos信息完成");
                    }
                    // 每60分钟尝试一次初始化
                    Thread.sleep(60 * 60 * 1000);
                } catch (Exception e) {
                    log.error("任务异常", e);
                }
            }
        });

        ThreadPoolManager.addBaseTask(() -> {
            while (true) {
                try {
                    List<GamePlayer> list = gamePlayerMapper.getBySoulPowerDesc();
                    list.forEach(x -> {
                        addActionPoint(x.getId(), 1);
                    });
                }catch (Exception e) {
                    log.error("任务异常", e);
                }finally {
                    try{
                        // 每5分钟恢复1点
                        Thread.sleep(5 * 60 * 1000);
                    }catch (Exception e) {
                        // do nothing
                    }
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

    public void addActionPoint(String token, Integer number) {
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        int finalNumber = gamePlayer.getActionPoint() + number;
        if (finalNumber <= 100) {
            gamePlayer.setActionPoint(finalNumber);
            gamePlayerMapper.updateByPrimaryKey(gamePlayer);
        }
    }

}
