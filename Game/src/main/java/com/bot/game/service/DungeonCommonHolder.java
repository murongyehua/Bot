package com.bot.game.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.bot.commom.util.ThreadPoolManager;
import com.bot.game.dto.DungeonGroupDTO;
import com.bot.game.dto.DungeonTryTimesDTO;
import com.bot.game.enums.ENDungeon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author liul
 * @version 1.0 2020/11/5
 */
@Slf4j
@Component
public class DungeonCommonHolder {
    /**
     * 副本队伍，每日零点清空
     */
    public static Map<String, List<DungeonGroupDTO>> dungeonGroup = new HashMap<>();
    /**
     * 次数统计，每日零点清空
     */
    public static Map<String, List<DungeonTryTimesDTO>> tryTimes = new HashMap<>();

    @PostConstruct
    public void initDungeon() {
        // 初始化副本组队信息
        log.info("开始初始化副本信息...");
        ENDungeon[] enDungeons = ENDungeon.values();
        for (ENDungeon enDungeon : enDungeons) {
            dungeonGroup.put(enDungeon.getValue(), new LinkedList<>());
        }
        log.info("副本信息初始化完毕");
        ThreadPoolManager.addBaseTask(() -> {
            // 每日零点重置副本挑战次数，清空未组齐的队伍
            Date now = new Date();
            if (DateUtil.isIn(now, DateUtil.parse("000000", DatePattern.PURE_TIME_PATTERN), DateUtil.parse("003000", DatePattern.PURE_TIME_PATTERN))) {
                tryTimes = new HashMap<>();
                dungeonGroup = new HashMap<>();
                for (ENDungeon enDungeon : enDungeons) {
                    dungeonGroup.put(enDungeon.getValue(), new LinkedList<>());
                }
            }
        });
    }

}
