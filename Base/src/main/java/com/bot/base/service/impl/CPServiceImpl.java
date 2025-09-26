package com.bot.base.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("cpServiceImpl")
public class CPServiceImpl implements BaseService {

    private static final String PIC_URL = "http://113.45.63.97/file/picCache/";

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        String[] reqs = reqContent.split(StrUtil.SPACE);
        if (reqs.length < 3) {
            return new CommonResp("cp宇宙获取失败，请检查指令格式", ENRespType.TEXT.getType());
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name1", reqs[1]);
        paramMap.put("name2", reqs[2]);
        if (reqs.length > 3) {
            paramMap.put("type", reqs[3]);
        }
        paramMap.put("data", "img");
        String url = "https://api.xingzhige.com/API/cp_generate_2/?";
        HttpResponse response = HttpRequest.post(url)
                .form(paramMap)
                .execute();
        String fileName = "cp_" + System.currentTimeMillis() + ".png";
        if (response.isOk()) {
            // 获取二进制图片数据
            byte[] imageData = response.bodyBytes();

            // 指定保存路径
            String savePath = "/data/files/picCache/" + fileName;
            // 确保目录存在
            FileUtil.mkParentDirs(savePath);
            // 写入文件
            File file = FileUtil.writeBytes(imageData, savePath);
            log.info("图片已保存至: " + file.getAbsolutePath());
        } else {
            log.info("请求失败: HTTP " + response.getStatus());
        }
        return new CommonResp(PIC_URL + fileName, ENRespType.IMG.getType());
    }

}
