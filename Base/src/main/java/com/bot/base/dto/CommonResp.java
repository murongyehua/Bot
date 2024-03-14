package com.bot.base.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommonResp {

    private String msg;

    /**
     * 0-文本 1-图片 2-视频 3-文件
     */
    private String type;

}
