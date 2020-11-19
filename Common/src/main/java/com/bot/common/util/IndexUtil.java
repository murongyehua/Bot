package com.bot.common.util;

/**
 * @author murongyehua
 * @version 1.0 2020/11/2
 */
public class IndexUtil {

    public static String getIndex(int index) {
        return String.valueOf(index);
    }

    public static String fullIndex(String indexStr) {
        int index = Integer.parseInt(indexStr);
        if (index > 9) {
            return String.valueOf(index);
        }
        return "0" + index;
    }

    public static String subIndex(String indexStr) {
        if (indexStr.startsWith("0")) {
            return indexStr.substring(1);
        }
        return indexStr;
    }
}
