package com.bot.game.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.commom.util.ThreadPoolManager;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.BaseMonsterMapper;
import com.bot.game.dao.mapper.BaseWeaponMapper;
import com.bot.game.dao.mapper.GamePlayerMapper;
import com.bot.game.dao.mapper.PlayerAppellationMapper;
import com.bot.game.dto.DungeonGroupDTO;
import com.bot.game.dto.DungeonSinglePlayerDTO;
import com.bot.game.dto.DungeonTryTimesDTO;
import com.bot.game.enums.ENAppellation;
import com.bot.game.enums.ENDungeon;
import com.bot.game.enums.ENDungeonResult;
import com.bot.game.service.impl.BattleServiceImpl;
import com.bot.game.service.impl.CommonPlayer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author murongyehua
 * @version 1.0 2020/11/5
 */
@Slf4j
@Component
public class DungeonCommonHolder {
    /**
     * 副本队伍，每日零点清空
     */
    public final static Map<String, List<DungeonGroupDTO>> DUNGEON_GROUP = new HashMap<>();
    /**
     * 次数统计，每日零点清空
     */
    public static Map<String, List<DungeonTryTimesDTO>> tryTimes = new HashMap<>();

    @Autowired
    private BaseMonsterMapper baseMonsterMapper;

    @Autowired
    private BaseWeaponMapper baseWeaponMapper;

    @Autowired
    private GamePlayerMapper gamePlayerMapper;

    @Autowired
    private PlayerAppellationMapper playerAppellationMapper;

    private StringBuilder detailRecord;

    @PostConstruct
    public void initDungeon() {
        // 初始化副本组队信息
        log.info("开始初始化副本信息...");
        ENDungeon[] enDungeons = ENDungeon.values();
        for (ENDungeon enDungeon : enDungeons) {
            DUNGEON_GROUP.put(enDungeon.getValue(), new LinkedList<>());
        }
        log.info("副本信息初始化完毕");
        // 神秘商店折扣初始化
        CommonPlayer.nowSale = RandomUtil.randomInt(6,11);
        // 第一称号分配
        initRankTop();
        ThreadPoolManager.addBaseTask(() -> {
            // 每日零点重置副本挑战次数，清空未组齐的队伍
            while (true) {
                try {
                    Date now = new Date();
                    if (DateUtil.isIn(now, DateUtil.parse("000000", DatePattern.PURE_TIME_PATTERN), DateUtil.parse("003000", DatePattern.PURE_TIME_PATTERN))) {
                        tryTimes.clear();
                        DUNGEON_GROUP.clear();
                        for (ENDungeon enDungeon : enDungeons) {
                            DUNGEON_GROUP.put(enDungeon.getValue(), new LinkedList<>());
                        }
                        // 神秘商店折扣刷新
                        CommonPlayer.nowSale = RandomUtil.randomInt(6,11);
                        // 第一称号刷新
                        initRankTop();
                    }
                }catch (Exception e) {
                    log.error("副本每日初始化任务异常", e);
                }finally {
                    try {
                        Thread.sleep(30 * 60 * 1000);
                    }catch (Exception e) {
                        // do nothing
                    }

                }
            }
        });

        ThreadPoolManager.addBaseTask(() -> {
            while (true) {
                try {
                    this.roundDungeonGroup();
                }catch (Exception e) {
                    log.error("副本轮循任务异常", e);
                }finally {
                    try {
                        Thread.sleep(2 * 60 * 1000);
                    }catch (Exception e) {
                        // do nothing
                    }

                }
            }
        });

    }

    private void roundDungeonGroup() {
        ENDungeon[] enDungeons = ENDungeon.values();
        for (ENDungeon enDungeon : enDungeons) {
            List<DungeonGroupDTO> groups = DUNGEON_GROUP.get(enDungeon.getValue());
            for (DungeonGroupDTO group : groups) {
                if (group.getPlayers().size() >= 2 && group.getResult().equals(ENDungeonResult.WAIT)) {
                    List<BaseMonster> monsters = this.getBoos(enDungeon);
                    if (this.dungeonBattle(group, monsters)) {
                        // 战斗胜利
                        group.setResult(ENDungeonResult.SUCCESS);
                        for (DungeonSinglePlayerDTO player : group.getPlayers()) {
                            group.getResultMap().put(player.getPlayerId(), GameConsts.Dungeon.VICTORY + this.addSuccessGoods(player.getPlayerId(), enDungeon));
                        }
                        return;
                    }
                    // 战斗失败
                    group.setResult(ENDungeonResult.FAIL);
                    for (DungeonSinglePlayerDTO player : group.getPlayers()) {
                        CommonPlayer.addOrSubMoney(player.getPlayerId(), GameConsts.Money.DUNGEON_FAIL);
                        group.getResultMap().put(player.getPlayerId(), GameConsts.Dungeon.FAIL + StrUtil.CRLF + "战斗详情" + StrUtil.CRLF + detailRecord.toString());
                    }
                }
            }
        }
    }

