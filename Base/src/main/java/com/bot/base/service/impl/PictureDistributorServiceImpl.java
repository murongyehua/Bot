package com.bot.base.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.PictureDistributor;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import com.bot.common.util.SendMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PictureDistributorServiceImpl implements PictureDistributor {

    public static final Map<String, Date> WAIT_DEAL_PICTURE_MAP = new HashMap<>();

    @Value("${picture.line}")
    private String url;

    @Value("${pic.path}")
    private String picPath;

    @Override
    public CommonResp dealPicture(String content, Long msgId, String token, String groupId) {
        // 现在只支持图片转线稿，如果后续有其他图片需求，这里要改成分发
        if (WAIT_DEAL_PICTURE_MAP.containsKey(token)) {
            String picName = SendMsgUtil.fetchPicture(content, msgId);
            if (picName == null) {
                return new CommonResp("接收图片失败，请联系管理员", ENRespType.TEXT.getType());
            }
            log.info("开始处理线稿:" + picPath+picName);
            String response = HttpUtil.get(url + "?url=" + picPath + picName);
            log.info("处理线稿" + response);
            String targetPic = (String) JSONUtil.parseObj(response).get("data");
            WAIT_DEAL_PICTURE_MAP.remove(token);
            return new CommonResp(targetPic, ENRespType.IMG.getType());
        }
        return null;
    }

}
