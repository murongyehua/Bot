package com.bot.game.service;

import com.bot.game.dao.entity.Game;
import com.bot.game.dao.mapper.GameMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/10/15
 */
@Slf4j
@Component
public class GameCommonHolder {

    public final static List<Game> GAMES = new LinkedList<>();

    @Autowired
    private GameMapper gameMapper;

    @PostConstruct
    public void initGame() {
        log.info("开始加载游戏信息..");
        List<Game> games = gameMapper.selectAll();
        GAMES.addAll(games);
        log.info("游戏信息加载完成");
    }

}
