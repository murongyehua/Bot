package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import com.bot.game.dao.entity.BotUserConfig;
import com.bot.game.dao.entity.BotUserConfigExample;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("englishServiceImpl")
public class EnglishServiceImpl implements BaseService {

    @Resource
    private BotUserConfigMapper userConfigMapper;

    @Value("${english.url}")
    private String url;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (reqContent.equals("开启每日英语")) {
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            if (CollectionUtil.isEmpty(userConfigList)) {
                BotUserConfig botUserConfig = new BotUserConfig();
                botUserConfig.setId(IdUtil.simpleUUID());
                botUserConfig.setUserId(groupId == null ? token : groupId);
                botUserConfig.setEnglishSwitch("1");
                userConfigMapper.insert(botUserConfig);
            }else {
                BotUserConfig botUserConfig = userConfigList.get(0);
                botUserConfig.setEnglishSwitch("1");
                userConfigMapper.updateByPrimaryKey(botUserConfig);
            }
            return new CommonResp("开启成功", ENRespType.TEXT.getType());
        }
        if (reqContent.equals("关闭每日英语")) {
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            if (CollectionUtil.isNotEmpty(userConfigList)) {
                BotUserConfig botUserConfig = userConfigList.get(0);
                botUserConfig.setEnglishSwitch(null);
                userConfigMapper.updateByPrimaryKey(botUserConfig);
            }
            return new CommonResp("关闭成功", ENRespType.TEXT.getType());
        }
        if (reqContent.equals("每日英语")) {
            return new CommonResp(this.getEnglish(), ENRespType.TEXT.getType());
        }
        return null;
    }

    private String getEnglish() {
        String response = HttpUtil.get(url);
        JSONObject data = (JSONObject) JSONUtil.parseObj(response).get("data");
        // 单词
        String word = (String) data.get("word");
        // 释义
        JSONArray translations = (JSONArray) data.get("translations");
        StringBuilder transBuilder = new StringBuilder();
        translations.forEach(x -> {
            JSONObject transObj = (JSONObject) x;
            transBuilder.append(transObj.get("pos")).append(StrUtil.DOT).append(transObj.get("tran_cn")).append(StrUtil.CRLF);
        });
        // 同义词
        JSONArray synonyms = (JSONArray) data.get("synonyms");
        StringBuilder synonymsBuilder = new StringBuilder();
        if (synonyms.size() == 0) {
            synonymsBuilder.append("无").append(StrUtil.CRLF);
        }else {
            for (int index=0; index < synonyms.size(); index++) {
                JSONObject synonymsObj = (JSONObject) synonyms.get(index);
                synonymsBuilder.append(index+1).append(StrUtil.DOT).append((String) synonymsObj.get("pos")).append((String) synonymsObj.get("tran"))
                        .append(StrUtil.CRLF);
                JSONArray hwds = (JSONArray)synonymsObj.get("Hwds");
                hwds.forEach(x -> {
                    JSONObject hwd = (JSONObject) x;
                    synonymsBuilder.append(hwd.get("word")).append(StrUtil.CRLF);
                });
            }
        }
        // 相关词
        JSONArray relWords = (JSONArray) data.get("relWords");
        StringBuilder relWordsBuilder = new StringBuilder();
        if (relWords.size() == 0) {
            relWordsBuilder.append("无").append(StrUtil.CRLF);
        }else {
            for (int index=0; index < relWords.size(); index++) {
                JSONObject relWordsObj = (JSONObject) relWords.get(index);
                relWordsBuilder.append(index+1).append(StrUtil.DOT).append((String) relWordsObj.get("Pos"))
                        .append(StrUtil.CRLF);
                JSONArray hwds = (JSONArray)relWordsObj.get("Hwds");
                hwds.forEach(x -> {
                    JSONObject hwd = (JSONObject) x;
                    relWordsBuilder.append(hwd.get("hwd")).append(StrUtil.CRLF)
                            .append(hwd.get("tran")).append(StrUtil.CRLF);
                });
            }
        }
        // 短语
        JSONArray phrases = (JSONArray) data.get("phrases");
        StringBuilder phrasesBuilder = new StringBuilder();
        if (phrases.size() == 0) {
            phrasesBuilder.append("无").append(StrUtil.CRLF);
        }else {
            for (int index=0; index < phrases.size(); index++) {
                JSONObject phrasesObj = (JSONObject) phrases.get(index);
                phrasesBuilder.append(index+1).append(StrUtil.DOT).append((String) phrasesObj.get("p_content"))
                        .append(StrUtil.CRLF).append((String) phrasesObj.get("p_cn")).append(StrUtil.CRLF);
            }
        }
        // 例句
        JSONArray sentences = (JSONArray) data.get("sentences");
        StringBuilder sentencesBuilder = new StringBuilder();
        if (sentences.size() == 0) {
            sentencesBuilder.append("无").append(StrUtil.CRLF);
        }else {
            for (int index=0; index < sentences.size(); index++) {
                JSONObject phrasesObj = (JSONObject) sentences.get(index);
                sentencesBuilder.append(index+1).append(StrUtil.DOT).append((String) phrasesObj.get("s_content"))
                        .append(StrUtil.CRLF).append((String) phrasesObj.get("s_cn")).append(StrUtil.CRLF);
            }
        }

        return "☆小林每日英语☆" + StrUtil.CRLF +
                "单词：" + word + StrUtil.CRLF +
                "释义：" + StrUtil.CRLF + transBuilder + StrUtil.CRLF+
                "同义词：" + StrUtil.CRLF + synonymsBuilder + StrUtil.CRLF+
                "相关词汇：" + StrUtil.CRLF + relWordsBuilder + StrUtil.CRLF+
                "短语：" + StrUtil.CRLF + phrasesBuilder + StrUtil.CRLF+
                "例句：" + StrUtil.CRLF + sentencesBuilder;
    }

}
