package com.bot.base.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("helpServiceImpl")
public class HelpServiceImpl implements BaseService {

    @Value("${help.word.url}")
    private String url;
    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (ObjectUtil.equals(reqContent, "菜单") || ObjectUtil.equals(reqContent, "帮助")) {
            String content = String.format("小林的内容太多图片放不下\r\n改成文档形式啦！！\r\n↓↓也更方便搜索和查找↓↓\r\n%s\r\n\r\n进去后点击右上角的更多菜单可以查看目录快速跳转！", url);
            return new CommonResp(content, ENRespType.TEXT.getType());
        }
        return null;
    }
}
