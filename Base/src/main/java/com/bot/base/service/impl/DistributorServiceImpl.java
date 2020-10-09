package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.bot.base.chain.Collector;
import com.bot.base.chain.Menu;
import com.bot.base.service.BaseService;
import com.bot.base.service.CommonTextLoader;
import com.bot.base.service.Distributor;
import com.bot.base.service.SystemManager;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.exception.BotException;
import com.bot.base.commom.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 指令分发
 * @author murongyehua
 * @version 1.0 2020/9/23
 */
@Slf4j
@Service
public class DistributorServiceImpl implements Distributor {

    @Autowired
    private Map<String, BaseService> serviceMap;

    @Autowired
    private Map<String, Menu> menuPrinterMap;

    @Autowired
    private Collector collector;

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private SystemManager systemManager;

    @Override
    public void doDistribute(HttpServletResponse response, String reqContent, String token) {
        try{
            response.setCharacterEncoding("utf-8");
            String resp = this.req2Resp(reqContent, token);
            log.info("回复[{}],[{}]", token, resp);
            if (resp.contains(BaseConsts.Distributor.AND_REG)) {
                String[] responseContents = resp.split(BaseConsts.Distributor.AND_REG);
                for (String responseContent : responseContents) {
                    messageSender.send(token, responseContent);
                }
                return;
            }
            response.getWriter().print(resp);
        }catch (Exception e) {
            log.error("目标[{}],响应异常");
        }
    }

    @Override
    public String doDistributeWithString(String reqContent, String token) {
        try{
            String resp = this.req2Resp(reqContent, token);
            log.info("回复[{}],[{}]", token, resp);
            return resp;
        }catch (Exception e) {
            log.error("目标[{}],响应异常");
        }
        return null;
    }

    private String req2Resp(String reqContent, String token) {
        // 判断是不是进入管理模式
        if (BaseConsts.SystemManager.TRY_INTO_MANAGER_INFO.equals(reqContent)) {
            return SystemManager.tryIntoManager(token);
        }
        // 判断是不是处于管理模式
        if (SystemManager.userTempInfo != null && SystemManager.userTempInfo.getToken().equals(token)) {
            return systemManager.managerDistribute(reqContent);
        }
        // 固定回答最优先 完全一致才命中
        for (String keyword : CommonTextLoader.someResponseMap.keySet()) {
            if (keyword.equals(reqContent)) {
                return this.getResponseByKey(keyword);
            }
        }
        // 先判断命中服务
        for (String keyword : CommonTextLoader.serviceInstructMap.keySet()) {
            if (reqContent.contains(keyword)) {
                return this.getService(CommonTextLoader.serviceInstructMap.get(keyword)).doQueryReturn(reqContent, token);
            }
        }
        // 再判断命中菜单 目前只有一个主菜单 后续可能有多个主菜单
        for (String keyword : CommonTextLoader.menuInstructMap.keySet()) {
            if (reqContent.contains(keyword)) {
                // 构建菜单调用链路
                return collector.buildCollector(token);
            }
        }
        // 非服务 非主菜单 可能是菜单链路内调用
        String maybeResp = collector.toNextOrPrevious(token, reqContent.trim());
        if (maybeResp != null) {
            return maybeResp;
        }
        // 全部未命中
        return geyDefaultMsg();
    }

    private BaseService getService(String className) {
        BaseService service = serviceMap.get(className);
        if (service == null) {
            throw new BotException("未知服务");
        }
        return service;
    }

    /**
     * 暂时没用 后续如果有多个主菜单时用起来
     * @param className
     * @return
     */
    @Deprecated
    private Menu getMenu(String className) {
        Menu menu = menuPrinterMap.get(className);
        if (menu == null) {
            throw new BotException("未知菜单");
        }
        return menu;
    }

    private String geyDefaultMsg() {
        int index = RandomUtil.randomInt(0, CommonTextLoader.defaultResponseMsg.size());
        return CommonTextLoader.defaultResponseMsg.get(index);
    }

    private String getResponseByKey(String keyword) {
        List<String> responses = CommonTextLoader.someResponseMap.get(keyword);
        int index = RandomUtil.randomInt(0, responses.size());
        return responses.get(index);
    }

}
