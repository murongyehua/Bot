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
import com.bot.game.dao.mapper.BotBottleMessageMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("bottleMessageServiceImpl")
public class BottleMessageServiceImpl implements BaseService {

    @Resource
    private BotBottleMessageMapper botBottleMessageMapper;

    @Value("${chat.url}")
    private String defaultUrl;

    @Value("${review.chat.key}")
    private String apiKey;

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
                // 查当前用户最近一条记录，防刷屏，1分钟内只能发送一条
                BotBottleMessageExample example = new BotBottleMessageExample();
                example.createCriteria().andUserIdEqualTo(token);
                example.setOrderByClause("send_time desc");
                List<BotBottleMessage> messageList = botBottleMessageMapper.selectByExample(example);
                if (CollectionUtil.isNotEmpty(messageList)) {
                    BotBottleMessage message = messageList.get(0);
                    if (DateUtil.between(DateUtil.parse(message.getSendTime()), new Date(), DateUnit.SECOND) < 60) {
                        return new CommonResp("为防止刷屏，发送漂流瓶有1分钟cd，请稍后再试~", ENRespType.TEXT.getType());
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
                        // 异步
                        ThreadPoolManager.addBaseTask(() -> {
                            for (String userId : SystemConfigCache.bottleUser) {
                                // 推送时过滤掉自己
                                if (!userId.equals(token) && !userId.equals(groupId)) {
                                    if (userId.contains("chatroom")) {
                                        SendMsgUtil.sendGroupMsg(userId, String.format("匿名：\r\n%s", content), null);
                                    }else {
                                        SendMsgUtil.sendMsg(userId, String.format("匿名：\r\n%s", content));
                                    }
                                }
                            }
                        });
                    }
                    return new CommonResp("发送成功", ENRespType.TEXT.getType());
                }else {
                    return new CommonResp(reviewResult, ENRespType.TEXT.getType());
                }
            }
        }
        return null;
    }

    private String review(String content) {
        try {
            String response = HttpSenderUtil.postJsonDataWithToken(defaultUrl,
                    JSONUtil.toJsonStr(new DeepChatReq(new JSONObject(), content, "streaming", null, IdUtil.fastUUID())),
                    apiKey);
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
            e.printStackTrace();
        }
        return "分析失败，请联系管理员检查。";
    }
}
