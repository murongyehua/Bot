package com.bot.base.service.impl;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.DeepChatReq;
import com.bot.base.dto.MorningReq;
import com.bot.common.util.HttpSenderUtil;

public class Test {

    public static void main(String[] args) {
        String resp = JSONUtil.toJsonStr(new DeepChatReq(new JSONObject(), "111", "blocking", "111", "111"));
        System.out.println(resp);
    }
}
