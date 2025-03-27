package com.bot.base.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import org.apache.http.client.methods.HttpPost;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SiliconflowUtil {

    private static final String URL = "https://api.siliconflow.cn/v1";

    private static final String SPEECH_PATH = "/audio/speech";

    private static final String FILE_SAVE_PATH = "/data/files/audio/";

    /**
     * 生成音频文件
     * @param inputContent 文字内容
     * @return 音频文件地址
     */
    public static String speech(String inputContent) {
        // 创建POST请求
        HttpRequest httpRequest = HttpUtil.createPost(URL + SPEECH_PATH);

        // 设置请求头
        Map<String, String> head = new HashMap<>();
        head.put("Authorization", "Bearer sk-ymrdvcfjszkwtelpdclkixnyrouixanwyjrrvktsxazjhijw");
        head.put("Content-Type", "application/json");  // 必须设置JSON类型
        httpRequest.addHeaders(head);

        // 构建JSON请求体（推荐用对象方式更安全）
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("model", "FunAudioLLM/CosyVoice2-0.5B");
        bodyMap.put("input", inputContent);
        bodyMap.put("voice", "FunAudioLLM/CosyVoice2-0.5B:diana");
        bodyMap.put("response_format", "pcm");
        bodyMap.put("sample_rate", 24000);
        bodyMap.put("stream", true);
        bodyMap.put("speed", 1);
        bodyMap.put("gain", 0);

        // 发送请求并获取响应
        try (HttpResponse response = httpRequest
                .body(JSONUtil.toJsonStr(bodyMap))  // 将Map转为JSON字符串
                .timeout(60000)  // 设置超时60秒
                .execute()) {

            if (response.isOk()) {  // 200状态码判断
                // 获取二进制流（根据接口返回类型选择）
                InputStream in = response.bodyStream();

                // 生成唯一文件名（示例：时间戳方式）
                String fileName = "audio_" + System.currentTimeMillis() + ".pcm";

                // 保存到本地（默认项目根目录，可修改路径）
                File outFile = new File(FILE_SAVE_PATH + fileName);
                FileUtil.writeFromStream(in, outFile);

                System.out.println("文件保存成功：" + outFile.getAbsolutePath());
                return fileName;
            } else {
                System.err.println("请求失败：" + response.getStatus()
                        + " | 响应内容：" + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("请求异常：" + e.getMessage());
        }
        return null;

    }

}
