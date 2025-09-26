package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.jx.*;
import com.bot.base.service.BaseService;
import com.bot.base.util.JXAttributeInitUtil;
import com.bot.base.util.JXBattleInitUtil;
import com.bot.base.util.JXTeamCdInitUtil;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.dto.ActivityAwardDTO;
import com.bot.common.enums.ENAwardType;
import com.bot.common.enums.ENChatEngine;
import com.bot.common.enums.ENJXCacheType;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import com.bot.common.util.SendMsgUtil;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("activityServiceImpl")
public class ActivityServiceImpl implements BaseService {

    @Resource
    private BotActivityUserMapper activityUserMapper;

    @Resource
    private BotUserAwardMapper userAwardMapper;

    @Resource
    private BotUserConfigMapper botUserConfigMapper;

    @Resource
    private JXCacheMapper jxCacheMapper;

    @Resource
    private JXShowHistoryQueryMapper showHistoryQueryMapper;

    @Value("${jx3.url}")
    private String jx3Url;

    @Value("${jx.token}")
    private String jx3Token;

    @Value("${jx.battle.template}")
    private String jxBattleTemplate;

    @Value("${jx.teamcd.template}")
    private String jxTeamCdTemplate;

    @Value("${jx.attribute.template}")
    private String jxAttributeTemplate;

    @Value("${jx.help.url}")
    private String jxHelpUrl;

    private static final String PIC_URL = "http://113.45.63.97/file/picCache/";

