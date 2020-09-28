package com.bot.base.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.service.BaseService;
import com.bot.commom.util.HttpSenderUtil;
import com.bot.commom.constant.BaseConsts;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 天气查询服务
 * @author murongyehua
 * @version 1.0 2020/9/22
 */
@Slf4j
@Service(BaseConsts.ClassBeanName.WEATHER)
public class WeatherServiceImpl implements BaseService {

    @Value("${weather.url}")
    private String url;

    @Value("${jd.key}")
    private String key;

    @Override
    public String doQueryReturn(String reqContent, String token) {
        String cityStr;
        int length = reqContent.length() - 2;
        Map<String, String> params = new HashMap<>(2);
        params.put(BaseConsts.Weather.KEY_NAME, key);
        for (int i=2;i<=length;i++) {
            cityStr = reqContent.substring(0, i);
            params.put(BaseConsts.Weather.CITY_NAME, toPinyin(cityStr));
            JSONObject heWeather5 = this.tryQuery(params);
            if (heWeather5 != null) {
                return this.getResp(reqContent, cityStr, heWeather5);
            }
        }
        return BaseConsts.Weather.FAIL_QUERY;
    }

    private JSONObject tryQuery(Map<String, String> params){
        try {
            String response = HttpSenderUtil.postNameValuePairs(url, params);
            JSONObject weatherJson = JSONUtil.parseObj(response);
            JSONObject result = (JSONObject) weatherJson.get("result");
            JSONArray heWeather5Arr = (JSONArray) result.get("HeWeather5");
            JSONObject heWeather5 = (JSONObject) heWeather5Arr.get(0);
            String queryStatus = (String) heWeather5.get("status");
            if (BaseConsts.Weather.QUERY_STATUS.equals(queryStatus)) {
                return heWeather5;
            }
        }catch (Exception e) {
            log.error("查询天气出现异常,城市[{}]", params.get(BaseConsts.Weather.CITY_NAME), e);
        }
        return null;
    }

    private String getResp(String content, String city, JSONObject heWeather5) {
        StringBuilder resp = new StringBuilder();
        resp.append(city).append(StrUtil.CRLF);
        JSONObject basic = (JSONObject) heWeather5.get("basic");
        JSONObject update = (JSONObject) basic.get("update");
        String updateTime = (String) update.get("loc");
        JSONArray dailyForecast = (JSONArray) heWeather5.get("daily_forecast");
        boolean isDone = false;
        boolean isNow = false;
        if (content.contains(BaseConsts.Weather.KEYWORD_NOW)) {
            resp.append(this.getNow(heWeather5));
            isDone = true;
            isNow = true;
        }
        if (content.contains(BaseConsts.Weather.KEYWORD_TODAY)) {
            resp.append(this.getData((JSONObject) dailyForecast.get(0)));
            isDone = true;
        }
        if (content.contains(BaseConsts.Weather.KEYWORD_TOMORROW)) {
            resp.append(this.getData((JSONObject) dailyForecast.get(1)));
            isDone = true;
        }
        if (content.contains(BaseConsts.Weather.KEYWORD_AFTER_TOMORROW)) {
            resp.append(this.getData((JSONObject) dailyForecast.get(2)));
            isDone = true;
        }
        if (content.contains(BaseConsts.Weather.SUGGEST)) {
            // 建议
            resp.append(this.getSuggest(heWeather5));
            isDone = true;
        }
        // 全部未命中 按今天处理
        if (!isDone) {
            resp.append(this.getData((JSONObject) dailyForecast.get(0)));
        }
        if (!isNow) {
            resp.append(BaseConsts.Weather.LAST_UPDATE_TIME).append(updateTime).append(StrUtil.CRLF);
        }
        return resp.toString();
    }

