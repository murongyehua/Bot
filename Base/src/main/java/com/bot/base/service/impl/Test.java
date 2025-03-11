package com.bot.base.service.impl;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.DeepChatReq;
import com.bot.base.dto.MorningReq;
import com.bot.common.util.HttpSenderUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Test {

    public static void main(String[] args) {
        BigDecimal result = new BigDecimal(200).divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
        System.out.println(result);
    }
}
