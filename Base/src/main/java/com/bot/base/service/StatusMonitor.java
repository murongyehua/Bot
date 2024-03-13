package com.bot.base.service;

import com.bot.base.dto.UserTempInfoDTO;
import com.bot.common.constant.BaseConsts;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * 状态监控者
 * @author murongyehua
 * @version 1.0 2020/9/28
 */
@Slf4j
@Component
public class StatusMonitor {

    @PostConstruct
    public void systemManagerMonitor () {
        ThreadPoolManager.addBaseTask(() -> {
            try {
                while (true) {
                    UserTempInfoDTO userTempInfo = SystemManager.userTempInfo;
                    Date now = new Date();
                    if (userTempInfo != null && now.getTime() - userTempInfo.getOutTime().getTime() > 0 ) {
                        SystemManager.userTempInfo = null;
                        SendMsgUtil.sendMsg(userTempInfo.getToken(), BaseConsts.SystemManager.MANAGE_OUT_TIME);
                    }
                    Thread.sleep(10000);
                }
            }catch (Exception e) {
                log.error("管理模式状态监控出现异常", e);
            }
        });
    }

}
