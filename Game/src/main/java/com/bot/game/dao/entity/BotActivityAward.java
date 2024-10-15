package com.bot.game.dao.entity;

import java.io.Serializable;

public class BotActivityAward implements Serializable {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_activity_award.id
     *
     * @mbggenerated
     */
    private String id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_activity_award.activity_id
     *
     * @mbggenerated
     */
    private String activityId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_activity_award.award_id
     *
     * @mbggenerated
     */
    private String awardId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_activity_award.type
     *
     * @mbggenerated
     */
    private String type;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_activity_award.percent
     *
     * @mbggenerated
     */
    private String percent;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_activity_award.prefix
     *
     * @mbggenerated
     */
    private String prefix;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_activity_award.number
     *
     * @mbggenerated
     */
    private String number;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_activity_award.id
     *
     * @return the value of bot_activity_award.id
     *
     * @mbggenerated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_activity_award.id
     *
     * @param id the value for bot_activity_award.id
     *
     * @mbggenerated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_activity_award.activity_id
     *
     * @return the value of bot_activity_award.activity_id
     *
     * @mbggenerated
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_activity_award.activity_id
     *
     * @param activityId the value for bot_activity_award.activity_id
     *
     * @mbggenerated
     */
    public void setActivityId(String activityId) {
        this.activityId = activityId == null ? null : activityId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_activity_award.award_id
     *
     * @return the value of bot_activity_award.award_id
     *
     * @mbggenerated
     */
    public String getAwardId() {
        return awardId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_activity_award.award_id
     *
     * @param awardId the value for bot_activity_award.award_id
     *
     * @mbggenerated
     */
    public void setAwardId(String awardId) {
        this.awardId = awardId == null ? null : awardId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_activity_award.type
     *
     * @return the value of bot_activity_award.type
     *
     * @mbggenerated
     */
    public String getType() {
        return type;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_activity_award.type
     *
     * @param type the value for bot_activity_award.type
     *
     * @mbggenerated
     */
    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_activity_award.percent
     *
     * @return the value of bot_activity_award.percent
     *
     * @mbggenerated
     */
    public String getPercent() {
        return percent;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_activity_award.percent
     *
     * @param percent the value for bot_activity_award.percent
     *
     * @mbggenerated
     */
    public void setPercent(String percent) {
        this.percent = percent == null ? null : percent.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_activity_award.prefix
     *
     * @return the value of bot_activity_award.prefix
     *
     * @mbggenerated
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_activity_award.prefix
     *
     * @param prefix the value for bot_activity_award.prefix
     *
     * @mbggenerated
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? null : prefix.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_activity_award.number
     *
     * @return the value of bot_activity_award.number
     *
     * @mbggenerated
     */
    public String getNumber() {
        return number;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_activity_award.number
     *
     * @param number the value for bot_activity_award.number
     *
     * @mbggenerated
     */
    public void setNumber(String number) {
        this.number = number == null ? null : number.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        BotActivityAward other = (BotActivityAward) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getActivityId() == null ? other.getActivityId() == null : this.getActivityId().equals(other.getActivityId()))
            && (this.getAwardId() == null ? other.getAwardId() == null : this.getAwardId().equals(other.getAwardId()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getPercent() == null ? other.getPercent() == null : this.getPercent().equals(other.getPercent()))
            && (this.getPrefix() == null ? other.getPrefix() == null : this.getPrefix().equals(other.getPrefix()))
            && (this.getNumber() == null ? other.getNumber() == null : this.getNumber().equals(other.getNumber()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getActivityId() == null) ? 0 : getActivityId().hashCode());
        result = prime * result + ((getAwardId() == null) ? 0 : getAwardId().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getPercent() == null) ? 0 : getPercent().hashCode());
        result = prime * result + ((getPrefix() == null) ? 0 : getPrefix().hashCode());
        result = prime * result + ((getNumber() == null) ? 0 : getNumber().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_activity_award
     *
     * @mbggenerated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", activityId=").append(activityId);
        sb.append(", awardId=").append(awardId);
        sb.append(", type=").append(type);
        sb.append(", percent=").append(percent);
        sb.append(", prefix=").append(prefix);
        sb.append(", number=").append(number);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}