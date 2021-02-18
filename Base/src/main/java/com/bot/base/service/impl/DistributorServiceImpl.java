package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.chain.Collector;
import com.bot.base.chain.Menu;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENFileType;
import com.bot.common.loader.CommonTextLoader;
import com.bot.base.service.Distributor;
import com.bot.base.service.SystemManager;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENUserGameStatus;
import com.bot.common.enums.ENYesOrNo;
import com.bot.common.exception.BotException;
import com.bot.base.commom.MessageSender;
import com.bot.game.service.GameHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
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

    @Autowired
    private GameHandler gameHandler;

    @Value("${help.img.path}")
    private String helpImgPath;

    @Value("${game.file.path}")
    private String gameFilePath;

    private final static Map<String, String> GAME_TOKENS = new HashMap<>();

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
            log.error("目标[{}],响应异常", token, e);
        }
    }

    @Override
    public String doDistributeWithString(String reqContent, String token) {
        try{
            String resp = this.req2Resp(reqContent, token);
            log.info("回复[{}],[{}]", token, resp);
            return resp;
        }catch (Exception e) {
            log.error("目标[{}],响应异常", token, e);
        }
        return null;
    }

    @Override
    public String doDistributeWithFilePath(ENFileType enFileType) {
        switch (enFileType) {
            case HELP_IMG:
                return helpImgPath;
            case GAME_FILE:
                return gameFilePath;
            default:
                return StrUtil.EMPTY;
        }
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
        // 获取token
        if (BaseConsts.SystemManager.GET_TOKEN.equals(reqContent)) {
            return token;
        }
        // 是不是处于游戏模式
        if (GAME_TOKENS.keySet().contains(token)) {
            if (GAME_TOKENS.get(token).equals(ENUserGameStatus.JOINED.getValue()) && BaseConsts.SystemManager.EXIT_GAME.equals(reqContent)) {
                // 退出游戏模式
                GAME_TOKENS.remove(token);
                return gameHandler.exit(token);
            }
            if (GAME_TOKENS.get(token).equals(ENUserGameStatus.WAIT_JOIN.getValue())) {
                // 二次确认时不进入游戏模式
                if (ENYesOrNo.NO.getValue().equals(reqContent.trim())) {
                    GAME_TOKENS.remove(token);
                    return BaseConsts.SystemManager.SUCCESS;
                }
                // 进入
                if (ENYesOrNo.YES.getValue().equals(reqContent.trim())) {
                    GAME_TOKENS.replace(token, ENUserGameStatus.JOINED.getValue());
                }
            }
            // 正常游戏模式调用
            return gameHandler.play(reqContent, token);
        }
        // 是不是进入游戏模式
        if (BaseConsts.SystemManager.GAME.equals(reqContent)) {
            GAME_TOKENS.put(token, ENUserGameStatus.WAIT_JOIN.getValue());
            return BaseConsts.SystemManager.JOIN_GAME_WARN;
        }
        // 固定回答最优先 完全一致才命中
        for (String keyword : CommonTextLoader.someResponseMap.keySet()) {
            if (keyword.equals(reqContent)) {
                return this.getResponseByKey(keyword);
            }
        }
        // 先判断命中服务
        for (String keyword : CommonTextLoader.serviceInstructMap.keySet()) {
            if (reqContent.startsWith(keyword)) {
                return this.getService(CommonTextLoader.serviceInstructMap.get(keyword)).doQueryReturn(reqContent, token);
            }
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
        return geyDefaultMsg(reqContent, token);
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

    private String geyDefaultMsg(String reqContent, String token) {
        BaseService service = serviceMap.get("defaultChatServiceImpl");
        String resp = service.doQueryReturn(reqContent, token);
        if (resp != null) {
            return resp;
        }
        int index = RandomUtil.randomInt(0, CommonTextLoader.defaultResponseMsg.size());
        return CommonTextLoader.defaultResponseMsg.get(index);
    }

    private String getResponseByKey(String keyword) {
        List<String> responses = CommonTextLoader.someResponseMap.get(keyword);
        int index = RandomUtil.randomInt(0, responses.size());
        return responses.get(index);
    }

}
