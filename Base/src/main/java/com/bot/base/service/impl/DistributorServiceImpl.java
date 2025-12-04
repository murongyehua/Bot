package com.bot.base.service.impl;

import cn.hutool.core.util.ObjectUtil;
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
import com.bot.common.util.SendMsgUtil;
import com.bot.game.dao.entity.BotEmoji;
import com.bot.game.dao.entity.BotEmojiExample;
import com.bot.game.dao.mapper.BotEmojiMapper;
import com.bot.game.service.GameHandler;
import com.bot.life.service.LifeHandler;
import com.twelvemonkeys.imageio.metadata.tiff.IFD;
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

    @Autowired
    private LifeHandler lifeHandler;

    @Autowired
    private GameRoomManager gameRoomManager;

    @Resource
    private RegService regService;

    @Resource
    private TopTokenServiceImpl topTokenService;

    @Resource
    private SignServiceImpl signService;

    @Resource
    private UserBoxServiceImpl userBoxService;

    @Resource
    private ActivityServiceImpl activityService;

    @Value("${help.img.path}")
    private String helpImgPath;

    @Value("${game.file.path}")
    private String gameFilePath;

    @Value("${manager.token}")
    private String managerToken;

    @Resource
    private BotEmojiMapper emojiMapper;

    private final static Map<String, String> GAME_TOKENS = new HashMap<>();
    private final static Map<String, String> LIFE_GAME_TOKENS = new HashMap<>();

    private final static Map<String, List<String>> TEMP_CHAT_RECORD = new HashMap<>();

    private final static Map<String, List<String>> BOT_SEND_RECORD = new HashMap<>();

    @Resource
    private DefaultChatServiceImpl defaultChatService;

    @Override
    @Deprecated
    public void doDistribute(HttpServletResponse response, String reqContent, String token, String channel) {
        try{
            response.setCharacterEncoding("utf-8");
            CommonResp resp = this.req2Resp(reqContent, token, null, false, false, channel);
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
    public CommonResp doDistributeWithString(String reqContent, String token, String groupId, boolean at, boolean mustRespFlag, String channel, String withoutPexContent) {
        try{
            CommonResp resp = this.req2Resp(withoutPexContent, token, groupId, at, mustRespFlag, channel);
            if (resp == null) {
                // 不回复时记录内容，如果连续两条内容一样就复制一条跟着发
                if (groupId != null) {
                    // 特殊处理 如果是表情包三个字就随机一个表情包
                    if ("表情包".equals(withoutPexContent)) {
                        BotEmoji emoji = this.fetchRandomEmoji();
                        if (emoji.getMd5().startsWith("http")) {
                            return new CommonResp(emoji.getMd5(), ENRespType.IMG.getType());
                        }
                        SendMsgUtil.sendEmoji(groupId, emoji.getMd5(), Integer.parseInt(emoji.getImgsize()));
                        return null;
                    }
                    if (TEMP_CHAT_RECORD.containsKey(groupId)) {
                        List<String> record = TEMP_CHAT_RECORD.get(groupId);
                        if (record.size() > 0) {
                            String lastContent = record.get(record.size() - 1);
                            if (lastContent.equals(withoutPexContent)) {
                                // 检查是否已经发送过这条重复消息
                                if (BOT_SEND_RECORD.containsKey(groupId) && BOT_SEND_RECORD.get(groupId).contains(lastContent)) {
                                    // 已经发过的内容不重复发送
                                    log.info("[{}]群聊中的重复消息不再重复发送", token);
                                } else {
                                    // 记录即将发送的重复消息
                                    if (BOT_SEND_RECORD.containsKey(groupId)) {
                                        BOT_SEND_RECORD.get(groupId).add(lastContent);
                                    } else {
                                        BOT_SEND_RECORD.put(groupId, new ArrayList<String>(){{add(lastContent);}});
                                    }
                                    return new CommonResp(lastContent, ENRespType.TEXT.getType());
                                }
                            }
                            record.add(withoutPexContent);
                        }
                    } else {
                        TEMP_CHAT_RECORD.put(groupId, new ArrayList<String>(){{add(withoutPexContent);}});
                        // 防止后面一直不说话了，这里5句清除一下
                        if (BOT_SEND_RECORD.containsKey(groupId) && BOT_SEND_RECORD.get(groupId).size() > 5) {
                            BOT_SEND_RECORD.remove(groupId);
                        }
                    }
                    // 如果是艾特或者是内容包含小林，就回复
                    if (at || reqContent.contains("小林")) {
                        String respContent = defaultChatService.deepChat(reqContent, "group", groupId);
                        return new CommonResp(respContent, ENRespType.TEXT.getType());
                    }
                    // 上面都未触发，则有15%的机率回复
                    String frequency = SystemConfigCache.chatFrequency.get(groupId);
                    if (Math.random() < (StrUtil.isNotEmpty(frequency) ? Double.parseDouble(frequency) : 0.15)) {
                        // 回复时有8%的几率是表情包
                        if (Math.random() < 0.08) {
                            BotEmoji emoji = this.fetchRandomEmoji();
                            if (emoji.getMd5().startsWith("http")) {
                                return new CommonResp(emoji.getMd5(), ENRespType.IMG.getType());
                            }
                            SendMsgUtil.sendEmoji(groupId, emoji.getMd5(), Integer.parseInt(emoji.getImgsize()));
                            return null;
                        }
                        String respContent = defaultChatService.deepChat(reqContent, "group", groupId);
                        return new CommonResp(respContent, ENRespType.TEXT.getType());
                    }
                }
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

    private BotEmoji fetchRandomEmoji() {
        return emojiMapper.selectByExample(new BotEmojiExample()).get(new Random().nextInt(emojiMapper.selectByExample(new BotEmojiExample()).size()));
    }

    private CommonResp req2Resp(String reqContent, String token, String groupId, boolean at, boolean mustRespFlag, String channel) {
        // 顶级token 走专属逻辑
        if (SystemConfigCache.topToken.contains(groupId == null ? token : groupId)) {
            return topTokenService.doQueryReturn(reqContent, groupId == null ? token : groupId, groupId, channel);
        }
        // 签到资格token 优先走专属逻辑 如果没有匹配上再用后续逻辑
        if (SystemConfigCache.signToken.contains(groupId == null ? token : groupId)) {
            CommonResp resp = signService.doQueryReturn(reqContent, token, groupId, channel);
            if (resp != null) {
                return resp;
            }
        }
        // 判断是不是进入管理模式
        if (BaseConsts.SystemManager.TRY_INTO_MANAGER_INFO.equals(reqContent)) {
            return new CommonResp(SystemManager.tryIntoManager(token), ENRespType.TEXT.getType());
        }
        // 判断是不是处于管理模式
        if (SystemManager.userTempInfo != null && SystemManager.userTempInfo.getToken().equals(token)) {
            return new CommonResp(systemManager.managerDistribute(reqContent), ENRespType.TEXT.getType());
        }
        // 过一遍系统级指令
        CommonResp userResp = userBoxService.doQueryReturn(reqContent, token, groupId, channel);
        if (userResp != null) {
            return userResp;
        }
        // 开通服务
        if (reqContent.startsWith(BaseConsts.SystemManager.TEMP_REG_PREFIX)) {
            return new CommonResp("现已取消试用功能，请兑换资格使用正式版。", ENRespType.TEXT.getType());
        }
        if (reqContent.startsWith(BaseConsts.SystemManager.REG_PREFIX)) {
            return new CommonResp("原开通功能现已停用，请使用碎玉兑换。", ENRespType.TEXT.getType());
        }
        // 生成邀请码
        if (reqContent.equals(BaseConsts.SystemManager.USER_CREATE_INVITE_CODE)) {
            return new CommonResp("原邀请码服务现已停用，请使用碎玉兑换。", ENRespType.TEXT.getType());
        }
        // 判断用户状态
        String checkResult = this.checkUserStatus(groupId == null ? token : groupId);
        if (checkResult != null) {
            return new CommonResp(checkResult, ENRespType.TEXT.getType());
        }
        // jx3
        if (reqContent.startsWith(BaseConsts.Activity.ACTIVITY_JX3)) {
            return activityService.doQueryReturn(reqContent, token, groupId, channel);
        }
        // 查询到期时间
        if (reqContent.equals(BaseConsts.SystemManager.QUERY_DEADLINE_DATE)) {
            return new CommonResp(regService.queryDeadLineDate(groupId == null ? token : groupId), ENRespType.TEXT.getType());
        }
        // 获取token
        if (BaseConsts.SystemManager.GET_TOKEN.equals(reqContent)) {
            return new CommonResp(StrUtil.isEmpty(groupId) ? token : groupId, ENRespType.TEXT.getType());
        }
        // 先判断命中服务
        for (String keyword : CommonTextLoader.serviceInstructMap.keySet()) {
            if (reqContent.startsWith(keyword) || reqContent.contains(keyword)) {
                CommonResp resp = this.getService(CommonTextLoader.serviceInstructMap.get(keyword)).doQueryReturn(reqContent,  token, groupId, channel);
                if (resp != null) {
                    return resp;
                }
                // 返回null的时候，要根据必须回复标记来判断是继续走默认聊天逻辑还是直接不予回复
                if (!mustRespFlag) {
                    return null;
                }
            }
        }
        // 是不是处于浮生卷游戏模式
        if (LIFE_GAME_TOKENS.containsKey(token)) {
            if ("退出".equals(reqContent)) {
                LIFE_GAME_TOKENS.remove(token);
                String imagePath = lifeHandler.exit(token);
                return new CommonResp(imagePath, ENRespType.IMG.getType());
            }
            String imagePath = lifeHandler.play(reqContent, token);
            if (imagePath != null) {
                return new CommonResp(imagePath, ENRespType.IMG.getType());
            }
        }
        // 是不是进入浮生卷游戏模式
        if ("浮生卷".equals(reqContent)) {
            LIFE_GAME_TOKENS.put(token, "active");
            String imagePath = lifeHandler.play(reqContent, token);
            return new CommonResp(imagePath, ENRespType.IMG.getType());
        }

        // ==========小游戏房间系统==========
        // 优先级最高：检查用户是否在游戏中
        if (gameRoomManager.isUserInGame(token)) {
            String gameResp = gameRoomManager.handleGameInstruction(token, reqContent);
            if (gameResp != null) {
                return new CommonResp(gameResp, ENRespType.TEXT.getType());
            }
        }
        // 游戏房间指令检查
        String gameRoomResp = gameRoomManager.handleGameCommand(reqContent, token, groupId);
        if (gameRoomResp != null) {
            return new CommonResp(gameRoomResp, ENRespType.TEXT.getType());
        }
        // ==========结束小游戏房间系统==========
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
            return geyDefaultMsg(reqContent, token, groupId, channel);
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

    private CommonResp geyDefaultMsg(String reqContent, String token, String groupId, String channel) {
        // 1.5.0.0增加逻辑，不支持群聊闲聊
        if (groupId != null) {
//            return new CommonResp("小林的群聊内的闲聊功能已下线，如有Ai使用需求请添加好友私聊使用，或者你可以使用小林的其他功能！\r\b发送”菜单“或者”帮助“获取使用手册，除闲聊外的功能仍可正常触发呢~", ENRespType.TEXT.getType());
            return null;
        }
        BaseService service = serviceMap.get("defaultChatServiceImpl");
        CommonResp resp = service.doQueryReturn(reqContent, token, groupId, channel);
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
