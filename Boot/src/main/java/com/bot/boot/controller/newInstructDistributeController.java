package com.bot.boot.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.bot.base.service.Distributor;
import com.bot.common.enums.ENFileType;
import com.bot.common.util.SendMsgUtil;
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

    @Value("${help.url}")
    private String helpImg;

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
            if (msg.contains(ENFileType.HELP_IMG.getLabel())) {
                SendMsgUtil.sendImg(userId, helpImg);
                return;
            }
            if (msg.contains(ENFileType.GAME_FILE.getLabel())) {
                SendMsgUtil.sendMsg(userId, "游戏pc端已停止维护，不再提供下载，如果有需要请联系我主人哦~");
                return;
            }
            String resp = distributor.doDistributeWithString(msg, userId);
            if (resp != null && resp.startsWith("http")) {
                SendMsgUtil.sendImg(userId, resp);
            }else {
                SendMsgUtil.sendMsg(userId, resp);
            }
        }
        // 群聊
        if (StrUtil.equals("80001", messageType)) {
            if (msg.startsWith("@小林Bot")) {
                log.info(String.format("消息体：%s", message));
                log.info(String.format("收到群消息: %s", msg));
                String groupId = (String) data.get("fromGroup");
                String effectMsg = msg.split("\u2005")[1];
                if (effectMsg.contains(ENFileType.HELP_IMG.getLabel())) {
                    SendMsgUtil.sendImg(groupId, helpImg);
                    return;
                }
                if (effectMsg.contains(ENFileType.GAME_FILE.getLabel())) {
                    SendMsgUtil.sendMsg(groupId, "游戏pc端已停止维护，不再提供下载，如果有需要请联系我主人哦~");
                    return;
                }
                String resp = distributor.doDistributeWithString(effectMsg, userId);
                if (resp != null && resp.startsWith("http")) {
                    SendMsgUtil.sendImg(groupId, resp);
                }else {
                    SendMsgUtil.sendMsg(groupId, resp);
                }
            }
        }
    }

    @PostMapping("/reloadConfig")
    public void reloadConfig() {
        systemConfigHolder.init();
    }

}
