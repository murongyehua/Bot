package com.bot.boot.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.Distributor;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENFileType;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import com.bot.game.service.SystemConfigHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/newInstruct")
@Slf4j
public class newInstructDistributeController {

    @Resource
    private Distributor distributor;

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @PostMapping("/chatListener")
    public void weChatDeal(@RequestBody JSONObject message) {
        String messageType = String.valueOf(message.get("messageType"));
        JSONObject data = (JSONObject) message.get("data");
        String userId = (String) data.get("fromUser");
        String msg = (String) data.get("content");
        if (ObjectUtil.notEqual("60001", messageType) && ObjectUtil.notEqual("80001", messageType)) {
            return;
        }
        // 私聊
        if (StrUtil.equals("60001", messageType)) {
            log.info(String.format("收到来自[%s]的私聊消息：[%s]", userId, msg));
            if (msg.contains(ENFileType.GAME_FILE.getLabel())) {
                SendMsgUtil.sendMsg(userId, "游戏pc端已停止维护，不再提供下载，请使用微信游玩。");
                return;
            }
            CommonResp resp = distributor.doDistributeWithString(msg.trim(), userId, null, false, true);
            if (resp != null && ENRespType.IMG.getType().equals(resp.getType())) {
                SendMsgUtil.sendImg(userId, resp.getMsg());
            }else if (resp != null && ENRespType.VIDEO.getType().equals(resp.getType())) {
                // 要发视频的时候先提醒耐心等待 再异步慢慢发
                SendMsgUtil.sendMsg(userId, BaseConsts.GirlVideo.SUCCESS);
                ThreadPoolManager.addBaseTask(() -> SendMsgUtil.sendVideo(userId, resp.getMsg()));
            }else if (resp != null && ENRespType.FILE.getType().equals(resp.getType())) {
                SendMsgUtil.sendFile(userId, resp.getMsg());
            }else if (resp != null){
                SendMsgUtil.sendMsg(userId, resp.getMsg());
            }
        }
        // 群聊
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
                CommonResp resp = distributor.doDistributeWithString(effectMsg, userId, groupId, at, at);
                if (resp != null && ENRespType.IMG.getType().equals(resp.getType())) {
                    SendMsgUtil.sendImg(groupId, resp.getMsg());
                }else if (resp != null && ENRespType.VIDEO.getType().equals(resp.getType())) {
                    // 要发视频的时候先提醒耐心等待 再异步慢慢发
                    SendMsgUtil.sendGroupMsg(groupId, BaseConsts.GirlVideo.SUCCESS, userId);
                    ThreadPoolManager.addBaseTask(() -> SendMsgUtil.sendGroupVideo(groupId, resp.getMsg(), userId));
                }else if (resp != null && ENRespType.FILE.getType().equals(resp.getType())){
                    SendMsgUtil.sendFile(userId, resp.getMsg());
                }else if (resp != null) {
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
    }

    @PostMapping("/reloadConfig")
    public void reloadConfig() {
        systemConfigHolder.init();
    }



}
