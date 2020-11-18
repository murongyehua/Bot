package com.bot.game.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.commom.enums.ENStatus;
import com.bot.game.chain.Collector;
import com.bot.game.dao.entity.Game;
import com.bot.game.dao.entity.GamePlayer;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.mapper.*;
import com.bot.game.dto.CompensateDTO;
import com.bot.game.service.GameCommonHolder;
import com.bot.game.service.GameHandler;
import com.bot.game.service.GameManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
@Service
public class GameHandlerServiceImpl implements GameHandler {

    @Autowired
    private Collector collector;

    @Autowired
    private GamePlayerMapper gamePlayerMapper;

    @Autowired
    private BaseGoodsMapper baseGoodsMapper;

    @Autowired
    private BasePhantomMapper basePhantomMapper;

    @Autowired
    private BaseSkillMapper baseSkillMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private PlayerAppellationMapper playerAppellationMapper;

    @Autowired
    private PlayerFriendsMapper playerFriendsMapper;

    @Autowired
    private PlayerGoodsMapper playerGoodsMapper;

    @Autowired
    private PlayerPhantomMapper playerPhantomMapper;

    @Autowired
    private BaseMonsterMapper baseMonsterMapper;

    @Autowired
    private BaseWeaponMapper baseWeaponMapper;

    @Autowired
    private PlayerWeaponMapper playerWeaponMapper;

    @Autowired
    private GameManageService gameManageService;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private GoodsBoxMapper goodsBoxMapper;

    private final static List<String> WAIT_REG = new LinkedList<>();

    private final static List<String> WAIT_LOGIN = new LinkedList<>();

    @Override
    public String exit(String token) {
        collector.removeToken(token);
        return GameConsts.CommonTip.EXIT_SUCCESS;
    }

    @Override
    public String play(String reqContent, String token) {
        // 游戏是否维护
        Game game = gameMapper.selectAll().get(0);
        if (ENStatus.LOCK.getValue().equals(game.getStatus())) {
            return GameConsts.CommonTip.LOCK;
        }
        // 先查是否已处于在线状态
        if (collector.isOnLine(token)) {
            return collector.toNextOrPrevious(token, reqContent.trim());
        }
        // 已处于待登录状态
        if (WAIT_LOGIN.contains(token)) {
            if (BaseConsts.Menu.ONE.equals(reqContent)) {
                WAIT_LOGIN.remove(token);
                return collector.buildCollector(token, this.getMapperMap());
            }else {
                return GameConsts.CommonTip.UN_KNOW_POINT;
            }
        }
        // 已处于待注册状态
        if (WAIT_REG.contains(token)) {
            if (this.isExsitName(reqContent)) {
                return GameConsts.CommonTip.REPEAT_REG;
            }
            gamePlayerMapper.insert(this.getGamePlayer(token, reqContent));
            // 赠送唤灵符
            PlayerGoods playerGoods = new PlayerGoods();
            playerGoods.setGoodId("1");
            playerGoods.setId(IdUtil.simpleUUID());
            playerGoods.setNumber(3);
            playerGoods.setPlayerId(token);
            playerGoodsMapper.insert(playerGoods);
            WAIT_REG.remove(token);
            return String.format(GameConsts.CommonTip.LOGIN_TIP, reqContent);
        }
        // 判断是注册还是登录
        GamePlayer gamePlayer = gamePlayerMapper.selectByPrimaryKey(token);
        if (gamePlayer == null) {
            WAIT_REG.add(token);
            return GameConsts.CommonTip.REG_TIP;
        }else {
            WAIT_LOGIN.add(token);
            return String.format(GameConsts.CommonTip.LOGIN_TIP, gamePlayer.getNickname());
        }
    }

    @Override
    public String manage(String reqContent) {
        if (reqContent.startsWith(GameConsts.Manage.COMPENSATE_MONEY)) {
            return gameManageService.compensateMoney(Integer.parseInt(reqContent.substring(4).trim()));
        }
        if (reqContent.startsWith(GameConsts.Manage.COMPENSATE)) {
            CompensateDTO compensate = new CompensateDTO();
            String content = reqContent.substring(2);
            String[] contents = content.split("\\|\\|");
            compensate.setGoodsId(contents[0]);
            compensate.setNumber(Integer.valueOf(contents[1]));
            String[] powers = contents[2].split(StrUtil.DASHED);
            compensate.setSoulPowerStart(Integer.valueOf(powers[0]));
            compensate.setSoulPowerEnd(Integer.valueOf(powers[1]));
            return gameManageService.compensate(compensate);
        }
        return GameConsts.CommonTip.ERROR_POINT;
    }

    private GamePlayer getGamePlayer(String token, String nickName) {
        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setId(token);
        gamePlayer.setGameId(GameCommonHolder.GAMES.get(0).getId());
        gamePlayer.setNickname(nickName);
        gamePlayer.setRegTime(new Date());
        gamePlayer.setSoulPower(1);
        gamePlayer.setStatus(ENStatus.NORMAL.getValue());
        gamePlayer.setMoney(0);
        gamePlayer.setActionPoint(100);
        return gamePlayer;
    }

    private boolean isExsitName(String nickName) {
        GamePlayer param = new GamePlayer();
        param.setNickname(nickName);
        List<GamePlayer> list = gamePlayerMapper.selectBySelective(param);
        return CollectionUtil.isNotEmpty(list);
    }

    private Map<String, Object> getMapperMap() {
        Map<String, Object> map = new HashMap<>(12);
        map.put(GameConsts.MapperName.BASE_GOODS, baseGoodsMapper);
        map.put(GameConsts.MapperName.BASE_PHANTOM, basePhantomMapper);
        map.put(GameConsts.MapperName.BASE_SKILL, baseSkillMapper);
        map.put(GameConsts.MapperName.GAME, gameMapper);
        map.put(GameConsts.MapperName.GAME_PLAYER, gamePlayerMapper);
        map.put(GameConsts.MapperName.PLAYER_APPELLATION, playerAppellationMapper);
        map.put(GameConsts.MapperName.PLAYER_FRIENDS, playerFriendsMapper);
        map.put(GameConsts.MapperName.PLAYER_GOODS, playerGoodsMapper);
        map.put(GameConsts.MapperName.PLAYER_PHANTOM, playerPhantomMapper);
        map.put(GameConsts.MapperName.BASE_MONSTER, baseMonsterMapper);
        map.put(GameConsts.MapperName.BASE_WEAPON, baseWeaponMapper);
        map.put(GameConsts.MapperName.PLAYER_WEAPON, playerWeaponMapper);
        map.put(GameConsts.MapperName.MESSAGE, messageMapper);
        map.put(GameConsts.MapperName.GOODS_BOX, goodsBoxMapper);
        return map;
    }
}
