package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.BaseGoods;
import com.bot.game.dao.entity.BaseSkill;
import com.bot.game.dao.entity.PlayerGoods;
import com.bot.game.dao.mapper.BaseGoodsMapper;
import com.bot.game.dao.mapper.BaseSkillMapper;
import com.bot.game.dao.mapper.PlayerGoodsMapper;
import com.bot.game.dto.GoodsDetailDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/10/15
 */
@Component("myKnapsackMenuPrinter")
public class MyKnapsackMenuPrinter extends Menu {

    MyKnapsackMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = GameConsts.MyKnapsack.MENU_NAME;
    }

    @Override
    public void getDescribe(String token) {
        this.menuChildrenMap.clear();
        PlayerGoodsMapper playerGoodsMapper =  (PlayerGoodsMapper) mapperMap.get(GameConsts.MapperName.PLAYER_GOODS);
        PlayerGoods playerGoods = new PlayerGoods();
        playerGoods.setPlayerId(token);
        List<PlayerGoods> list = playerGoodsMapper.selectBySelective(playerGoods);
        if (CollectionUtil.isEmpty(list)) {
            this.describe = GameConsts.CommonTip.GOODS_EMPTY;
        }else {
            for (int index=0; index < list.size(); index++) {
                this.menuChildrenMap.put(String.valueOf(index + 1), new GoodsDetailMenuPrinter(this.getGoodsDetailDTO(token, list.get(index))));
            }
            this.describe = GameConsts.CommonTip.GOODS_UES;
        }
    }

    private GoodsDetailDTO getGoodsDetailDTO(String token, PlayerGoods playerGoods) {
        GoodsDetailDTO goodsDetailDTO = new GoodsDetailDTO();
        BaseGoodsMapper baseGoodsMapper = (BaseGoodsMapper) mapperMap.get(GameConsts.MapperName.BASE_GOODS);
        BaseGoods baseGoods = baseGoodsMapper.selectByPrimaryKey(playerGoods.getGoodId());
        goodsDetailDTO.setToken(token);
        goodsDetailDTO.setName(baseGoods.getName());
        goodsDetailDTO.setGoodsId(baseGoods.getId());
        goodsDetailDTO.setNumber(playerGoods.getNumber());
        goodsDetailDTO.setDescribe(baseGoods.getDescribe());
        goodsDetailDTO.setEffect(baseGoods.getEffect());
        goodsDetailDTO.setTargetId(baseGoods.getTargetId());
        goodsDetailDTO.setPlayerGoodsId(playerGoods.getId());
        if (StrUtil.isNotEmpty(baseGoods.getTargetId())) {
            BaseSkillMapper baseSkillMapper = (BaseSkillMapper) mapperMap.get(GameConsts.MapperName.BASE_SKILL);
            BaseSkill baseSkill = baseSkillMapper.selectByPrimaryKey(baseGoods.getTargetId());
            goodsDetailDTO.setAttribute(baseSkill.getAttribute());
        }
        return goodsDetailDTO;
    }

    @Override
    public void reInitMenu(String token) {
        this.getDescribe(token);
    }

}
