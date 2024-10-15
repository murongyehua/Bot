package com.bot.game.dao.mapper;

import com.bot.game.dao.entity.BotActivityAward;
import com.bot.game.dao.entity.BotActivityAwardExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BotActivityAwardMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    int countByExample(BotActivityAwardExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    int deleteByExample(BotActivityAwardExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    int insert(BotActivityAward record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    int insertSelective(BotActivityAward record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    List<BotActivityAward> selectByExample(BotActivityAwardExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    BotActivityAward selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    int updateByExampleSelective(@Param("record") BotActivityAward record, @Param("example") BotActivityAwardExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    int updateByExample(@Param("record") BotActivityAward record, @Param("example") BotActivityAwardExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(BotActivityAward record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(BotActivityAward record);
}