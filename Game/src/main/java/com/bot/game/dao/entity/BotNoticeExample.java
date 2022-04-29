package com.bot.game.dao.entity;

import java.util.ArrayList;
import java.util.List;

public class BotNoticeExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public BotNoticeExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdIsNull() {
            addCriterion("notice_target_id is null");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdIsNotNull() {
            addCriterion("notice_target_id is not null");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdEqualTo(String value) {
            addCriterion("notice_target_id =", value, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdNotEqualTo(String value) {
            addCriterion("notice_target_id <>", value, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdGreaterThan(String value) {
            addCriterion("notice_target_id >", value, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdGreaterThanOrEqualTo(String value) {
            addCriterion("notice_target_id >=", value, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdLessThan(String value) {
            addCriterion("notice_target_id <", value, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdLessThanOrEqualTo(String value) {
            addCriterion("notice_target_id <=", value, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdLike(String value) {
            addCriterion("notice_target_id like", value, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdNotLike(String value) {
            addCriterion("notice_target_id not like", value, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdIn(List<String> values) {
            addCriterion("notice_target_id in", values, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdNotIn(List<String> values) {
            addCriterion("notice_target_id not in", values, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdBetween(String value1, String value2) {
            addCriterion("notice_target_id between", value1, value2, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeTargetIdNotBetween(String value1, String value2) {
            addCriterion("notice_target_id not between", value1, value2, "noticeTargetId");
            return (Criteria) this;
        }

        public Criteria andNoticeContentIsNull() {
            addCriterion("notice_content is null");
            return (Criteria) this;
        }

        public Criteria andNoticeContentIsNotNull() {
            addCriterion("notice_content is not null");
            return (Criteria) this;
        }

        public Criteria andNoticeContentEqualTo(String value) {
            addCriterion("notice_content =", value, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentNotEqualTo(String value) {
            addCriterion("notice_content <>", value, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentGreaterThan(String value) {
            addCriterion("notice_content >", value, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentGreaterThanOrEqualTo(String value) {
            addCriterion("notice_content >=", value, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentLessThan(String value) {
            addCriterion("notice_content <", value, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentLessThanOrEqualTo(String value) {
            addCriterion("notice_content <=", value, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentLike(String value) {
            addCriterion("notice_content like", value, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentNotLike(String value) {
            addCriterion("notice_content not like", value, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentIn(List<String> values) {
            addCriterion("notice_content in", values, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentNotIn(List<String> values) {
            addCriterion("notice_content not in", values, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentBetween(String value1, String value2) {
            addCriterion("notice_content between", value1, value2, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeContentNotBetween(String value1, String value2) {
            addCriterion("notice_content not between", value1, value2, "noticeContent");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeIsNull() {
            addCriterion("notice_type is null");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeIsNotNull() {
            addCriterion("notice_type is not null");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeEqualTo(String value) {
            addCriterion("notice_type =", value, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeNotEqualTo(String value) {
            addCriterion("notice_type <>", value, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeGreaterThan(String value) {
            addCriterion("notice_type >", value, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeGreaterThanOrEqualTo(String value) {
            addCriterion("notice_type >=", value, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeLessThan(String value) {
            addCriterion("notice_type <", value, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeLessThanOrEqualTo(String value) {
            addCriterion("notice_type <=", value, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeLike(String value) {
            addCriterion("notice_type like", value, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeNotLike(String value) {
            addCriterion("notice_type not like", value, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeIn(List<String> values) {
            addCriterion("notice_type in", values, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeNotIn(List<String> values) {
            addCriterion("notice_type not in", values, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeBetween(String value1, String value2) {
            addCriterion("notice_type between", value1, value2, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTypeNotBetween(String value1, String value2) {
            addCriterion("notice_type not between", value1, value2, "noticeType");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeIsNull() {
            addCriterion("notice_time is null");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeIsNotNull() {
            addCriterion("notice_time is not null");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeEqualTo(String value) {
            addCriterion("notice_time =", value, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeNotEqualTo(String value) {
            addCriterion("notice_time <>", value, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeGreaterThan(String value) {
            addCriterion("notice_time >", value, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeGreaterThanOrEqualTo(String value) {
            addCriterion("notice_time >=", value, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeLessThan(String value) {
            addCriterion("notice_time <", value, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeLessThanOrEqualTo(String value) {
            addCriterion("notice_time <=", value, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeLike(String value) {
            addCriterion("notice_time like", value, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeNotLike(String value) {
            addCriterion("notice_time not like", value, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeIn(List<String> values) {
            addCriterion("notice_time in", values, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeNotIn(List<String> values) {
            addCriterion("notice_time not in", values, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeBetween(String value1, String value2) {
            addCriterion("notice_time between", value1, value2, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andNoticeTimeNotBetween(String value1, String value2) {
            addCriterion("notice_time not between", value1, value2, "noticeTime");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagIsNull() {
            addCriterion("at_all_flag is null");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagIsNotNull() {
            addCriterion("at_all_flag is not null");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagEqualTo(String value) {
            addCriterion("at_all_flag =", value, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagNotEqualTo(String value) {
            addCriterion("at_all_flag <>", value, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagGreaterThan(String value) {
            addCriterion("at_all_flag >", value, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagGreaterThanOrEqualTo(String value) {
            addCriterion("at_all_flag >=", value, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagLessThan(String value) {
            addCriterion("at_all_flag <", value, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagLessThanOrEqualTo(String value) {
            addCriterion("at_all_flag <=", value, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagLike(String value) {
            addCriterion("at_all_flag like", value, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagNotLike(String value) {
            addCriterion("at_all_flag not like", value, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagIn(List<String> values) {
            addCriterion("at_all_flag in", values, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagNotIn(List<String> values) {
            addCriterion("at_all_flag not in", values, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagBetween(String value1, String value2) {
            addCriterion("at_all_flag between", value1, value2, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andAtAllFlagNotBetween(String value1, String value2) {
            addCriterion("at_all_flag not between", value1, value2, "atAllFlag");
            return (Criteria) this;
        }

        public Criteria andNoticeDayIsNull() {
            addCriterion("notice_day is null");
            return (Criteria) this;
        }

        public Criteria andNoticeDayIsNotNull() {
            addCriterion("notice_day is not null");
            return (Criteria) this;
        }

        public Criteria andNoticeDayEqualTo(String value) {
            addCriterion("notice_day =", value, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayNotEqualTo(String value) {
            addCriterion("notice_day <>", value, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayGreaterThan(String value) {
            addCriterion("notice_day >", value, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayGreaterThanOrEqualTo(String value) {
            addCriterion("notice_day >=", value, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayLessThan(String value) {
            addCriterion("notice_day <", value, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayLessThanOrEqualTo(String value) {
            addCriterion("notice_day <=", value, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayLike(String value) {
            addCriterion("notice_day like", value, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayNotLike(String value) {
            addCriterion("notice_day not like", value, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayIn(List<String> values) {
            addCriterion("notice_day in", values, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayNotIn(List<String> values) {
            addCriterion("notice_day not in", values, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayBetween(String value1, String value2) {
            addCriterion("notice_day between", value1, value2, "noticeDay");
            return (Criteria) this;
        }

        public Criteria andNoticeDayNotBetween(String value1, String value2) {
            addCriterion("notice_day not between", value1, value2, "noticeDay");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table bot_notice
     *
     * @mbggenerated do_not_delete_during_merge
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table bot_notice
     *
     * @mbggenerated
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}