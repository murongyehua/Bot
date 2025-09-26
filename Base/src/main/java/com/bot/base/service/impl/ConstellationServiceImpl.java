package com.bot.base.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENConstellationType;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("constellationServiceImpl")
public class ConstellationServiceImpl implements BaseService {

    @Value("${constellation.path}")
    private String url;

    @Value("${constellation.key}")
    private String key;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        try {
            String consName = reqContent.substring(0, 3);
            // 懒得接其他的了，只支持查询日运势，其他的有需要再说
            String type = "日";
            String typeValue = ENConstellationType.getValueByLabel(type);
            if (typeValue != null) {
                return new CommonResp(this.sendQuery(consName, typeValue), ENRespType.TEXT.getType());
            }
            return new CommonResp(this.sendQuery(consName, ENConstellationType.TODAY.getValue()), ENRespType.TEXT.getType());
        }catch (Exception e) {
            return new CommonResp(BaseConsts.Constellation.ERROR, ENRespType.TEXT.getType());
        }
    }

    private String sendQuery(String consName, String type) {
        String response = HttpSenderUtil.get(url +
                String.format("?consName=%s&type=%s&key=%s", consName, type, key), null);
        if (ENConstellationType.TODAY.getValue().equals(type)) {
            // 日
            JSONObject jsonObject = JSONUtil.parseObj(response);
            String dateTime = (String) jsonObject.get("datetime");
            String friend = (String) jsonObject.get("QFriend");
            String color = (String) jsonObject.get("color");
            String health = (String) jsonObject.get("health");
            String love = (String) jsonObject.get("love");
            String work = (String) jsonObject.get("work");
            String money = (String) jsonObject.get("money");
            String all = (String) jsonObject.get("all");
            Integer number = (Integer) jsonObject.get("number");
            String summary = (String) jsonObject.get("summary");
            return String.format(BaseConsts.Constellation.TODAY_FORMAT, consName, dateTime, friend, color, health, love, work, money, all, number, summary);
        }
        // 其他

        return BaseConsts.Constellation.ERROR;
    }

}
