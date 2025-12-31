package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.DeepChatReq;
import com.bot.base.service.BaseService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import com.bot.game.dao.entity.BotBottleMessage;
import com.bot.game.dao.entity.BotBottleMessageExample;
import com.bot.game.dao.entity.BotGameUserScore;
import com.bot.game.dao.entity.BotGameUserScoreExample;
import com.bot.game.dao.mapper.BotBottleMessageMapper;
import com.bot.game.dao.mapper.BotGameUserScoreMapper;
import com.bot.game.service.SystemConfigHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Service("bottleMessageServiceImpl")
public class BottleMessageServiceImpl implements BaseService {

    @Resource
    private BotBottleMessageMapper botBottleMessageMapper;

    @Value("${chat.url}")
    private String defaultUrl;

    @Value("${review.chat.key}")
    private String apiKey;

    @Value("${name.api.key}")
    private String nameApiKey;

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @Resource
    private BotGameUserScoreMapper gameUserScoreMapper;

    public static String TODAY_ID = null;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (reqContent.startsWith("漂流瓶")) {
            String[] reqs = reqContent.split(StrUtil.SPACE);
            if (reqs.length == 1 && reqContent.equals("漂流瓶")) {
                // 获取漂流瓶
                BotBottleMessageExample example = new BotBottleMessageExample();
                example.createCriteria().andShowTimesLessThan(3);
                List<BotBottleMessage> messageList = botBottleMessageMapper.selectByExample(example);
                if (CollectionUtil.isEmpty(messageList)) {
                    return new CommonResp("漂流瓶里空空如也~", ENRespType.TEXT.getType());
                }
                // 随机发送一条并增加展示次数
                BotBottleMessage message = messageList.get((int) (Math.random() * messageList.size()));
                message.setShowTimes(message.getShowTimes() + 1);
                botBottleMessageMapper.updateByPrimaryKey(message);
                return new CommonResp(String.format("匿名：\r\n%s", message.getContent()), ENRespType.TEXT.getType());
            }
            if (reqs.length >= 2) {
                // 发送漂流瓶
                String content = reqContent.replaceFirst("漂流瓶 ", "");
                if (content.length() > 200) {
                    return new CommonResp("漂流瓶内容不能超过200字哦。", ENRespType.TEXT.getType());
                }
                // 查当前用户最近一条记录，防刷屏，30s内只能发送一条
                BotBottleMessageExample example = new BotBottleMessageExample();
                example.createCriteria().andUserIdEqualTo(token);
                example.setOrderByClause("send_time desc");
                List<BotBottleMessage> messageList = botBottleMessageMapper.selectByExample(example);
                boolean isLastToday = false;
                if (CollectionUtil.isNotEmpty(messageList)) {
                    BotBottleMessage message = messageList.get(0);
                    if (DateUtil.isSameDay(DateUtil.parseDate(message.getSendTime()), new Date())) {
                        isLastToday = true;
                    }
                    if (DateUtil.between(DateUtil.parse(message.getSendTime()), new Date(), DateUnit.SECOND) < 30) {
                        return new CommonResp("为防止刷屏，发送漂流瓶有30秒cd，请稍后再试~", ENRespType.TEXT.getType());
                    }
                }
                // 审核
                String reviewResult = this.review(content);
                // 判断结果
                if ("通过".equals(reviewResult)) {
                    BotBottleMessage message = new BotBottleMessage();
                    message.setContent(content);
                    message.setUserId(token);
                    message.setSendTime(DateUtil.now());
                    message.setShowTimes(0);
                    botBottleMessageMapper.insert(message);
                    // 给开了广播的用户推送
                    if (CollectionUtil.isNotEmpty(SystemConfigCache.bottleUser)) {
                        // 查是不是当天第一条，如果是就获取名字作为当天的匿名名称
                        if (!isLastToday || !SystemConfigCache.userAnonymousName.containsKey(token)) {
                            String name = this.getAnonymousName();
                            if (name == null) {
                                return new CommonResp("获取匿名名称失败，请稍后再试~", ENRespType.TEXT.getType());
                            }
                            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
                            scoreExample.createCriteria().andUserIdEqualTo(token);
                            List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(scoreExample);
                            BotGameUserScore userScore;
                            if (CollectionUtil.isEmpty(scores)) {
                                // 首次使用，初始化用户积分数据
                                userScore = new BotGameUserScore();
                                userScore.setUserId(token);
                                userScore.setNickname(groupId != null ? SendMsgUtil.getGroupNickName(groupId, token) : token);
                                userScore.setScore(0);
                                userScore.setInviteCount(0);
                                userScore.setAnonymous(name);
                                gameUserScoreMapper.insert(userScore);
                            }else {
                                userScore = scores.get(0);
                                userScore.setAnonymous(name);
                                gameUserScoreMapper.updateByPrimaryKey(userScore);
                            }
                            systemConfigHolder.loadAnonymousName();
                        }
                        // 异步
                        ThreadPoolManager.addBaseTask(() -> {
                            for (String userId : SystemConfigCache.bottleUser) {
                                // 推送时过滤掉自己
                                if (!userId.equals(token) && !userId.equals(groupId)) {
                                    if (userId.contains("chatroom")) {
                                        SendMsgUtil.sendGroupMsg(userId, String.format("%s%s：\r\n%s", SystemConfigCache.userAnonymousName.get(token),
                                                SystemConfigCache.userWordMap.get(token) == null ? "" : String.format("『%s』", SystemConfigCache.userWordMap.get(token)),
                                                content), null);
                                    }else {
                                        SendMsgUtil.sendMsg(userId, String.format("%s%s：\r\n%s", SystemConfigCache.userAnonymousName.get(token),
                                                SystemConfigCache.userWordMap.get(token) == null ? "" : String.format("『%s』", SystemConfigCache.userWordMap.get(token)),
                                                content));
                                    }
                                }
                            }
                        });
                    }
                    String extend = "";
                    if (!isLastToday) {
                        extend = "，你今天的昵称是：" + SystemConfigCache.userAnonymousName.get(token);
                    }
                    return new CommonResp(String.format("发送成功%s", extend), ENRespType.TEXT.getType());
                }else {
                    return new CommonResp(reviewResult, ENRespType.TEXT.getType());
                }
            }
        }
        return null;
    }

    private String getAnonymousName() {
        int maxRetries = 5;
        for (int i = 0; i < maxRetries; i++) {
            try {
                log.info("====>" + nameApiKey);
                String response = HttpSenderUtil.postJsonDataWithToken(defaultUrl,
                        JSONUtil.toJsonStr(new DeepChatReq(new JSONObject(), "生成", "blocking", TODAY_ID, IdUtil.fastUUID())),
                        nameApiKey);
                log.info("=====>" + response);
                StringBuilder answer = new StringBuilder();
                JSONObject json = JSONUtil.parseObj(response);
                String answerStr = json.getStr("answer");
                if (StrUtil.isEmpty(answerStr)) {
                    TODAY_ID = null;
                    continue;
                }
                answer = new StringBuilder(answerStr);
                String conversationId = json.getStr("conversation_id");
                if (StrUtil.isEmpty(conversationId)) {
                    TODAY_ID = null;
                }else {
                    TODAY_ID = conversationId;
                }
                String result = UnicodeUtil.toString(answer.toString());
                // 检查是否重复
                if (!SystemConfigCache.userAnonymousName.containsValue(result)) {
                    return result;
                }
            } catch (Exception e) {
                log.error("AI获取昵称失败", e);
            }
        }

        // 如果5次都重复，按失败处理
        return null;

    }

    private String review(String content) {
        try {
            String response = HttpSenderUtil.postJsonDataWithToken(defaultUrl,
                    JSONUtil.toJsonStr(new DeepChatReq(new JSONObject(), content, "streaming", null, IdUtil.fastUUID())),
                    apiKey);
            log.info("AI审核返回消息=====>" + response);
            StringBuilder answer = new StringBuilder();
            String[] datas = response.split("data: ");
            for (String data : datas) {
                if (StrUtil.isNotEmpty(data)) {
                    JSONObject dataObject = JSONUtil.parseObj(data);
                    String event = dataObject.getStr("event");
                    switch (event) {
                        case "agent_message":
                            String answerPart = dataObject.getStr("answer");
                            answer.append(answerPart);
                            break;
                    }
                }
            }
            return UnicodeUtil.toString(answer.toString());
        }catch (Exception e) {
            log.error("AI审核异常", e);
            e.printStackTrace();
        }
        return "分析失败，请联系管理员检查。";
    }
}
