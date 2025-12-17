package com.bot.base.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.service.impl.ActivityServiceImpl;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import com.bot.game.dao.entity.BotUserConfig;
import com.bot.game.dao.entity.BotUserConfigExample;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import com.bot.game.service.SystemConfigHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenServerTask {

    private static final Map<String, String> SERVER_STATUS_MAP = new HashMap<>();

    @Resource
    private BotUserConfigMapper userConfigMapper;

    @Resource
    private ActivityServiceImpl activityService;

    @PostConstruct
    public void run() {
        ThreadPoolManager.addBaseTask(() -> {
            while (true) {
                try {
                    if (CollectionUtil.isNotEmpty(SystemConfigCache.openServer)) {
                        BotUserConfigExample example = new BotUserConfigExample();
                        example.createCriteria().andUserIdIn(SystemConfigCache.openServer);
                        List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(example);
                        for (BotUserConfig userConfig : userConfigList) {
                            // 本轮已查询过
                            if (SERVER_STATUS_MAP.containsKey(userConfig.getJxServer())) {
                                if (SERVER_STATUS_MAP.get(userConfig.getJxServer()).equals(userConfig.getJxServerStatus())) {
                                    continue;
                                } else {
                                    userConfig.setJxServerStatus(SERVER_STATUS_MAP.get(userConfig.getJxServer()));
                                    userConfigMapper.updateByPrimaryKey(userConfig);
                                    if (userConfig.getUserId().contains("chatroom")) {
                                        SendMsgUtil.sendGroupMsg(userConfig.getUserId(), SERVER_STATUS_MAP.get(userConfig.getJxServer()), null);
                                    } else {
                                        SendMsgUtil.sendMsg(userConfig.getUserId(), SERVER_STATUS_MAP.get(userConfig.getJxServer()));
                                    }
                                }
                                continue;
                            }
                            // 本轮未查询 去查询
                            String result = activityService.openServer(userConfig.getJxServer());
                            if (!result.contains("失败")) {
                                SERVER_STATUS_MAP.put(userConfig.getJxServer(), result);
                                if (result.equals(userConfig.getJxServerStatus())) {
                                    continue;
                                }
                                userConfig.setJxServerStatus(result);
                                userConfigMapper.updateByPrimaryKey(userConfig);
                                if (userConfig.getUserId().contains("chatroom")) {
                                    SendMsgUtil.sendGroupMsg(userConfig.getUserId(), result, null);
                                } else {
                                    SendMsgUtil.sendMsg(userConfig.getUserId(), result);
                                }
                            }
                        }
                    }
                    Thread.sleep(1000 * 60 * 3);
                } catch (Exception e) {
                    log.error("开启服务器任务异常", e);
                    try {
                        Thread.sleep(1000 * 60 * 2);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }finally {
                    SERVER_STATUS_MAP.clear();
                }
            }
        });
    }

}
