package com.bot.base.service.impl;

import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("dogServiceImpl")
public class DogServiceImpl implements BaseService {

    @Value("${dog.url}")
    private String dogUrl;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token) {
        String extContent = reqContent.replace(BaseConsts.Dog.NAME, StrUtil.EMPTY).trim();
        if (StrUtil.isEmpty(extContent)) {
            return new CommonResp(dogUrl, ENRespType.IMG.getType());
        }else {
            return new CommonResp(dogUrl + "&msg=" + extContent, ENRespType.IMG.getType());
        }
    }

}
