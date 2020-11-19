package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.common.constant.GameConsts;
import com.bot.common.util.IndexUtil;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.entity.PlayerPhantom;
import com.bot.game.dao.mapper.PlayerPhantomMapper;
import com.bot.game.dto.ExploreBuffDTO;
import com.bot.game.dto.GoodsDetailDTO;
import com.bot.game.dto.UseGoodsDTO;
import com.bot.game.enums.ENGoodEffect;
import com.bot.game.service.impl.CommonPlayer;
import com.bot.game.service.impl.PhantomAddSkillServiceImpl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author murongyehua
 * @version 1.0 2020/10/19
 */
public class UseGoodsPrinter extends Menu {

    private final GoodsDetailDTO goodsDetailDTO;

    UseGoodsPrinter(GoodsDetailDTO goodsDetailDTO) {
        this.goodsDetailDTO = goodsDetailDTO;
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.MyKnapsack.USE_GOODS_MENU;
    }

    @Override
    public void getDescribe(String token) {
        if (goodsDetailDTO.getNumber() == 0) {
            this.describe = GameConsts.MyKnapsack.EMPTY;
            return;
        }
        ENGoodEffect enGoodEffect = ENGoodEffect.getByValue(goodsDetailDTO.getEffect());
        StringBuilder stringBuilder = new StringBuilder();
        switch (enGoodEffect) {
            case GET_PHANTOM:
                stringBuilder.append(GameConsts.MyKnapsack.GET_PHANTOM);
                break;
            case SKILL:
                this.useSkillCard(stringBuilder);
                break;
            case WAN_1:
            case WAN_2:
            case WAN_3:
                this.useWan(enGoodEffect, stringBuilder);
                break;
            case WAN_4:
                this.useResetAttribute(stringBuilder, token);
                break;
            case WAN_5:
                this.useForgetSkill(stringBuilder, token);
                break;
            case WAN_6:
                this.useAddActionPoint(stringBuilder, token, 5, ENGoodEffect.WAN_6);
                break;
            case WAN_7:
                this.useAddActionPoint(stringBuilder, token, 10, ENGoodEffect.WAN_7);
                break;
                default:
                    break;
        }
        this.describe = stringBuilder.toString();
    }

