package com.bot.commom.util;

/**
 * @author murongyehua
 * @version 1.0 2020/11/2
 */
public class IndexUtil {

    public static String getIndex(int index) {
        if (index > 9) {
            return String.valueOf(index);
        }
        return "0" + index;
    }
}
