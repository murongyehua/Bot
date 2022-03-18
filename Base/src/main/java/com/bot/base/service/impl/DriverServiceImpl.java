package com.bot.base.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.service.BaseService;
import com.bot.common.util.HttpSenderUtil;
import com.bot.common.util.SendMsgUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("driverServiceImpl")
public class DriverServiceImpl implements BaseService {

    @Value("${driver.path}")
    private String url;

    @Value("${driver.key}")
    private String key;

    static Map<String, String> cache = new HashMap<>();

    @Override
    public String doQueryReturn(String reqContent, String token) {
        if (ObjectUtil.equals(reqContent, "答案")) {
            String answer = cache.get(token);
            if (answer == null) {
                return "什么答案？";
            }
            cache.remove(token);
            return answer;
        }
        String response = HttpSenderUtil.get(url + String.format("?type=c1&subject=1&pagesize=1&pagenum=1&sort=normal&appkey=%s", key), null);
        JSONObject jsonObject = JSONUtil.parseObj(response);
        JSONObject result = (JSONObject) jsonObject.get("result");
        JSONObject resultDetail = (JSONObject) result.get("result");
        JSONArray resultArray = (JSONArray) resultDetail.get("list");
        JSONObject content = resultArray.get(0, JSONObject.class);
        cache.put(token, String.format("%s\r\n%s", content.get("answer"), content.get("explain")));

        // 返回问题
        // 先看有没有图片
        String pic = (String) content.get("pic");
        if (StrUtil.isNotEmpty(pic)) {
            SendMsgUtil.sendImg(token, pic);
        }
        // 发送题目
        String option1 = (String) content.get("option1");
        String option2 = (String) content.get("option2");
        String option3 = (String) content.get("option3");
        String option4 = (String) content.get("option4");
        String question = (String) content.get("question");
        if (StrUtil.isEmpty(option1)) {
            return question;
        }
        return String.format("%s\r\n%s\r\n%s\r\n%s\r\n%s", question, option1, option2, option3, option4);
    }

}
