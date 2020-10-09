package com.bot.commom.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

/**
 * @author liul
 * @version 1.0 2020/10/9
 */
@Slf4j
public class TextUtil {

    public static String requestToString(HttpServletRequest request) {
        try {
            ServletInputStream  inputStream = request.getInputStream();
            StringBuilder content = new StringBuilder();
            byte[] b = new byte[1024];
            int lens;
            while ((lens = inputStream.read(b)) > 0) {
                content.append(new String(b, 0, lens));
            }
            return content.toString();
        }catch (Exception e) {
            log.error("流解析异常");
        }
        return null;
    }

}
