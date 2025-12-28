package com.bot.base.task;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.service.WorkManager;
import com.bot.base.service.impl.BottleMessageServiceImpl;
import com.bot.common.util.ThreadPoolManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Slf4j
@Component
public class AutoTask {

    @PostConstruct
    public void run() {
        ThreadPoolManager.addBaseTask(() -> {
            while (true) {
                try {
                    Date now = new Date();
                    Date endTime = DateUtil.parse(DateUtil.format(now, DatePattern.NORM_DATE_PATTERN) + StrUtil.SPACE + "00:30:00");
                    // 每日零点
                    if (DateUtil.isIn(now, DateUtil.beginOfDay(now), endTime)) {
                        WorkManager.WORK_TOKENS.clear();
                        WorkManager.WAIT_DEAL_DATA_LIST.clear();
                        BottleMessageServiceImpl.TODAY_ID = null;
                    }
                }catch (Exception e) {
                    log.error("每日自动任务异常", e);
                }finally {
                    try {
                        Thread.sleep(30 * 60 * 1000);
                    }catch (Exception e) {
                        // do nothing
                    }

                }
            }
        });
    }

}
