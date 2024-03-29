package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import com.bot.common.loader.CommonTextLoader;
import com.bot.common.constant.BaseConsts;
import org.springframework.stereotype.Service;


/**
 * 答案之书服务
 * @author murongyehua
 * @version 1.0 2020/9/25
 */
@Service(BaseConsts.ClassBeanName.ANSWER_BOOK)
public class AnswerBookServiceImpl implements BaseService {

    @Override
    public CommonResp doQueryReturn(String reqContent, String token) {
        int index = RandomUtil.randomInt(0, CommonTextLoader.answers.size());
        return new CommonResp(CommonTextLoader.answers.get(index), ENRespType.TEXT.getType());
    }
}
