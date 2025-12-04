package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeSkill;

import java.util.List;

/**
 * 技能Mapper接口
 * @author Assistant
 */
public interface LifeSkillMapper {
    
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入记录
     */
    int insert(LifeSkill record);
    
    /**
     * 根据主键查询
     */
    LifeSkill selectByPrimaryKey(Long id);
    
    /**
     * 查询所有技能
     */
    List<LifeSkill> selectAll();
    
    /**
     * 根据属性查询技能
     */
    List<LifeSkill> selectByAttribute(Integer attribute);
    
    /**
     * 根据技能类型查询
     */
    List<LifeSkill> selectByType(Integer type);
    
    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(LifeSkill record);
}
