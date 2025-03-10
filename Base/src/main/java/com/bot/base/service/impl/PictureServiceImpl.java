package com.bot.base.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENPictureType;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author liul
 * @version 1.0 2021/2/19
 */
@Service("pictureServiceImpl")
public class PictureServiceImpl implements BaseService {

    @Value("${picture.path}")
    private String url;

    @Override
    @Deprecated
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        String msg = reqContent.replaceAll(BaseConsts.Picture.SUFFIX, StrUtil.EMPTY);
        String response = HttpSenderUtil.get(url + "?lx=" + ENPictureType.getValueByContainLabel(msg) + "&format=json", null);
        if (StrUtil.isEmpty(response)) {
            return new CommonResp(BaseConsts.Picture.FAIL_QUERY, ENRespType.TEXT.getType());
        }
        JSONObject object = JSONUtil.parseObj(response);
        String code = (String) object.get("code");
        if (!"200".equals(code)) {
            return new CommonResp(BaseConsts.Picture.FAIL_QUERY, ENRespType.TEXT.getType());
        }
        return new CommonResp((String) object.get("imgurl"), ENRespType.IMG.getType());
    }


}
