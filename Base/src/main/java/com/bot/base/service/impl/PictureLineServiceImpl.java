package com.bot.base.service.impl;

import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("pictureLineServiceImpl")
public class PictureLineServiceImpl implements BaseService {

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        PictureDistributorServiceImpl.WAIT_DEAL_PICTURE_MAP.put(token, new Date());
        return new CommonResp("已预备好转线稿，请发送图片", ENRespType.TEXT.getType());
    }

}
