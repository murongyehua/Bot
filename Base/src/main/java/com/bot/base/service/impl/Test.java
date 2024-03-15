package com.bot.base.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.TencentChatReq;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;

public class Test {

    public static void main(String[] args) {
        try {
            JSONObject json = JSONUtil.parseObj(HttpSenderUtil.postJsonData("https://luckycola.com.cn/hunyuan/txhy", JSONUtil.toJsonStr(new TencentChatReq("你好", 0, "65f3ea775ee51f638b0fc2e5", "dMv4H01710484087100ZGkMXlypEo"))));
            Integer code = (Integer) json.get("code");
            if (0 == code) {
                String content = (String) ((JSONObject)((JSONObject) json.get("data")).get("result")).get("Content");
                System.out.println(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
