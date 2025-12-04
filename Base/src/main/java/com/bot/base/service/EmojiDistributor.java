package com.bot.base.service;

import com.bot.base.dto.CommonResp;

public interface EmojiDistributor {

    /**
     * 处理表情
     * @param msgId
     * @param token
     * @param groupId
     * @return
     */
    CommonResp dealEmoji(String md5, int length, Long msgId, String token, String groupId);

}
