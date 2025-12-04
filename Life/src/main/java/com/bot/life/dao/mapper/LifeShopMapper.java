package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeShop;

import java.util.List;

/**
 * 商店Mapper接口
 * @author Assistant
 */
public interface LifeShopMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeShop record);
    
    /**
     * 根据主键查询
     */
    LifeShop selectByPrimaryKey(Long id);
    
    /**
     * 查询所有商店商品
     */
    List<LifeShop> selectAll();
    
    /**
     * 根据道具ID查询商店商品
     */
    LifeShop selectByItemId(Long itemId);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeShop record);
}
