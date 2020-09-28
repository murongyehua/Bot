package com.bot.base.commom;

import com.bot.commom.util.HttpSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/9/22
 */
@Slf4j
@Component
public class MessageSender {

    @Value("${system.robot.qq}")
    private String robotQQ ;

    @Value("${system.message.send.url}")
    private String url;

    @Value("${system.message.send.key}")
    private String key;

    public String send(String token, String message) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("RobotQQ", robotQQ));
        params.add(new BasicNameValuePair("Key", key));
        params.add(new BasicNameValuePair("QQ", token));
        params.add(new BasicNameValuePair("Message", message));
        try {
            String result = HttpSenderUtil.get(url, params);
            log.info("目标[{}],消息发送完毕，响应结果[{}]", token, result);
            return result;
        }catch (Exception e){
            log.error("目标[{}],请求异常");
        }
        return null;
    }

}