    private static final String SHOWS_URL = "https://ai.ialy.top/file/template/showHistory.html?id=";

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        BotUserConfigExample example = new BotUserConfigExample();
        // 这里只按群查，但如果不是群就把token赋给群
        // 主要是抽奖功能还是按个人来的，暂时还不想弃用，就都保留了做兼容
        if (groupId == null) {
            groupId = token;
        }
        example.createCriteria().andUserIdEqualTo(groupId);
        List<BotUserConfig> botUserConfigs = botUserConfigMapper.selectByExample(example);
        String serverName = "";
        if (CollectionUtil.isNotEmpty(botUserConfigs)) {
            serverName = botUserConfigs.get(0).getJxServer();
        }
        String[] reqs =  reqContent.split(StrUtil.SPACE);
        // 菜单
        if (reqs.length == 2 && (reqs[1].equals("菜单") || reqs[1].equals("帮助"))) {
            return new CommonResp(jxHelpUrl, ENRespType.IMG.getType());
        }
        // 绑定服务器
        if (reqs.length == 3 && reqs[1].equals(BaseConsts.Activity.BIND_SERVER)) {
            return new CommonResp(this.bindServer(groupId, reqs[2], botUserConfigs), ENRespType.TEXT.getType());
        }
        if (StrUtil.isEmpty(serverName)) {
            return new CommonResp("请先绑定服务器，格式为【剑三+空格+绑定区服+空格+区服名称】，如“剑三 绑定区服 天鹅坪”", ENRespType.TEXT.getType());
        }
        if (reqs.length == 2 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_GET_AWARD)) {
            return new CommonResp(this.getAward(token), ENRespType.TEXT.getType());
        }
        if (reqs.length == 2 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_MY_AWARD)) {
            return new CommonResp(this.fetchMyAward(token), ENRespType.TEXT.getType());
        }
        if (reqs.length == 2 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_ALL_AWARD)) {
            return new CommonResp(this.allAwardInfo(), ENRespType.TEXT.getType());
        }
        // 绑定抽奖id
        if (reqs.length == 3 && reqs[1].equals(BaseConsts.Activity.ACTIVITY_BIND)) {
            return new CommonResp(this.bindAccount(token, reqs[2]), ENRespType.TEXT.getType());
        }
        // 活动日历
        if (reqs.length == 2 && (reqs[1].equals(BaseConsts.Activity.TODAY_DAILY)
                || reqs[1].equals(BaseConsts.Activity.TOMORROW_DAILY)
                || reqs[1].equals(BaseConsts.Activity.TOMORROW_TOMORROW_DAILY))) {
            return new CommonResp(this.dailyQuery(reqs[1], serverName), ENRespType.TEXT.getType());
        }
        // 开服情况
        if ((reqs.length == 2 || reqs.length == 3) && reqs[1].equals(BaseConsts.Activity.OPEN_SERVER)) {
            if (reqs.length == 3) {
                // 查非绑定区服
                return new CommonResp(this.openServer(reqs[2]), ENRespType.TEXT.getType());
            }
            return new CommonResp(this.openServer(serverName), ENRespType.TEXT.getType());
        }
        // QQ秀
        if ((reqs.length == 3 || reqs.length == 4) && BaseConsts.Activity.QQ_SHOW.contains(reqs[1])) {
            if (reqs.length == 4) {
                return this.show(reqs[2], reqs[3]);
            }
            return this.show(serverName, reqs[2]);
        }
        // 随机秀
        if ((reqs.length == 2 || reqs.length == 3 || reqs.length == 4) && BaseConsts.Activity.RANDOM_SHOW.equals(reqs[1])) {
            // 剑三 随机秀 体型 门派
            if (reqs.length == 2) {
                return this.randomShow(null, null, token, groupId);
            }
            if (reqs.length == 3) {
                return this.randomShow(reqs[2], null, token, groupId);
            }
            return this.randomShow(reqs[2], reqs[3], token, groupId);
        }
        // 全部名片
        if ((reqs.length == 3 || reqs.length == 4) && BaseConsts.Activity.ALL_SHOWS.contains(reqs[1])) {
            if (groupId != null) {
                return new CommonResp("很抱歉！查全部名片功能暂时下线了...", ENRespType.TEXT.getType());
            }
            if (reqs.length == 4) {
                return this.allShow(reqs[2], reqs[3]);
            }
            return this.allShow(serverName, reqs[2]);
        }
        // 金价
        if ((reqs.length == 2 || reqs.length == 3) && BaseConsts.Activity.MONEY.equals(reqs[1])) {
            if (reqs.length == 3) {
                return new CommonResp(this.money(reqs[2]), ENRespType.TEXT.getType());
            }
            return new CommonResp(this.money(serverName), ENRespType.TEXT.getType());
        }
        // 战绩
        if ((reqs.length == 3 || reqs.length == 4) && BaseConsts.Activity.BATTLE.equals(reqs[1])) {
            if (reqs.length == 4) {
                return this.arena(reqs[2], reqs[3]);
            }
            return this.arena(serverName, reqs[2]);
        }
        // 副本记录
        if ((reqs.length == 3 || reqs.length == 4) && BaseConsts.Activity.TEAM_CD.equals(reqs[1])) {
            if (reqs.length == 4) {
                return this.teamCd(reqs[2], reqs[3]);
            }
            return this.teamCd(serverName, reqs[2]);
        }
        // 装备属性
        if ((reqs.length == 3 || reqs.length == 4) && BaseConsts.Activity.ATTRIBUTE.equals(reqs[1])) {
            if (reqs.length == 4) {
                return this.attribute(reqs[2], reqs[3]);
            }
            return this.attribute(serverName, reqs[2]);
        }
        // 资讯
        if (reqs.length == 2 && BaseConsts.Activity.NEWS.equals(reqs[1])) {
            return new CommonResp(this.news(ENJXCacheType.NEWS, "/news/allnews"), ENRespType.TEXT.getType());
        }
        // 公告
        if (reqs.length == 2 && BaseConsts.Activity.NOTICE.equals(reqs[1])) {
            return new CommonResp(this.news(ENJXCacheType.NOTICE, "/news/announce"), ENRespType.TEXT.getType());
        }
        return new CommonResp(BaseConsts.Activity.UN_KNOW, ENRespType.TEXT.getType());
    }

    private CommonResp allShow(String serverName, String name) {
        JXCacheExample jxCacheExample = new JXCacheExample();
        String cacheKey = String.format("%s-%s", serverName, name);
        jxCacheExample.createCriteria().andCacheKeyEqualTo(cacheKey).andCacheTypeEqualTo(ENJXCacheType.SHOW.getValue());
        List<JXCache> cacheList = jxCacheMapper.selectByExample(jxCacheExample);
        if (CollectionUtil.isEmpty(cacheList)) {
            return new CommonResp("你还没有用小林获取过名片，无法查询全部名片呢~", ENRespType.TEXT.getType());
        }

        // 去重
        List<String> urls = new ArrayList<>();
        for (JXCache jxCache : cacheList) {
            if (urls.contains(jxCache.getContent())) {
                continue;
            }
            urls.add(jxCache.getContent());
        }

        // 生成查询
        String queryId = IdUtil.simpleUUID();
        String now = DateUtil.now();
        for (String show : urls) {
            JXShowHistoryQuery showHistoryQuery = new JXShowHistoryQuery();
            showHistoryQuery.setQueryId(queryId);
            showHistoryQuery.setShowUrl(show);
            showHistoryQuery.setQueryDate(now);
            showHistoryQuery.setId(IdUtil.simpleUUID());
            showHistoryQuery.setTitle(name + "的名片秀");
            showHistoryQueryMapper.insert(showHistoryQuery);
        }
        return new CommonResp("全部名片整理好啦，请点击链接查看或保存：\r\n" + SHOWS_URL + queryId, ENRespType.TEXT.getType());
    }

    private CommonResp attribute(String serverName, String name) {
        try {
            // 先查有没有缓存
            JXCacheExample jxCacheExample = new JXCacheExample();
            String cacheKey = String.format("%s-%s", serverName, name);
            jxCacheExample.createCriteria().andCacheTypeEqualTo(ENJXCacheType.ATTRIBUTE.getValue()).andCacheKeyEqualTo(cacheKey);
            jxCacheExample.setOrderByClause("save_date desc");
            List<JXCache> cacheList = jxCacheMapper.selectByExample(jxCacheExample);
            if (CollectionUtil.isNotEmpty(cacheList)) {
                JXCache jxCache = cacheList.get(0);
                // 没超过30min，直接返回
                if (DateUtil.offsetMinute(new Date(), -30).before(DateUtil.parse(jxCache.getSaveDate()))) {
                    return new CommonResp(jxCache.getContent(), ENRespType.IMG.getType());
                }
            }
            String response = HttpSenderUtil.postJsonData(jx3Url + "/role/attribute", JSONUtil.toJsonStr(new BattleReq(serverName, name, SystemConfigCache.tuilanToken, jx3Token)));
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer) respObj.get("code");
            if (code != 200) {
                log.error("获取角色属性失败，返回信息：{}", response);
                return new CommonResp((String) respObj.get("msg"), ENRespType.TEXT.getType());
            }
            String jsonData = JSONUtil.toJsonStr(respObj.get("data"));
            // 再去查名片 取数据库的 没有就空着
            jxCacheExample.clear();
            jxCacheExample.createCriteria().andCacheTypeEqualTo(ENJXCacheType.SHOW.getValue()).andCacheKeyEqualTo(cacheKey);
            jxCacheExample.setOrderByClause("save_date desc");
            List<JXCache> showCacheList = jxCacheMapper.selectByExample(jxCacheExample);
            String showUrl = null;
            if (CollectionUtil.isNotEmpty(showCacheList)) {
                showUrl = showCacheList.get(0).getContent();
            }
            String fileName = JXAttributeInitUtil.init(jxAttributeTemplate, jsonData, showUrl);
            if (fileName == null) {
                return new CommonResp("生成图片失败，请联系管理员检查", ENRespType.TEXT.getType());
            }
            String fileUrl = PIC_URL + fileName;
            JXCache jxCache = new JXCache();
            jxCache.setId(IdUtil.simpleUUID());
            jxCache.setCacheType(ENJXCacheType.ATTRIBUTE.getValue());
            jxCache.setCacheKey(cacheKey);
            jxCache.setSaveDate(DateUtil.now());
            jxCache.setContent(fileUrl);
            jxCacheMapper.insert(jxCache);
            return new CommonResp(fileUrl, ENRespType.IMG.getType());
        }catch (Exception e) {
            e.printStackTrace();
            log.error("处理失败", e);
            return new CommonResp("获取失败，请联系管理员检查", ENRespType.TEXT.getType());
        }
    }

    private CommonResp teamCd(String serverName, String name) {
        try {
            // 先查有没有缓存
            JXCacheExample jxCacheExample = new JXCacheExample();
            String cacheKey = String.format("%s-%s", serverName, name);
            jxCacheExample.createCriteria().andCacheTypeEqualTo(ENJXCacheType.TEAM_CD.getValue()).andCacheKeyEqualTo(cacheKey);
            jxCacheExample.setOrderByClause("save_date desc");
            List<JXCache> cacheList = jxCacheMapper.selectByExample(jxCacheExample);
            if (CollectionUtil.isNotEmpty(cacheList)) {
                JXCache jxCache = cacheList.get(0);
                // 没超过30min，直接返回
                if (DateUtil.offsetMinute(new Date(), -30).before(DateUtil.parse(jxCache.getSaveDate()))) {
                    return new CommonResp(jxCache.getContent(), ENRespType.IMG.getType());
                }
            }
            String response = HttpSenderUtil.postJsonData(jx3Url + "/role/teamCdList", JSONUtil.toJsonStr(new BattleReq(serverName, name, SystemConfigCache.tuilanToken, jx3Token)));
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer) respObj.get("code");
            if (code != 200) {
                log.error("获取副本记录失败，返回信息：{}", response);
                return new CommonResp((String) respObj.get("msg"), ENRespType.TEXT.getType());
            }
            String jsonData = JSONUtil.toJsonStr(respObj.get("data"));
            String fileName = JXTeamCdInitUtil.init(jxTeamCdTemplate, jsonData);
            if (fileName == null) {
                return new CommonResp("生成图片失败，请联系管理员检查", ENRespType.TEXT.getType());
            }
            String fileUrl = PIC_URL + fileName;
            JXCache jxCache = new JXCache();
            jxCache.setId(IdUtil.simpleUUID());
            jxCache.setCacheType(ENJXCacheType.TEAM_CD.getValue());
            jxCache.setCacheKey(cacheKey);
            jxCache.setSaveDate(DateUtil.now());
            jxCache.setContent(fileUrl);
            jxCacheMapper.insert(jxCache);
            return new CommonResp(fileUrl, ENRespType.IMG.getType());
        }catch (Exception e) {
            e.printStackTrace();
            log.error("处理失败", e);
            return new CommonResp("获取失败，请联系管理员检查", ENRespType.TEXT.getType());
        }
    }

    private CommonResp arena(String serverName, String name) {
        try {
            // 先查有没有缓存
            JXCacheExample jxCacheExample = new JXCacheExample();
            String cacheKey = String.format("%s-%s", serverName, name);
            jxCacheExample.createCriteria().andCacheTypeEqualTo(ENJXCacheType.BATTLE.getValue()).andCacheKeyEqualTo(cacheKey);
            jxCacheExample.setOrderByClause("save_date desc");
            List<JXCache> cacheList = jxCacheMapper.selectByExample(jxCacheExample);
            if (CollectionUtil.isNotEmpty(cacheList)) {
                JXCache jxCache = cacheList.get(0);
                // 没超过1h，直接返回
                if (DateUtil.offsetHour(new Date(), -1).before(DateUtil.parse(jxCache.getSaveDate()))) {
                    return new CommonResp(jxCache.getContent(), ENRespType.IMG.getType());
                }
            }
            String response = HttpSenderUtil.postJsonData(jx3Url + "/arena/recent", JSONUtil.toJsonStr(new BattleReq(serverName, name, SystemConfigCache.tuilanToken, jx3Token)));
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer) respObj.get("code");
            if (code != 200) {
                log.error("获取战绩失败，返回信息：{}", response);
                return new CommonResp((String) respObj.get("msg"), ENRespType.TEXT.getType());
            }
            String jsonData = JSONUtil.toJsonStr(respObj.get("data"));
            String fileName = JXBattleInitUtil.init(jxBattleTemplate, jsonData);
            if (fileName == null) {
                return new CommonResp("生成图片失败，请联系管理员检查", ENRespType.TEXT.getType());
            }
            String fileUrl = PIC_URL + fileName;
            JXCache jxCache = new JXCache();
            jxCache.setId(IdUtil.simpleUUID());
            jxCache.setCacheType(ENJXCacheType.BATTLE.getValue());
            jxCache.setCacheKey(cacheKey);
            jxCache.setSaveDate(DateUtil.now());
            jxCache.setContent(fileUrl);
            jxCacheMapper.insert(jxCache);
            return new CommonResp(fileUrl, ENRespType.IMG.getType());
        }catch (Exception e) {
            e.printStackTrace();
            log.error("处理失败", e);
            return new CommonResp("获取失败，请联系管理员检查", ENRespType.TEXT.getType());
        }
    }

    private String news(ENJXCacheType enjxCacheType, String url) {
        try {
            // 先查有没有缓存
            JXCacheExample jxCacheExample = new JXCacheExample();
            jxCacheExample.createCriteria().andCacheTypeEqualTo(enjxCacheType.getValue());
            jxCacheExample.setOrderByClause("save_date desc");
            List<JXCache> cacheList = jxCacheMapper.selectByExample(jxCacheExample);
            if (CollectionUtil.isNotEmpty(cacheList)) {
                JXCache jxCache = cacheList.get(0);
                // 没超过2h，直接返回
                if (DateUtil.offsetHour(new Date(), -2).before(DateUtil.parse(jxCache.getSaveDate()))) {
                    return jxCache.getContent();
                }
            }
            String response = HttpSenderUtil.postJsonData(jx3Url + "/news/allnews", JSONUtil.toJsonStr(new NewsReq(1)));
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer) respObj.get("code");
            if (code != 200) {
                log.error("获取资讯失败，返回信息：{}", response);
                return (String) respObj.get("msg");
            }
            JSONArray dataArr = (JSONArray) respObj.get("data");
            JSONObject data = JSONUtil.parseObj(dataArr.get(0));
            String result = String.format("%s\r\n标题：%s\r\n链接：\r\n%s", data.get("class"), data.get("title"), data.get("url"));
            JXCache jxCache = new JXCache();
            jxCache.setId(IdUtil.simpleUUID());
            jxCache.setCacheType(enjxCacheType.getValue());
            jxCache.setCacheKey(enjxCacheType.getValue());
            jxCache.setSaveDate(DateUtil.now());
            jxCache.setContent(result);
            jxCacheMapper.insert(jxCache);
            return result;
        }catch (Exception e) {
            e.printStackTrace();
            log.error("处理失败", e);
            return "获取失败，请联系管理员检查";
        }
    }



    private String money(String serverName) {
        try {
            // 先查有没有缓存
            JXCacheExample jxCacheExample = new JXCacheExample();
            jxCacheExample.createCriteria().andCacheKeyEqualTo(serverName).andCacheTypeEqualTo(ENJXCacheType.MONEY.getValue());
            jxCacheExample.setOrderByClause("save_date desc");
            List<JXCache> cacheList = jxCacheMapper.selectByExample(jxCacheExample);
            if (CollectionUtil.isNotEmpty(cacheList)) {
                JXCache jxCache = cacheList.get(0);
                // 没超过12h，直接返回
                if (DateUtil.offsetHour(new Date(), -12).before(DateUtil.parse(jxCache.getSaveDate()))) {
                    return jxCache.getContent();
                }
            }
            String response = HttpSenderUtil.postJsonData(jx3Url + "/trade/demon", JSONUtil.toJsonStr(new MoneyReq(serverName, 1, jx3Token)));
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer) respObj.get("code");
            if (code != 200) {
                log.error("获取金价失败，返回信息：{}", response);
                return (String) respObj.get("msg");
            }
            // 解析
            JSONArray dataArr = (JSONArray) respObj.get("data");
            JSONObject data = JSONUtil.parseObj(dataArr.get(0));
            String now = DateUtil.now();
            String result = String.format(BaseConsts.Activity.MONEY_PRICE, now,
                    serverName, data.get("wanbaolou"), data.get("tieba"), data.get("dd373"), data.get("uu898"));
            JXCache jxCache = new JXCache();
            jxCache.setId(IdUtil.simpleUUID());
            jxCache.setCacheType(ENJXCacheType.MONEY.getValue());
            jxCache.setCacheKey(serverName);
            jxCache.setSaveDate(now);
            jxCache.setContent(result);
            jxCacheMapper.insert(jxCache);
            return result;
        }catch (Exception e) {
            e.printStackTrace();
            log.error("处理失败", e);
            return "获取失败，请联系管理员检查";
        }
    }

    private CommonResp show(String serverName, String name) {
        try {
            // 先查有没有缓存
            String cacheKey = String.format("%s-%s", serverName, name);
            JXCacheExample jxCacheExample = new JXCacheExample();
            jxCacheExample.createCriteria().andCacheKeyEqualTo(cacheKey).andCacheTypeEqualTo(ENJXCacheType.SHOW.getValue());
            jxCacheExample.setOrderByClause("save_date desc");
            List<JXCache> cacheList = jxCacheMapper.selectByExample(jxCacheExample);
            if (CollectionUtil.isNotEmpty(cacheList)) {
                JXCache jxCache = cacheList.get(0);
                // 没超过10min，直接返回
                if (DateUtil.offsetMinute(new Date(), -10).before(DateUtil.parse(jxCache.getSaveDate()))) {
                    return new CommonResp(jxCache.getContent(), ENRespType.IMG.getType());
                }
            }
            String response = HttpSenderUtil.postJsonData(jx3Url + "/show/card", JSONUtil.toJsonStr(new ShowReq(serverName, name, jx3Token)));
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer) respObj.get("code");
            if (code != 200) {
                log.error("获取qq秀失败，返回信息：{}", response);
                return new CommonResp((String) respObj.get("msg"), ENRespType.TEXT.getType());
            }
            JSONObject data = JSONUtil.parseObj(respObj.get("data"));
            String showAvatar= (String) data.get("showAvatar");
            // 存缓存
            JXCache jxCache = new JXCache();
            jxCache.setId(IdUtil.simpleUUID());
            jxCache.setCacheKey(cacheKey);
            jxCache.setContent(showAvatar);
            jxCache.setSaveDate(DateUtil.now());
            jxCache.setCacheType(ENJXCacheType.SHOW.getValue());
            jxCacheMapper.insert(jxCache);
            return new CommonResp(showAvatar, ENRespType.IMG.getType());
        }catch (Exception e) {
            e.printStackTrace();
            log.error("处理失败", e);
            return new CommonResp("获取失败，请联系管理员检查", ENRespType.TEXT.getType());
        }
    }

    private CommonResp randomShow(String body, String force, String token, String groupId) {
        try {
            String response = HttpSenderUtil.postJsonData(jx3Url + "/show/random", JSONUtil.toJsonStr(new RandomShowReq(body, force, jx3Token)));
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer) respObj.get("code");
            if (code != 200) {
                log.error("获取qq秀失败，返回信息：{}", response);
                return new CommonResp((String) respObj.get("msg"), ENRespType.TEXT.getType());
            }
            JSONObject data = JSONUtil.parseObj(respObj.get("data"));
            String showAvatar= (String) data.get("showAvatar");
            String serverName = (String) data.get("serverName");
            String name = (String) data.get("roleName");
            // 存缓存
            JXCache jxCache = new JXCache();
            jxCache.setId(IdUtil.simpleUUID());
            String cacheKey = String.format("%s-%s", serverName, name);
            jxCache.setCacheKey(cacheKey);
            jxCache.setContent(showAvatar);
            jxCache.setSaveDate(DateUtil.now());
            jxCache.setCacheType(ENJXCacheType.SHOW.getValue());
            jxCacheMapper.insert(jxCache);
            // 发送提示
            String tip = "随机到【%s】的【%s】\r\n请查收~";
            SendMsgUtil.sendGroupMsg(groupId, String.format(tip, serverName, name), token);
            return new CommonResp(showAvatar, ENRespType.IMG.getType());
        }catch (Exception e) {
            e.printStackTrace();
            log.error("处理失败", e);
            return new CommonResp("获取失败，请联系管理员检查", ENRespType.TEXT.getType());
        }
    }

    private String openServer(String serverName) {
        try {
            JXCacheExample jxCacheExample = new JXCacheExample();
            jxCacheExample.createCriteria().andCacheKeyEqualTo(serverName).andCacheTypeEqualTo(ENJXCacheType.OPEN_SERVER.getValue());
            jxCacheExample.setOrderByClause("save_date desc");
            List<JXCache> cacheList = jxCacheMapper.selectByExample(jxCacheExample);
            if (CollectionUtil.isNotEmpty(cacheList)) {
                JXCache jxCache = cacheList.get(0);
                // 没超过2min，直接返回
                if (DateUtil.offsetMinute(new Date(), -2).before(DateUtil.parse(jxCache.getSaveDate()))) {
                    return jxCache.getContent();
                }
            }
            String response = HttpSenderUtil.postJsonData(jx3Url + "/server/check", JSONUtil.toJsonStr(new OpenReq(serverName)));
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer) respObj.get("code");
            if (code != 200) {
                log.error("获取开服状态失败，返回信息：{}", response);
                return "获取失败，请联系管理员检查";
            }
            // 解析
            JSONObject data = JSONUtil.parseObj(respObj.get("data"));
            String zone = (String) data.get("zone");
            String server = (String) data.get("server");
            int status = (Integer) data.get("status");
//            int time = (Integer) data.get("time");
            String result =  String.format("【%s】%s,%s", zone, server, status == 1? "已开服" : "维护中");
            // 存缓存
            JXCache jxCache = new JXCache();
            jxCache.setId(IdUtil.simpleUUID());
            jxCache.setCacheKey(serverName);
            jxCache.setContent(result);
            jxCache.setSaveDate(DateUtil.now());
            jxCache.setCacheType(ENJXCacheType.OPEN_SERVER.getValue());
            jxCacheMapper.insert(jxCache);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("处理失败", e);
            return "获取失败，请联系管理员检查";
        }
    }

    private String bindServer(String token, String serverName, List<BotUserConfig> botUserConfigs) {
        if (CollectionUtil.isEmpty(botUserConfigs)) {
            BotUserConfig botUserConfig = new BotUserConfig();
            botUserConfig.setId(IdUtil.simpleUUID());
            botUserConfig.setUserId(token);
            botUserConfig.setChatEngine(ENChatEngine.DEFAULT.getValue());
            botUserConfig.setJxServer(serverName);
            botUserConfigMapper.insert(botUserConfig);
            return "绑定成功。";
        }
        BotUserConfig botUserConfig = botUserConfigs.get(0);
        botUserConfig.setJxServer(serverName);
        botUserConfigMapper.updateByPrimaryKey(botUserConfig);
        return "绑定成功。";
    }

    private String dailyQuery(String content, String serverName) {
        try {
            String response = null;
            if (content.equals(BaseConsts.Activity.TODAY_DAILY)) {
                response = HttpSenderUtil.postJsonData(jx3Url + "/active/calendar", JSONUtil.toJsonStr(new DailyReq(serverName, 0)));
            }else if (content.equals(BaseConsts.Activity.TOMORROW_DAILY)) {
                response = HttpSenderUtil.postJsonData(jx3Url + "/active/calendar", JSONUtil.toJsonStr(new DailyReq(serverName, 1)));
            }else if (content.equals(BaseConsts.Activity.TOMORROW_TOMORROW_DAILY)) {
                response = HttpSenderUtil.postJsonData(jx3Url + "/active/calendar", JSONUtil.toJsonStr(new DailyReq(serverName, 2)));
            }else {
                return "指令错误，暂只支持查3日内的日常。";
            }
            JSONObject respObj = JSONUtil.parseObj(response);
            int code = (Integer)respObj.get("code");
            if (code != 200) {
                log.error("获取日常失败，返回信息：{}", response);
                return "获取失败，请联系管理员检查";
            }
            // 解析
            JSONObject data = JSONUtil.parseObj(respObj.get("data"));
            String date = (String) data.get("date");
            String war = (String) data.get("war");
            String battle = (String) data.get("battle");
            String orecar = (String) data.get("orecar");
            String school = (String) data.get("school");
            String rescue = (String) data.get("rescue");
            JSONArray lucks = (JSONArray) data.get("luck");
            JSONArray teams = (JSONArray) data.get("team");
            String draw = (String) data.get("draw");
            if (draw != null) {
                return String.format(BaseConsts.Activity.DAILY_RETURN_FORMAT, content, date, war, battle, orecar, school, rescue, draw, lucks.get(0), lucks.get(1), lucks.get(2),
                        teams.get(0), teams.get(1), teams.get(2));
            }
            return String.format(BaseConsts.Activity.DAILY_RETURN_FORMAT_WITHOUT_DRAW, content, date, war, battle, orecar, school, rescue, lucks.get(0), lucks.get(1), lucks.get(2),
                    teams.get(0), teams.get(1), teams.get(2));
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return "获取失败，请联系管理员检查";
        }
    }

    private String bindAccount(String token, String account) {
        BotActivityUserExample example = new BotActivityUserExample();
        example.createCriteria().andIdEqualTo(token);
        List<BotActivityUser> userList = activityUserMapper.selectByExample(example);
        if (CollectionUtil.isNotEmpty(userList)) {
            BotActivityUser botActivityUser = userList.get(0);
            botActivityUser.setBindId(account);
            activityUserMapper.updateByExampleSelective(botActivityUser, example);
        }else {
            BotActivityUser botActivityUser = new BotActivityUser();
            botActivityUser.setId(token);
            botActivityUser.setBindId(account);
            activityUserMapper.insert(botActivityUser);
        }
        return "绑定成功。";
    }

    private synchronized String getAward(String token) {
        if (CollectionUtil.isEmpty(SystemConfigCache.activityAwardList)) {
            return "当前没有开启的抽奖活动";
        }
        // 是否绑了游戏id
        BotActivityUserExample activityUserExample = new BotActivityUserExample();
        activityUserExample.createCriteria().andIdEqualTo(token);
        if (CollectionUtil.isEmpty(activityUserMapper.selectByExample(activityUserExample))) {
            return "请先绑定游戏id";
        }
        // 是否参与过
        BotUserAwardExample userAwardExample = new BotUserAwardExample();
        userAwardExample.createCriteria()
                .andActivityIdEqualTo(SystemConfigCache.activityAwardList.get(0).getActivityId())
                .andUserIdEqualTo(token);
        List<BotUserAward> userAwardList = userAwardMapper.selectByExample(userAwardExample);
        if (userAwardList.size() >= 1) {
            return BaseConsts.Activity.REPEAT;
        }
        // 抽奖
        ActivityAwardDTO baoDiDTO = null;
        ActivityAwardDTO awardDTO = null;
        for (ActivityAwardDTO activityAwardDTO :  SystemConfigCache.activityAwardList) {
            if (ENAwardType.DI_BAO.getValue().equals(activityAwardDTO.getType())) {
                baoDiDTO = activityAwardDTO;
            }else {
                int x = RandomUtil.randomInt(0, 100);
                if (activityAwardDTO.getNumber() > 0 && x < Integer.parseInt(activityAwardDTO.getPercent())) {
                    awardDTO = activityAwardDTO;
                    activityAwardDTO.setNumber(activityAwardDTO.getNumber() - 1);
                    break;
                }
            }
        }
        if (awardDTO == null && baoDiDTO != null) {
            awardDTO = baoDiDTO;
        }
        if (awardDTO == null) {
            return "很遗憾，未中奖";
        }else {
            // 保存
            BotUserAward botUserAward = new BotUserAward();
            botUserAward.setId(IdUtil.simpleUUID());
            botUserAward.setAwardName(String.format("【%s】%s", awardDTO.getPrefix(), awardDTO.getAwardName()));
            botUserAward.setUserId(token);
            botUserAward.setActivityId(awardDTO.getActivityId());
            botUserAward.setActivityAwardId(awardDTO.getId());
            userAwardMapper.insert(botUserAward);
            return String.format("恭喜您抽到了【%s】%s！", awardDTO.getPrefix(), awardDTO.getAwardName());
        }
    }

    private String fetchMyAward(String token) {
        BotUserAwardExample userAwardExample = new BotUserAwardExample();
        userAwardExample.createCriteria().andUserIdEqualTo(token);
        List<BotUserAward> userAwardList = userAwardMapper.selectByExample(userAwardExample);
        if (CollectionUtil.isEmpty(userAwardList)) {
            return "您当前无中奖信息。";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("您当前已中奖项如下：").append(StrUtil.CRLF);
        userAwardList.forEach(x -> {
            stringBuilder.append(x.getAwardName()).append(StrUtil.CRLF);
        });
        return stringBuilder.toString();
    }

    private String allAwardInfo() {
        List<BotUserAward> userAwardList = userAwardMapper.selectByExample(new BotUserAwardExample());
        if (CollectionUtil.isEmpty(userAwardList)) {
            return "无中奖信息。";
        }
        Map<String, String> tokenAccountMap = activityUserMapper.selectByExample(new BotActivityUserExample()).stream().collect(Collectors.toMap(BotActivityUser::getId, BotActivityUser::getBindId));
        Map<String, List<BotUserAward>> groupAwardMap = userAwardList.stream().collect(Collectors.groupingBy(BotUserAward::getUserId));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("中奖汇总：").append(StrUtil.CRLF);
        for (String token : groupAwardMap.keySet()) {
            stringBuilder.append("游戏id：").append(tokenAccountMap.get(token))
                    .append(StrUtil.CRLF).append("奖品：").append(StrUtil.CRLF);
            groupAwardMap.get(token).forEach(x -> {
                stringBuilder.append(x.getAwardName()).append(StrUtil.CRLF);
            });
        }
        return stringBuilder.toString();
    }

}
