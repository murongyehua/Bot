package com.bot.base.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import org.springframework.stereotype.Service;

@Service("coinServiceImpl")
public class CoinServiceImpl implements BaseService {
    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (ObjectUtil.notEqual(reqContent, "抛硬币")) {
            return null;
        }
        double rand = Math.random();
        if (rand <= 0.5) {
            return new CommonResp("正面", ENRespType.TEXT.getType());
        }else {
            return new CommonResp("反面", ENRespType.TEXT.getType());
        }
    }
}
