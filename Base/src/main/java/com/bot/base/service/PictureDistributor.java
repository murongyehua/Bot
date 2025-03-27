package com.bot.base.service;

import com.bot.base.dto.CommonResp;

public interface PictureDistributor {

    /**
     * 处理图片
     * @param content
     * @param msgId
     * @param token
     * @param groupId
     * @return
     */
    CommonResp dealPicture(String content, Long msgId, String token, String groupId);

}
