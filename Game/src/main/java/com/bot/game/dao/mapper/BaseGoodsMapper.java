package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.BaseGoods;

import java.util.List;

public interface BaseGoodsMapper {
    int deleteByPrimaryKey(String id);

    int insert(BaseGoods record);

    int insertSelective(BaseGoods record);

    BaseGoods selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BaseGoods record);

    int updateByPrimaryKey(BaseGoods record);

    List<BaseGoods> selectBySelective(BaseGoods record);
}