    private List<BaseMonster> getBoos(ENDungeon enDungeon) {
        BaseMonster param = new BaseMonster();
        param.setArea(enDungeon.getValue());
        return baseMonsterMapper.selectBySelective(param);
    }

    private boolean dungeonBattle(DungeonGroupDTO group, List<BaseMonster> monsters) {
        detailRecord = new StringBuilder();
        Integer[] hps = new Integer[]{0, 0};
        List<DungeonSinglePlayerDTO> players = group.getPlayers();
        for (DungeonSinglePlayerDTO player : players) {
            List<PlayerPhantom> playerPhantoms = player.getPhantoms();
            for (PlayerPhantom playerPhantom : playerPhantoms) {
                for (int index = 0; index < monsters.size(); index++) {
                    BattleServiceImpl battleService = new BattleServiceImpl(monsters.get(index), playerPhantom, true, hps[index]);
                    String tempResult = battleService.doPlay(playerPhantom.getPlayerId());
                    // 分析结果
                    detailRecord.append(CommonPlayer.battleDetailMap.get(playerPhantom.getPlayerId())).append(StrUtil.CRLF).append(StrUtil.CRLF);
                    String[] finalHps = tempResult.split(StrUtil.UNDERLINE);
                    if (Integer.parseInt(finalHps[0]) <= 0) {
                        // 这个Boss死掉了
                        playerPhantom.setHp(Integer.parseInt(finalHps[1]));
                    }else {
                        // 幻灵死掉了
                        hps[index] = Integer.parseInt(finalHps[0]);
                        playerPhantom.setHp(0);
                        break;
                    }
                }
            }
        }
        for (DungeonSinglePlayerDTO player : players) {
            List<PlayerPhantom> alivePhantoms = player.getPhantoms().stream().filter(x -> x.getHp() > 0).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(alivePhantoms)) {
                return true;
            }
        }
        return false;
    }

    private String addSuccessGoods(String token, ENDungeon enDungeon) {
        StringBuilder stringBuilder = new StringBuilder();
        // 灵石
        CommonPlayer.addOrSubMoney(token, GameConsts.Money.DUNGEON_SUCCESS);
        stringBuilder.append("灵石*").append(GameConsts.Money.DUNGEON_SUCCESS).append(StrUtil.COMMA);
        int number = RandomUtil.randomInt(0, 100);
        if (number <= GameConsts.Dungeon.GET_DUNGEON) {
            // 法宝
            List<BaseWeapon> list = baseWeaponMapper.selectAll();
            BaseWeapon baseWeapon = list.get(RandomUtil.randomInt(list.size()));
            CommonPlayer.addWeapon(token, baseWeapon);
            CommonPlayer.computeAndUpdateSoulPower(token);
            stringBuilder.append(String.format("法宝[%s]", baseWeapon.getName())).append(StrUtil.COMMA).append("若已拥有将会自动转化为灵气，灵气最高5级").append(StrUtil.CRLF);
        } else {
            // 道具
            BaseGoods baseGoods = CommonPlayer.getResultGoods(enDungeon.getValue());
            if (baseGoods == null) {
                // 保底
                CommonPlayer.addPlayerGoods("29", token, 2);
                stringBuilder.append("天命散 * 2").append(StrUtil.CRLF);
            }else {
                CommonPlayer.addPlayerGoods(baseGoods.getId(), token, 1);
                stringBuilder.append(String.format("%s*%s", baseGoods.getName(), 1)).append(StrUtil.CRLF);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 重置排行榜第一称号
     */
    private void initRankTop() {
        // 删除旧的称号
        playerAppellationMapper.deleteByAppellation(ENAppellation.A03.getAppellation());
        // 最高战力者
        List<GamePlayer> gamePlayers = gamePlayerMapper.getBySoulPowerDesc();
        PlayerAppellation playerAppellation = new PlayerAppellation();
        playerAppellation.setGetTime(new Date());
        playerAppellation.setAppellation(ENAppellation.A03.getAppellation());
        playerAppellation.setId(IdUtil.simpleUUID());
        playerAppellation.setPlayerId(gamePlayers.get(0).getId());
        playerAppellationMapper.insert(playerAppellation);
    }
}
