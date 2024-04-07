package com.bot.base.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.MorningReq;
import com.bot.base.dto.TencentChatReq;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;

public class Test {

    public static void main(String[] args) {
        String morningUrl = "https://api.linhun.vip/api/jhrsrb?type=weibo&apiKey=a3021e4da42d696c225d7339abd4970b";
        String morningKey = "a3021e4da42d696c225d7339abd4970b";
        String type = "weibo";
        MorningReq req = new MorningReq();
        req.setApiKey(morningKey);
        req.setType(type);
        try {
            String weiboResponse = HttpSenderUtil.get(morningUrl, null);
            JSONArray dataList = (JSONArray) JSONUtil.parseObj(weiboResponse).get("data");
            StringBuilder stringBuilder = new StringBuilder();
            for(int index = 0; index < 10; index++) {
                JSONObject object = (JSONObject) dataList.get(index);
                stringBuilder.append((String) object.get("title")).append(StrUtil.CRLF);
                stringBuilder.append((String) object.get("mobilUrl")).append(StrUtil.CRLF);
            }
            System.out.println(stringBuilder);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
