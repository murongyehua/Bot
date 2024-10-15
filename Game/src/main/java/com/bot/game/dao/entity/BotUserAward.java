package com.bot.game.dao.entity;

import java.io.Serializable;

public class BotUserAward implements Serializable {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_award.id
     *
     * @mbggenerated
     */
    private String id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_award.user_id
     *
     * @mbggenerated
     */
    private String userId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_award.activity_award_id
     *
     * @mbggenerated
     */
    private String activityAwardId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_award.award_name
     *
     * @mbggenerated
     */
    private String awardName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_award.activity_id
     *
     * @mbggenerated
     */
    private String activityId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table bot_user_award
     *
     * @mbggenerated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_award.id
     *
     * @return the value of bot_user_award.id
     *
     * @mbggenerated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_award.id
     *
     * @param id the value for bot_user_award.id
     *
     * @mbggenerated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_award.user_id
     *
     * @return the value of bot_user_award.user_id
     *
     * @mbggenerated
     */
    public String getUserId() {
        return userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_award.user_id
     *
     * @param userId the value for bot_user_award.user_id
     *
     * @mbggenerated
     */
    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_award.activity_award_id
     *
     * @return the value of bot_user_award.activity_award_id
     *
     * @mbggenerated
     */
    public String getActivityAwardId() {
        return activityAwardId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_award.activity_award_id
     *
     * @param activityAwardId the value for bot_user_award.activity_award_id
     *
     * @mbggenerated
     */
    public void setActivityAwardId(String activityAwardId) {
        this.activityAwardId = activityAwardId == null ? null : activityAwardId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_award.award_name
     *
     * @return the value of bot_user_award.award_name
     *
     * @mbggenerated
     */
    public String getAwardName() {
        return awardName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_award.award_name
     *
     * @param awardName the value for bot_user_award.award_name
     *
     * @mbggenerated
     */
    public void setAwardName(String awardName) {
        this.awardName = awardName == null ? null : awardName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_award.activity_id
     *
     * @return the value of bot_user_award.activity_id
     *
     * @mbggenerated
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_award.activity_id
     *
     * @param activityId the value for bot_user_award.activity_id
     *
     * @mbggenerated
     */
    public void setActivityId(String activityId) {
        this.activityId = activityId == null ? null : activityId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_user_award
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
        BotUserAward other = (BotUserAward) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getActivityAwardId() == null ? other.getActivityAwardId() == null : this.getActivityAwardId().equals(other.getActivityAwardId()))
            && (this.getAwardName() == null ? other.getAwardName() == null : this.getAwardName().equals(other.getAwardName()))
            && (this.getActivityId() == null ? other.getActivityId() == null : this.getActivityId().equals(other.getActivityId()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_user_award
     *
     * @mbggenerated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getActivityAwardId() == null) ? 0 : getActivityAwardId().hashCode());
        result = prime * result + ((getAwardName() == null) ? 0 : getAwardName().hashCode());
        result = prime * result + ((getActivityId() == null) ? 0 : getActivityId().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_user_award
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
        sb.append(", userId=").append(userId);
        sb.append(", activityAwardId=").append(activityAwardId);
        sb.append(", awardName=").append(awardName);
        sb.append(", activityId=").append(activityId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}