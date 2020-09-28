package com.bot.base.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liul
 * @version 1.0 2020/9/28
 */
@Service
public class CommonTextLoader {

    /**
     * 服务指令
     * key-指令 value-服务类名
     */
    public static Map<String, String> serviceInstructMap;
    /**
     * 菜单指令
     * key-指令 value-菜单类名
     */
    public static Map<String, String> menuInstructMap;

    /**
     * 固定回答
     * key-接收到的消息 value-回答的内容列表，随机取一个返回
     */
    public static Map<String, List<String>> someResponseMap;

    /**
     * 出现未知指令时的默认返回消息，随机取其一
     */
    public static List<String> defaultResponseMsg;

    /**
     * 答案之书
     */
    public static List<String> answers;

    @Value("${text.path}")
    private String textPath;

    @PostConstruct
    public void loadText() {
        // 加载服务指令
        List<String> serviceInstructs = FileUtil.readLines(textPath + "serviceInstructCode.txt", "utf-8");
        serviceInstructMap = this.baseLoad(serviceInstructs);
        // 加载菜单指令
        List<String> menuInstructs = FileUtil.readLines(textPath + "menuInstructCode.txt", "utf-8");
        menuInstructMap = this.baseLoad(menuInstructs);
        // 加载默认回复
        defaultResponseMsg = FileUtil.readLines(textPath + "defaultResponse.txt", "utf-8");
        // 加载固定回答
        List<String> someResponse = FileUtil.readLines(textPath + "someResponse.txt", "utf-8");
        someResponseMap = new HashMap<>(16);
        for (String response : someResponse) {
            String[] keysAndResponses = response.split(BaseConsts.Distributor.SPLIT_REG);
            String[] keys = keysAndResponses[0].split(StrUtil.COMMA);
            for (String key : keys) {
                someResponseMap.put(key, Arrays.asList(keysAndResponses[1].split(StrUtil.COMMA)));
            }
        }
        // 加载答案之书
        answers = FileUtil.readLines(textPath + "answerBook.txt", "utf-8");
    }

    private Map<String, String> baseLoad(List<String> instructs) {
        Map<String, String> instructMap = new HashMap<>(instructs.size());
        for (String instruct : instructs) {
            String[] strings = instruct.split(BaseConsts.Distributor.SPLIT_REG);
            String[] codes = strings[0].split(StrUtil.COMMA);
            for (String code : codes) {
                instructMap.put(code, strings[1]);
            }
        }
        return instructMap;
    }
}
