package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import com.bot.common.loader.CommonTextLoader;
import com.bot.common.constant.BaseConsts;
import com.bot.common.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * 答案之书服务
 * @author murongyehua
 * @version 1.0 2020/9/25
 */
@Service(BaseConsts.ClassBeanName.ANSWER_BOOK)
public class AnswerBookServiceImpl implements BaseService {

    @Value("${answer.book.url}")
    private String url;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        String extContent = reqContent.replaceAll(BaseConsts.Menu.ANSWER_BOOK, "").trim();
        if (StrUtil.isEmpty(extContent)) {
            return new CommonResp("请带上你的问题再使用答案之书", ENRespType.TEXT.getType());
        }
        String resultObj = HttpSenderUtil.get(url + "?question=" + extContent, null);
        JSONObject data = (JSONObject) JSONUtil.parseObj(resultObj).get("data");
        String result = (String) data.get("description_zh");
        return new CommonResp(result, ENRespType.TEXT.getType());
    }
}
