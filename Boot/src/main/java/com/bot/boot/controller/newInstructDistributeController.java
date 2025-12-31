package com.bot.boot.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.dto.jx.ShowHistoryResp;
import com.bot.base.service.Distributor;
import com.bot.base.service.EmojiDistributor;
import com.bot.base.service.PictureDistributor;
import com.bot.base.service.SystemManager;
import com.bot.base.service.impl.JXShowHistoryManager;
import com.bot.base.service.impl.QQDealDistributor;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.dto.qqsender.QQGroupMessage;
import com.bot.common.enums.ENFileType;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.*;
import com.bot.game.service.SystemConfigHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/newInstruct")
@Slf4j
public class newInstructDistributeController {

    @Resource
    private Distributor distributor;

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @Resource
    private PictureDistributor pictureDistributor;

    @Resource
    private JXShowHistoryManager showHistoryManager;

    @Resource
    private QQDealDistributor qqDealDistributor;

    @Resource
    private EmojiDistributor emojiDistributor;


    /**
     * 消息体id，判重用
     */
    private static List<Long> msgIdList = new ArrayList<>();

    @PostMapping("/chatListener")
    public void weChatDeal(@RequestBody JSONObject message) {
        String messageType = String.valueOf(message.get("messageType"));
        JSONObject data = (JSONObject) message.get("data");
        String userId = (String) data.get("fromUser");
        String msg = (String) data.get("content");
        Long msgId = data.getLong("msgId");
        if (ObjectUtil.notEqual("60001", messageType)
                && ObjectUtil.notEqual("80001", messageType)
                && ObjectUtil.notEqual("60002", messageType)
                && ObjectUtil.notEqual("80002", messageType)
                && ObjectUtil.notEqual("85008", messageType)
                && ObjectUtil.notEqual("85009", messageType)
                && ObjectUtil.notEqual("60006", messageType)
                && ObjectUtil.notEqual("80006", messageType)
                && ObjectUtil.notEqual("60022", messageType)
                && ObjectUtil.notEqual("60010", messageType)) {
            return;
        }
        if (msgIdList.contains(msgId)) {
            return;
        }
        msgIdList.add(msgId);
        // 私聊文字
        if (StrUtil.equals("60001", messageType)) {
            log.info(String.format("收到来自[%s]的私聊消息：[%s]", userId, msg));
            if (msg.contains(ENFileType.GAME_FILE.getLabel())) {
                SendMsgUtil.sendMsg(userId, "游戏pc端已停止维护，不再提供下载，请使用微信游玩。");
                return;
            }
            CommonResp resp = distributor.doDistributeWithString(msg.trim(), userId, null, false, true, "wx", msg.trim());
            if (resp != null && ENRespType.IMG.getType().equals(resp.getType())) {
                SendMsgUtil.sendImg(userId, resp.getMsg());
            } else if (resp != null && ENRespType.VIDEO.getType().equals(resp.getType())) {
                // 要发视频的时候先提醒耐心等待 再异步慢慢发
                SendMsgUtil.sendMsg(userId, BaseConsts.GirlVideo.SUCCESS);
                ThreadPoolManager.addBaseTask(() -> SendMsgUtil.sendVideo(userId, resp.getMsg()));
            } else if (resp != null && ENRespType.FILE.getType().equals(resp.getType())) {
                SendMsgUtil.sendFile(userId, resp.getMsg());
            } else if (resp != null && ENRespType.AUDIO.getType().equals(resp.getType())) {
                SendMsgUtil.sendAudio(userId, resp.getMsg());
            } else if (resp != null) {
                SendMsgUtil.sendMsg(userId, resp.getMsg());
            }
        }
        // 群聊文字
        if (StrUtil.equals("80001", messageType)) {
//            if (msg.startsWith("@小林Bot")) {
            log.info(String.format("消息体：%s", message));
            log.info(String.format("收到群消息: %s", msg));
            String groupId = (String) data.get("fromGroup");
            String effectMsg = msg;
            boolean at = false;
            if (msg.contains("@小林Bot")) {
                at = true;
                effectMsg = msg.replaceAll("@小林Bot", "").replaceAll("\u2005", "").trim();
            }
            if (effectMsg.contains(ENFileType.GAME_FILE.getLabel())) {
                SendMsgUtil.sendGroupMsg(groupId, "游戏pc端已停止维护，不再提供下载，请使用微信游玩。", userId);
                return;
            }
            String nickName = SendMsgUtil.getGroupNickName(groupId, userId);
            String withoutPexContent = effectMsg;
            effectMsg = String.format("%s在群里说：" + effectMsg, nickName);
            CommonResp resp = distributor.doDistributeWithString(effectMsg, userId, groupId, at, at, "wx", withoutPexContent);
            if (resp !=null && ENRespType.IMG.getType().equals(resp.getType()) && StrUtil.isNotEmpty(resp.getMsg())) {
                SendMsgUtil.sendImg(groupId, resp.getMsg());
            } else if (resp != null && ENRespType.VIDEO.getType().equals(resp.getType()) && StrUtil.isNotEmpty(resp.getMsg())) {
                // 要发视频的时候先提醒耐心等待 再异步慢慢发
                SendMsgUtil.sendGroupMsg(groupId, BaseConsts.GirlVideo.SUCCESS, userId);
                ThreadPoolManager.addBaseTask(() -> SendMsgUtil.sendGroupVideo(groupId, resp.getMsg(), userId));
            } else if (resp != null && ENRespType.FILE.getType().equals(resp.getType()) && StrUtil.isNotEmpty(resp.getMsg())) {
                SendMsgUtil.sendFile(groupId, resp.getMsg());
            } else if (resp != null && ENRespType.AUDIO.getType().equals(resp.getType())&& StrUtil.isNotEmpty(resp.getMsg())) {
                SendMsgUtil.sendAudio(groupId, resp.getMsg());
            } else if (resp != null && StrUtil.isNotEmpty(resp.getMsg())) {
                SendMsgUtil.sendGroupMsg(groupId, resp.getMsg(), userId);
            }
//            }
//            else if (msg.startsWith(BaseConsts.Activity.ACTIVITY_JX3)) {
//                // 增加抽奖逻辑
//                log.info(String.format("消息体：%s", message));
//                log.info(String.format("收到群消息: %s", msg));
//                String groupId = (String) data.get("fromGroup");
//                CommonResp resp = distributor.doDistributeWithString(msg, userId, groupId);
//                SendMsgUtil.sendGroupMsg(groupId, resp.getMsg(), userId);
//            }
        }
        // 私聊图片
        if ("60002".equals(messageType)) {
            log.info("收到私聊图片");
            CommonResp resp = pictureDistributor.dealPicture(msg, msgId, userId, null);
            // 支持回复图片和文字
            if (resp != null && ENRespType.IMG.getType().equals(resp.getType())) {
                SendMsgUtil.sendImg(userId, resp.getMsg());
            } else if (resp != null) {
                SendMsgUtil.sendMsg(userId, resp.getMsg());
            }
        }
        // 群聊图片
        if ("80002".equals(messageType)) {
            String groupId = (String) data.get("fromGroup");
            CommonResp resp = pictureDistributor.dealPicture(msg, msgId, userId, groupId);
            // 支持回复图片和文字
            if (resp != null && ENRespType.IMG.getType().equals(resp.getType())) {
                SendMsgUtil.sendImg(groupId, resp.getMsg());
            } else if (resp != null) {
                SendMsgUtil.sendMsg(userId, resp.getMsg());
            }
        }
        // 私聊表情
        if ("60006".equals(messageType)) {
            log.info("收到私聊emoji");
            Integer length = (Integer) data.get("length");
            String md5 = (String) data.get("md5");
            emojiDistributor.dealEmoji(md5, length, msgId, userId, null);
        }
        // 群聊表情
        if ("80006".equals(messageType)) {
            String groupId = (String) data.get("fromGroup");
            Integer length = (Integer) data.get("length");
            String md5 = (String) data.get("md5");
            emojiDistributor.dealEmoji(md5, length, msgId, userId, groupId);
        }
        // 私聊邀请进群
        if ("60022".equals(messageType)) {
            String url = (String) data.get("url");
            SendMsgUtil.acceptUrl(url);
        }
        // 进群
        if ("85008".equals(messageType) || "85009".equals(messageType)) {
            String groupId = (String) data.get("fromGroup");
            String toUser = (String) data.get("toUser");
            String content = SystemConfigCache.welcomeMap.get(groupId);
            if (content != null) {
                SendMsgUtil.sendGroupMsg(groupId, content, toUser);
            }
        }

        // 私聊小程序消息
        if ("60010".equals(messageType)) {
            String content = (String) data.get("content");
            int startIndex = content.indexOf("<appmsg");
            int endIndex = content.indexOf("</appmsg>");
            if (startIndex != -1 && endIndex != -1) {
                String appmsgContent = content.substring(startIndex, endIndex + 8); // +8 包含 "</appmsg>"
                if ("1".equals(SystemManager.appletModel)) {
                    appmsgContent = appmsgContent.replace("如寄", "小林");
                    sendApplet2AllUser(appmsgContent);
                    SystemManager.appletModel = null;
                }
            }
        }
    }

