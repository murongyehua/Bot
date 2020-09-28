package com.bot.base.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.bot.base.service.BaseService;
import com.bot.base.service.CommonTextLoader;
import com.bot.commom.constant.BaseConsts;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 答案之书服务
 * @author liul
 * @version 1.0 2020/9/25
 */
@Service(BaseConsts.ClassBeanName.ANSWER_BOOK)
public class AnswerBookServiceImpl implements BaseService {

    @Override
    public String doQueryReturn(String reqContent, String token) {
        int index = RandomUtil.randomInt(0, CommonTextLoader.answers.size());
        return CommonTextLoader.answers.get(index);
    }
}
