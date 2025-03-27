package com.bot.base.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.DeepChatReq;
import com.bot.base.dto.MorningReq;
import com.bot.common.util.HttpSenderUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
        List<String> lines = FileUtil.readLines("C:\\Users\\11371\\Desktop\\所有设备.txt", "utf-8");
        int success = 0;
        int fail = 0;
        for (String line : lines) {
            try {
                HttpRequest request = HttpUtil.createGet("http://182.92.193.151:48080/admin-api/main/device-log/notice2UploadLogFile?deviceSerial=" + line);
                Map<String, String> head = new HashMap<>();
                head.put("tenant-id", "163");
                head.put("Authorization", "Bearer test1");
                request.addHeaders(head);
                request.execute();
                System.out.println(line + " -- 通知成功");
                success++;
                Thread.sleep(2 * 1000);
            }catch (Exception e) {
                System.out.println(line + " -- !!通知失败!!");
                fail++;
            }
            System.out.println("成功：" + success + "    失败：" + fail);
        }


    }
}