    private void useSkillCard(StringBuilder stringBuilder) {
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom param = new PlayerPhantom();
        param.setPlayerId(goodsDetailDTO.getToken());
        List<PlayerPhantom> list = playerPhantomMapper.selectAllCarried(param);
        List<PlayerPhantom> finalList = list.stream().filter(x -> {
            if (!x.getAttribute().equals(goodsDetailDTO.getAttribute())) {
                return false;
            }
            if (StrUtil.isNotEmpty(x.getSkills())) {
                String[] skills = x.getSkills().split(StrUtil.COMMA);
                if (skills.length >= 3) {
                    return false;
                }
                List<String> tempList = Arrays.asList(skills);
                return !tempList.contains(goodsDetailDTO.getTargetId());
            }
            return true;
        }).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(finalList)) {
            stringBuilder.append(GameConsts.MyKnapsack.USE_SKILL_NO_PHANTOM);
            return;
        }
        for (int index = 0; index < finalList.size(); index++) {
            this.playServiceMap.put(IndexUtil.getIndex(index + 1), new PhantomAddSkillServiceImpl(this.getUseGoodsDTO(goodsDetailDTO, finalList.get(index)), goodsDetailDTO));
        }
        stringBuilder.append(GameConsts.MyKnapsack.USE_SKILL_CARD);
    }

    private UseGoodsDTO getUseGoodsDTO(GoodsDetailDTO goodsDetailDTO, PlayerPhantom playerPhantom) {
        UseGoodsDTO useGoodsDTO = new UseGoodsDTO();
        useGoodsDTO.setToken(goodsDetailDTO.getToken());
        useGoodsDTO.setGoodsId(goodsDetailDTO.getGoodsId());
        useGoodsDTO.setEffect(goodsDetailDTO.getEffect());
        useGoodsDTO.setTitle(GameConsts.GoodsDetail.USE);
        useGoodsDTO.setTargetId(goodsDetailDTO.getTargetId());
        useGoodsDTO.setPlayerPhantom(playerPhantom);
        useGoodsDTO.setNumber(goodsDetailDTO.getNumber());
        useGoodsDTO.setPlayerGoodsId(goodsDetailDTO.getPlayerGoodsId());
        return useGoodsDTO;
    }

    private void useWan(ENGoodEffect enGoodEffect, StringBuilder stringBuilder) {
        ExploreBuffDTO nowBuff = CommonPlayer.exploreBuffMap.get(goodsDetailDTO.getToken());
        if (nowBuff != null) {
            if (System.currentTimeMillis() <= nowBuff.getOutTime().getTime()) {
                stringBuilder.append(GameConsts.MyKnapsack.BUFF_REPEAT);
                return;
            }
            CommonPlayer.exploreBuffMap.remove(goodsDetailDTO.getToken());
        }
        ExploreBuffDTO exploreBuffDTO = new ExploreBuffDTO();
        exploreBuffDTO.setEnGoodEffect(enGoodEffect);
        int time = 0;
        switch (enGoodEffect) {
            case WAN_1:
                time = 10;
                break;
            case WAN_2:
            case WAN_3:
                time = 5;
                break;
            default:
                    break;
        }
        long timeValue = time * 1000 * 60;
        exploreBuffDTO.setOutTime(new Date(System.currentTimeMillis() + timeValue));
        CommonPlayer.exploreBuffMap.put(goodsDetailDTO.getToken(), exploreBuffDTO);
        PlayerGoods playerGoods = new PlayerGoods();
        playerGoods.setId(goodsDetailDTO.getPlayerGoodsId());
        playerGoods.setNumber(goodsDetailDTO.getNumber());
        CommonPlayer.afterUseGoods(playerGoods);
        int nowNumber = goodsDetailDTO.getNumber() - 1;
        goodsDetailDTO.setNumber(Math.max(nowNumber, 0));
        stringBuilder.append(GameConsts.MyKnapsack.BUFF_USE).append(StrUtil.CRLF).append(GameConsts.CommonTip.TURN_BACK);
    }

    private void useResetAttribute(StringBuilder stringBuilder, String token) {
        stringBuilder.append(GameConsts.MyKnapsack.CHOOSE_RESET);
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom param = new PlayerPhantom();
        param.setPlayerId(token);
        List<PlayerPhantom> playerPhantoms = playerPhantomMapper.selectAllCarried(param);
        for (int index=0; index < playerPhantoms.size(); index++) {
            this.menuChildrenMap.put(String.valueOf(index + 1), new UseResetAttributePrinter(playerPhantoms.get(index), goodsDetailDTO));
        }
    }

    private void useForgetSkill(StringBuilder stringBuilder, String token) {
        stringBuilder.append(GameConsts.MyKnapsack.CHOOSE_FORGET);
        PlayerPhantomMapper playerPhantomMapper = (PlayerPhantomMapper) mapperMap.get(GameConsts.MapperName.PLAYER_PHANTOM);
        PlayerPhantom param = new PlayerPhantom();
        param.setPlayerId(token);
        List<PlayerPhantom> playerPhantoms = playerPhantomMapper.selectAllCarried(param);
        for (int index=0; index < playerPhantoms.size(); index++) {
            this.menuChildrenMap.put(String.valueOf(index + 1), new ForgetSkillPrinter(playerPhantoms.get(index), goodsDetailDTO));
        }
    }

    private void useAddActionPoint(StringBuilder stringBuilder, String token, Integer number, ENGoodEffect goodEffect) {
        // 校验
        PlayerGoods playerGoods = CommonPlayer.checkGoodsNumber(token, goodEffect, null);
        if (playerGoods == null) {
            stringBuilder.append(GameConsts.MyKnapsack.EMPTY + StrUtil.CRLF + GameConsts.CommonTip.TURN_BACK);
            return;
        }
        boolean isCan = CommonPlayer.addOrSubActionPoint(token, number);
        if (!isCan) {
            stringBuilder.append(GameConsts.MyKnapsack.ACTION_POINT_FULL);
            return;
        }
        // 扣除
        CommonPlayer.afterUseGoods(playerGoods);
        int nowNumber = goodsDetailDTO.getNumber() - 1;
        goodsDetailDTO.setNumber(Math.max(nowNumber, 0));
        stringBuilder.append(GameConsts.MyKnapsack.BUFF_USE);
    }

}
