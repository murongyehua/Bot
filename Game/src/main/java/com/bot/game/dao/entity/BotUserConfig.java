package com.bot.game.dao.entity;

import java.io.Serializable;

public class BotUserConfig implements Serializable {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_config.id
     *
     * @mbggenerated
     */
    private String id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_config.user_id
     *
     * @mbggenerated
     */
    private String userId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_config.chat_engine
     *
     * @mbggenerated
     */
    private String chatEngine;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_config.morning_type
     *
     * @mbggenerated
     */
    private String morningType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_config.work_daily_config
     *
     * @mbggenerated
     */
    private String workDailyConfig;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column bot_user_config.jx_server
     *
     * @mbggenerated
     */
    private String jxServer;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table bot_user_config
     *
     * @mbggenerated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_config.id
     *
     * @return the value of bot_user_config.id
     *
     * @mbggenerated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_config.id
     *
     * @param id the value for bot_user_config.id
     *
     * @mbggenerated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_config.user_id
     *
     * @return the value of bot_user_config.user_id
     *
     * @mbggenerated
     */
    public String getUserId() {
        return userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_config.user_id
     *
     * @param userId the value for bot_user_config.user_id
     *
     * @mbggenerated
     */
    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_config.chat_engine
     *
     * @return the value of bot_user_config.chat_engine
     *
     * @mbggenerated
     */
    public String getChatEngine() {
        return chatEngine;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_config.chat_engine
     *
     * @param chatEngine the value for bot_user_config.chat_engine
     *
     * @mbggenerated
     */
    public void setChatEngine(String chatEngine) {
        this.chatEngine = chatEngine == null ? null : chatEngine.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_config.morning_type
     *
     * @return the value of bot_user_config.morning_type
     *
     * @mbggenerated
     */
    public String getMorningType() {
        return morningType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_config.morning_type
     *
     * @param morningType the value for bot_user_config.morning_type
     *
     * @mbggenerated
     */
    public void setMorningType(String morningType) {
        this.morningType = morningType == null ? null : morningType.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_config.work_daily_config
     *
     * @return the value of bot_user_config.work_daily_config
     *
     * @mbggenerated
     */
    public String getWorkDailyConfig() {
        return workDailyConfig;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_config.work_daily_config
     *
     * @param workDailyConfig the value for bot_user_config.work_daily_config
     *
     * @mbggenerated
     */
    public void setWorkDailyConfig(String workDailyConfig) {
        this.workDailyConfig = workDailyConfig == null ? null : workDailyConfig.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column bot_user_config.jx_server
     *
     * @return the value of bot_user_config.jx_server
     *
     * @mbggenerated
     */
    public String getJxServer() {
        return jxServer;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column bot_user_config.jx_server
     *
     * @param jxServer the value for bot_user_config.jx_server
     *
     * @mbggenerated
     */
    public void setJxServer(String jxServer) {
        this.jxServer = jxServer == null ? null : jxServer.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_user_config
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
        BotUserConfig other = (BotUserConfig) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getChatEngine() == null ? other.getChatEngine() == null : this.getChatEngine().equals(other.getChatEngine()))
            && (this.getMorningType() == null ? other.getMorningType() == null : this.getMorningType().equals(other.getMorningType()))
            && (this.getWorkDailyConfig() == null ? other.getWorkDailyConfig() == null : this.getWorkDailyConfig().equals(other.getWorkDailyConfig()))
            && (this.getJxServer() == null ? other.getJxServer() == null : this.getJxServer().equals(other.getJxServer()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_user_config
     *
     * @mbggenerated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getChatEngine() == null) ? 0 : getChatEngine().hashCode());
        result = prime * result + ((getMorningType() == null) ? 0 : getMorningType().hashCode());
        result = prime * result + ((getWorkDailyConfig() == null) ? 0 : getWorkDailyConfig().hashCode());
        result = prime * result + ((getJxServer() == null) ? 0 : getJxServer().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_user_config
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
        sb.append(", chatEngine=").append(chatEngine);
        sb.append(", morningType=").append(morningType);
        sb.append(", workDailyConfig=").append(workDailyConfig);
        sb.append(", jxServer=").append(jxServer);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}