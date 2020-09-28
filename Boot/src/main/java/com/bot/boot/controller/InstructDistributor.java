package com.bot.boot.controller;

import com.bot.base.service.Distributor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
            log.info("接收到私人消息[{}]，发送人[{}]", message, sender);
            distributor.doDistribute(response, message, sender);
        }
        if ("ClusterIM".equals(o) && message.startsWith("@小林Bot")) {
            log.info("接收到群消息[{}],群号[{}],群名[{}],发送人[{}]", message, groupId, groupName, sender);
            message = message.split("\\)")[1];
            log.info("截取后的消息: [{}]", message);
            distributor.doDistribute(response, message, sender);
        }
        if ("DiscussionIM".equals(o) && message.startsWith("@小林Bot")) {
            log.info("接收到讨论组消息[{}],讨论组ID[{}],,发送人[{}]", message, groupId, sender);
            message = message.split("\\)")[1];
            log.info("截取后的消息: [{}]", message);
            distributor.doDistribute(response, message, sender);
        }
    }

}
