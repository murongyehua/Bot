package com.bot.common.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.bot.common.constant.GameRoomConsts;

import java.util.Date;

/**
 * 游戏房间时间处理工具类
 * @author Assistant
 */
public class GameRoomDateUtil {

    /**
     * 获取当前时间字符串
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    public static String now() {
        return DateUtil.format(new Date(), GameRoomConsts.TIME_PATTERN);
    }

    /**
     * 解析时间字符串
     */
    public static Date parse(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return DateUtil.parse(dateStr, GameRoomConsts.TIME_PATTERN);
    }

    /**
     * 计算时间差(分钟)
     */
    public static long minutesBetween(String startTime, String endTime) {
        Date start = parse(startTime);
        Date end = parse(endTime);
        if (start == null || end == null) {
            return 0;
        }
        return DateUtil.between(start, end, DateUnit.MINUTE);
    }

    /**
     * 判断是否超时(分钟)
     */
    public static boolean isTimeout(String lastTime, int timeoutMinutes) {
        if (lastTime == null || lastTime.trim().isEmpty()) {
            return true;
        }
        long minutes = minutesBetween(lastTime, now());
        return minutes > timeoutMinutes;
    }

    /**
     * 格式化日期
     */
    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        return DateUtil.format(date, GameRoomConsts.TIME_PATTERN);
    }

    /**
     * 获取当前时间Timestamp对象
     */
    public static java.sql.Timestamp nowTimestamp() {
        return java.sql.Timestamp.valueOf(now());
    }
}
