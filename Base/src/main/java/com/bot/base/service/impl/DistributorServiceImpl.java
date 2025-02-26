package com.bot.base.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.bot.base.chain.Collector;
import com.bot.base.chain.Menu;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.*;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.enums.*;
import com.bot.common.loader.CommonTextLoader;
import com.bot.common.constant.BaseConsts;
import com.bot.common.exception.BotException;
import com.bot.base.commom.MessageSender;
import com.bot.common.util.HttpSenderUtil;
import com.bot.game.service.GameHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

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

    @Resource
    private WorkManager workManager;

    @Autowired
    private GameHandler gameHandler;

    @Resource
    private RegService regService;

    @Resource
    private TopTokenServiceImpl topTokenService;

    @Resource
    private ActivityServiceImpl activityService;

    @Value("${help.img.path}")
    private String helpImgPath;

    @Value("${game.file.path}")
    private String gameFilePath;

    private final static Map<String, String> GAME_TOKENS = new HashMap<>();

    @Override
    @Deprecated
    public void doDistribute(HttpServletResponse response, String reqContent, String token) {
        try{
            response.setCharacterEncoding("utf-8");
            CommonResp resp = this.req2Resp(reqContent, token, null, false);
            log.info("回复[{}],[{}]", token, resp);
            if (resp.getMsg().contains(BaseConsts.Distributor.AND_REG)) {
                String[] responseContents = resp.getMsg().split(BaseConsts.Distributor.AND_REG);
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
    public CommonResp doDistributeWithString(String reqContent, String token, String groupId, boolean at) {
        try{
            CommonResp resp = this.req2Resp(reqContent, token, groupId, at);
            if (resp == null) {
                log.info("[{}]不予回复", token);
                return null;
            }
            log.info("回复[{}],[{}]", token, resp.getMsg());
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

    private CommonResp req2Resp(String reqContent, String token, String groupId, boolean at) {
        // 顶级token 走专属逻辑
        if (SystemConfigCache.topToken.contains(groupId == null ? token : groupId)) {
            return topTokenService.doQueryReturn(reqContent, groupId == null ? token : groupId, groupId);
        }
        // 判断是不是进入管理模式
        if (BaseConsts.SystemManager.TRY_INTO_MANAGER_INFO.equals(reqContent)) {
            return new CommonResp(SystemManager.tryIntoManager(token), ENRespType.TEXT.getType());
        }
        // 判断是不是处于管理模式
        if (SystemManager.userTempInfo != null && SystemManager.userTempInfo.getToken().equals(token)) {
            return new CommonResp(systemManager.managerDistribute(reqContent), ENRespType.TEXT.getType());
        }
        // jx3
        if (reqContent.startsWith(BaseConsts.Activity.ACTIVITY_JX3)) {
            return activityService.doQueryReturn(reqContent, token, groupId);
        }
        // 开通服务
        if (reqContent.startsWith(BaseConsts.SystemManager.TEMP_REG_PREFIX)) {
            return new CommonResp(regService.tryTempReg(groupId == null ? token : groupId,
                    reqContent.replaceAll(BaseConsts.SystemManager.TEMP_REG_PREFIX, StrUtil.EMPTY),
                    groupId == null ? ENRegType.PERSONNEL : ENRegType.GROUP), ENRespType.TEXT.getType());
        }
        if (reqContent.startsWith(BaseConsts.SystemManager.REG_PREFIX)) {
            return new CommonResp(regService.tryReg(groupId == null ? token : groupId,
                    reqContent.replaceAll(BaseConsts.SystemManager.REG_PREFIX, StrUtil.EMPTY),
                    groupId == null ? ENRegType.PERSONNEL : ENRegType.GROUP), ENRespType.TEXT.getType());
        }
        // 判断用户状态
        String checkResult = this.checkUserStatus(groupId == null ? token : groupId);
        if (checkResult != null) {
            return new CommonResp(checkResult, ENRespType.TEXT.getType());
        }
        // 查询到期时间
        if (reqContent.contains(BaseConsts.SystemManager.QUERY_DEADLINE_DATE)) {
            return new CommonResp(regService.queryDeadLineDate(groupId == null ? token : groupId), ENRespType.TEXT.getType());
        }
        // 获取token
        if (BaseConsts.SystemManager.GET_TOKEN.equals(reqContent)) {
            return new CommonResp(StrUtil.isEmpty(groupId) ? token : groupId, ENRespType.TEXT.getType());
        }
        // 是不是处于游戏模式
        if (GAME_TOKENS.containsKey(token)) {
            if (GAME_TOKENS.get(token).equals(ENUserGameStatus.JOINED.getValue()) && BaseConsts.SystemManager.EXIT_GAME.equals(reqContent)) {
                // 退出游戏模式
                GAME_TOKENS.remove(token);
                return new CommonResp(gameHandler.exit(token), ENRespType.TEXT.getType());
            }
            if (GAME_TOKENS.get(token).equals(ENUserGameStatus.WAIT_JOIN.getValue())) {
                // 二次确认时不进入游戏模式
                if (ENYesOrNo.NO.getValue().equals(reqContent.trim())) {
                    GAME_TOKENS.remove(token);
                    return new CommonResp(BaseConsts.SystemManager.SUCCESS, ENRespType.TEXT.getType());
                }
                // 进入
                if (ENYesOrNo.YES.getValue().equals(reqContent.trim())) {
                    GAME_TOKENS.replace(token, ENUserGameStatus.JOINED.getValue());
                }
            }
            // 正常游戏模式调用
            return new CommonResp(gameHandler.play(reqContent, token), ENRespType.TEXT.getType());
        }
        // 是不是进入游戏模式
        if (BaseConsts.SystemManager.GAME.equals(reqContent)) {
            return new CommonResp("山海见闻游戏已无限期停止，感谢您的游玩和支持，有缘再见。", ENRespType.TEXT.getType());
            // 游戏停用了
//            GAME_TOKENS.put(token, ENUserGameStatus.WAIT_JOIN.getValue());
//            return new CommonResp(BaseConsts.SystemManager.JOIN_GAME_WARN, ENRespType.TEXT.getType());
        }
        // 是不是处于工作模式
        if (WorkManager.WORK_TOKENS.contains(token)) {
           return workManager.doWork(reqContent, token);
        }
        // 是不是进入工作模式
        if (BaseConsts.Work.ENTRY.equals(reqContent)) {
            return new CommonResp(workManager.entryWork(token), ENRespType.TEXT.getType());
        }
        // 固定回答最优先 完全一致才命中
        for (String keyword : CommonTextLoader.someResponseMap.keySet()) {
            if (keyword.equals(reqContent)) {
                return new CommonResp(this.getResponseByKey(keyword), ENRespType.TEXT.getType());
            }
        }
        // 先判断命中服务
        for (String keyword : CommonTextLoader.serviceInstructMap.keySet()) {
            if (reqContent.startsWith(keyword)) {
                return this.getService(CommonTextLoader.serviceInstructMap.get(keyword)).doQueryReturn(reqContent, groupId == null ? token : groupId, groupId);
            }
            if (reqContent.contains(keyword)) {
                return this.getService(CommonTextLoader.serviceInstructMap.get(keyword)).doQueryReturn(reqContent, groupId == null ? token : groupId, groupId);
            }
        }
        // 菜单取消了，都走服务
        // 再判断命中菜单 目前只有一个主菜单 后续可能有多个主菜单
//        for (String keyword : CommonTextLoader.menuInstructMap.keySet()) {
//            if (reqContent.contains(keyword)) {
//                // 构建菜单调用链路
//                return new CommonResp(collector.buildCollector(token), ENRespType.TEXT.getType());
//            }
//        }
        // 非服务 非主菜单 可能是菜单链路内调用
        String maybeResp = collector.toNextOrPrevious(token, reqContent.trim());
        if (maybeResp != null) {
            return new CommonResp(maybeResp, ENRespType.TEXT.getType());
        }
        // 全部未命中
        // 非群聊 或 群聊艾特 认为是闲聊
        if (groupId == null || at) {
            return geyDefaultMsg(reqContent, token, groupId);
        }
        return null;
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

    private CommonResp geyDefaultMsg(String reqContent, String token, String groupId) {
        BaseService service = serviceMap.get("defaultChatServiceImpl");
        CommonResp resp = service.doQueryReturn(reqContent, groupId == null ? token : groupId, groupId);
        if (resp != null) {
            return resp;
        }
        return null;
    }

    private String getResponseByKey(String keyword) {
        List<String> responses = CommonTextLoader.someResponseMap.get(keyword);
        int index = RandomUtil.randomInt(0, responses.size());
        return responses.get(index);
    }

    private String checkUserStatus(String activeId) {
        Date deadLineDate = SystemConfigCache.userDateMap.get(activeId);
        if (deadLineDate == null || deadLineDate.before(new Date())) {
            return BaseConsts.SystemManager.OVER_TIME_TIP;
        }
        return null;
    }

}