    private String getData(JSONObject data) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BaseConsts.Weather.DATE).append((String) data.get("date")).append(StrUtil.CRLF);
        JSONObject cond = (JSONObject) data.get("cond");
        JSONObject tmp = (JSONObject) data.get("tmp");
        JSONObject wind = (JSONObject) data.get("wind");
        stringBuilder.append(BaseConsts.Weather.DAY_COND).append((String) cond.get("txt_d")).append(StrUtil.CRLF);
        stringBuilder.append(BaseConsts.Weather.NIGHT_COND).append((String) cond.get("txt_n")).append(StrUtil.CRLF);
        stringBuilder.append(BaseConsts.Weather.DEG).append((String) tmp.get("min")).append("°")
                .append(StrUtil.DASHED).append((String) tmp.get("max")).append("°").append(StrUtil.CRLF);
        stringBuilder.append(BaseConsts.Weather.WIND).append((String) wind.get("sc")).append("级").append(StrUtil.SPACE)
                .append((String) wind.get("dir")).append(StrUtil.CRLF);
        return stringBuilder.toString();
    }

    private String getNow(JSONObject heWeather5) {
        JSONObject now = (JSONObject) heWeather5.get("now");
        JSONObject cond = (JSONObject) now.get("cond");
        JSONObject wind = (JSONObject) now.get("wind");
        String condStr = (String) cond.get("txt");
        String tmpStr = now.get("tmp") + "°";
        String windStr = (String) wind.get("dir") + wind.get("sc") + "级";
        return String.format(BaseConsts.Weather.NOW_TEXT, condStr, tmpStr, windStr);
    }

    private String getSuggest(JSONObject heWeather5) {
        StringBuilder stringBuilder = new StringBuilder();
        JSONObject suggestion = (JSONObject) heWeather5.get("suggestion");
        JSONObject air = (JSONObject) suggestion.get("air");
        JSONObject comf = (JSONObject) suggestion.get("comf");
        JSONObject cw = (JSONObject) suggestion.get("cw");
        JSONObject drsg = (JSONObject) suggestion.get("drsg");
        JSONObject flu = (JSONObject) suggestion.get("flu");
        JSONObject sport = (JSONObject) suggestion.get("sport");
        JSONObject trav = (JSONObject) suggestion.get("trav");
        JSONObject uv = (JSONObject) suggestion.get("uv");
        stringBuilder.append(BaseConsts.Weather.AIR).append(air.get("brf")).append(StrUtil.CRLF).append(air.get("txt")).append(StrUtil.CRLF).append(StrUtil.CRLF);
        stringBuilder.append(BaseConsts.Weather.COMF).append(comf.get("brf")).append(StrUtil.CRLF).append(comf.get("txt")).append(StrUtil.CRLF).append(StrUtil.CRLF);
        stringBuilder.append(BaseConsts.Weather.CW).append(cw.get("brf")).append(StrUtil.CRLF).append(cw.get("txt")).append(StrUtil.CRLF).append(StrUtil.CRLF);
        stringBuilder.append(BaseConsts.Weather.DRSG).append(drsg.get("brf")).append(StrUtil.CRLF).append(drsg.get("txt")).append(StrUtil.CRLF).append(StrUtil.CRLF);
        stringBuilder.append(BaseConsts.Weather.FLU).append(flu.get("brf")).append(StrUtil.CRLF).append(flu.get("txt")).append(StrUtil.CRLF).append(StrUtil.CRLF);
        stringBuilder.append(BaseConsts.Weather.SPORT).append(sport.get("brf")).append(StrUtil.CRLF).append(sport.get("txt")).append(StrUtil.CRLF).append(StrUtil.CRLF);
        stringBuilder.append(BaseConsts.Weather.TRAV).append(trav.get("brf")).append(StrUtil.CRLF).append(trav.get("txt")).append(StrUtil.CRLF).append(StrUtil.CRLF);
        stringBuilder.append(BaseConsts.Weather.UV).append(uv.get("brf")).append(StrUtil.CRLF).append(uv.get("txt")).append(StrUtil.CRLF).append(StrUtil.CRLF);
        return stringBuilder.toString();
    }

    private String getDate(JSONObject json) {
        // 气象 穿衣 空气质量 舒适 不宜
        return null;
    }

    /**
     * 汉字转为拼音
     * @param chinese
     * @return
     */
    private String toPinyin(String chinese){
        String pinyinStr = "";
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < newChar.length; i++) {
            if (newChar[i] > 128) {
                try {
                    pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0];
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            }else{
                pinyinStr += newChar[i];
            }
        }
        return pinyinStr;
    }

}