    private void sendApplet2AllUser(String content) {
        for (String token : SystemConfigCache.userDateMap.keySet()) {
            SendMsgUtil.sendApplet(content, token);
        }
    }

    @PostMapping("/reloadConfig")
    public void reloadConfig() {
        systemConfigHolder.init();
        msgIdList.clear();
    }

    @GetMapping("/getShows")
    public ShowHistoryResp getShows(String queryId) {
        return showHistoryManager.getShows(queryId);
    }

    @PostMapping("/QQ/event")
    public void QQChat(HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        byte[] httpBody = IoUtil.readBytes(request.getInputStream());
        String bodyStr = new String(httpBody, StandardCharsets.UTF_8);
        log.info("收到qq消息：" + bodyStr);
        BotDo.BotEvent event = JSONUtil.toBean(bodyStr, BotDo.BotEvent.class);
        String message = event.getD().getEvent_ts() + event.getD().getPlain_token();
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        String signature = SignatureED25519.sign(QQSender.clientSecret, messageBytes);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, String> res = new HashMap<>();
        res.put("plain_token", event.getD().getPlain_token());
        res.put("signature", signature);
        out.println(JSONUtil.toJsonStr(res));
        out.close();
        JSONObject allBody = JSONUtil.parseObj(bodyStr);
        JSONObject jsonObject = (JSONObject) allBody.get("d");
        QQGroupMessage qqGroupMessage = JSONUtil.toBean(jsonObject, QQGroupMessage.class);
        CommonResp resp = qqDealDistributor.req2Resp(qqGroupMessage.getContent().replaceAll("/", "").trim(), qqGroupMessage.getAuthor().getMember_openid(), qqGroupMessage.getGroup_openid(), "qq");
        if (resp != null && ENRespType.IMG.getType().equals(resp.getType())) {
            log.info("回复：" + resp.getMsg());
            QQSender.sendGroupMessageMedia(qqGroupMessage.getGroup_openid(), resp.getMsg(), " ", qqGroupMessage.getId());
        } else if (resp != null) {
            log.info("回复：" + resp.getMsg());
            QQSender.sendGroupMessageTxt(qqGroupMessage.getGroup_openid(), resp.getMsg(), qqGroupMessage.getId());
        }
    }

}
