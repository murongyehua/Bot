package com.bot.life.dao.mapper;

import com.bot.life.dao.entity.LifeMonsterSkill;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 怪物技能Mapper接口
 * @author Assistant
 */
public interface LifeMonsterSkillMapper {
    int deleteByPrimaryKey(Long id);
    int insert(LifeMonsterSkill record);
    LifeMonsterSkill selectByPrimaryKey(Long id);
    int updateByPrimaryKey(LifeMonsterSkill record);
    
    /**
     * 根据怪物ID查询技能列表（包含技能详情）
     */
    List<LifeMonsterSkill> selectByMonsterId(@Param("monsterId") Long monsterId);
}
