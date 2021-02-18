package com.bot.boot.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.WeChatResp;
import com.bot.base.dto.WeChatRespData;
import com.bot.base.service.Distributor;
import com.bot.common.enums.ENFileType;
import com.bot.common.util.TextUtil;
import com.bot.game.dto.ResultContext;
import com.bot.game.service.CheckReg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 指令接收Controller
 * @author murongyehua
 * @version 1.0 2020/9/22
 */
@RestController
@RequestMapping("/instruct")
@Slf4j
public class InstructDistributor {

    @Autowired
    private Distributor distributor;

    @Autowired
    private CheckReg checkReg;

    @PostMapping("/listener")
    public void listener(HttpServletRequest request, HttpServletResponse response) {
        String o = request.getParameter("Event");
        String key = request.getParameter("Key");
        String sender = request.getParameter("Sender");
        String senderName = request.getParameter("SenderName");
        String message = request.getParameter("Message");
        String groupId = request.getParameter("GroupId");
        String groupName = request.getParameter("GroupName");
        if ("NormalIM".equals(o)) {
            log.info("接收到QQ私人消息[{}]，发送人[{}]", message, sender);
            distributor.doDistribute(response, message.trim(), sender);
        }
        if ("ClusterIM".equals(o) && message.startsWith("@小林Bot")) {
            log.info("接收到QQ群消息[{}],群号[{}],群名[{}],发送人[{}]", message, groupId, groupName, sender);
            message = message.split("\\)")[1];
            log.info("截取后的消息: [{}]", message);
            distributor.doDistribute(response, message.trim(), sender);
        }
        if ("DiscussionIM".equals(o) && message.startsWith("@小林Bot")) {
            log.info("接收到QQ讨论组消息[{}],讨论组ID[{}],,发送人[{}]", message, groupId, sender);
            message = message.split("\\)")[1];
            log.info("截取后的消息: [{}]", message);
            distributor.doDistribute(response, message.trim(), sender);
        }
    }

    @PostMapping(value = "/chatListener", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object chatWith(WeChatResp resp, HttpServletRequest request) {
        String content = TextUtil.requestToString(request);
        JSONObject object = JSONUtil.parseObj(content);
        String type = (String) object.get("type");
        String user = (String) object.get("user");
        JSONObject data = (JSONObject) object.get("data");
        String dataType = (String) data.get("data_type");
        String token = (String) data.get("from_wxid");
        String sendUserName = (String) data.get("from_nickname");
        String msg = ((String) data.get("msg")).trim();
        if ("1".equals(dataType)) {
            if ("msg::single".equals(type)) {
                log.info("接收到微信私人消息[{}]，发送人[{}]", msg, token);
                if (msg.contains(ENFileType.HELP_IMG.getLabel())) {
                    this.getFileResp(resp, token, 2, distributor.doDistributeWithFilePath(ENFileType.HELP_IMG));
                }else if (msg.contains(ENFileType.GAME_FILE.getLabel())) {
                    this.getFileResp(resp, token, 5, distributor.doDistributeWithFilePath(ENFileType.GAME_FILE));
                }else {
                    String response = distributor.doDistributeWithString(msg, token);
                    this.getMsgResp(resp, token, response);
                }
            }else if ("msg::chatroom".equals(type) && msg.startsWith("@小林Bot")){
                String chatRoom = (String) data.get("from_chatroom_wxid");
                String sendUser = (String) data.get("from_member_wxid");
                log.info("接收到微信群消息[{}]，发送人[{}]", msg, sendUser);
                msg = msg.split("\\?")[1];
                log.info("截取后的消息: [{}]", msg);
                if (msg.contains(ENFileType.HELP_IMG.getLabel())) {
                    this.getFileResp(resp, chatRoom, 2, distributor.doDistributeWithFilePath(ENFileType.HELP_IMG));
                }else if (msg.contains(ENFileType.GAME_FILE.getLabel())) {
                    this.getFileResp(resp, chatRoom, 5, distributor.doDistributeWithFilePath(ENFileType.GAME_FILE));
                }else {
                    String response = distributor.doDistributeWithString(msg, sendUser);
                    this.getMsgResp(resp, chatRoom, response);
                    resp.getData()[0].setAt_someone(sendUser);
                }
            }
        }
        return JSONUtil.toJsonStr(JSONUtil.parseObj(resp));
    }

    private void getMsgResp(WeChatResp resp, String toUser, String msg) {
        WeChatRespData[] weChatRespData = new WeChatRespData[]{new WeChatRespData()};
        resp.setData(weChatRespData);
        resp.getData()[0].setCl(1);
        resp.setTo_user(toUser);
        resp.getData()[0].setMsg(msg);
    }

    private void getFileResp(WeChatResp resp, String toUser, int type, String filePath) {
        WeChatRespData[] weChatRespData = new WeChatRespData[]{new WeChatRespData()};
        resp.setData(weChatRespData);
        resp.getData()[0].setCl(type);
        resp.setTo_user(toUser);
        if (type == 2) {
            resp.getData()[0].setImg_abspath(filePath);
        }else {
            resp.getData()[0].setFile_abspath(filePath);
        }
    }

    @PostMapping("/test")
    public String test(String msg) {
        String token = "test123";
        return distributor.doDistributeWithString(msg, token);
    }

    @PostMapping("/testOther")
    public String testOther(String msg) {
        String token = "test321";
        return distributor.doDistributeWithString(msg, token);
    }

    @PostMapping("/checkReg")
    public String checkReg(String token) {
        boolean isReg = checkReg.checkReg(token);
        return String.valueOf(isReg);
    }

    @PostMapping("/reg")
    public ResultContext reg(String nickName) {
        return checkReg.reg(nickName);
    }

    @PostMapping("/client")
    public String clientGame(String token, String msg) {
        return distributor.doDistributeWithString(msg, token);
    }

}
