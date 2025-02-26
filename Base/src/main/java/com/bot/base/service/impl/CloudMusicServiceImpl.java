package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 音乐服务
 * @author murongyehua
 * @version 1.0 2020/10/10
 */
@Service("cloudMusicServiceImpl")
public class CloudMusicServiceImpl implements BaseService {

    @Value("${cloud.music.url}")
    private String url;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        int index = RandomUtil.randomInt(0, BaseConsts.Music.SORT.length);
        String sort = BaseConsts.Music.SORT[index];
        String finalUrl = url + "?sort=" + sort + "&format=json";
        JSONObject json = JSONUtil.parseObj(HttpSenderUtil.get(finalUrl, null));
        Integer code = (Integer) json.get("code");
        if (1 == code) {
            JSONObject data = (JSONObject) json.get("data");
            StringBuilder stringBuilder = new StringBuilder();
            String name = (String) data.get("name");
            String url = (String) data.get("url");
            stringBuilder.append(name).append(StrUtil.CRLF);
            stringBuilder.append(url).append(StrUtil.CRLF);
            stringBuilder.append(BaseConsts.Music.TIP);
            return new CommonResp(stringBuilder.toString(), ENRespType.TEXT.getType());
        }
        return new CommonResp(BaseConsts.Music.ERROR, ENRespType.TEXT.getType());
    }

}
