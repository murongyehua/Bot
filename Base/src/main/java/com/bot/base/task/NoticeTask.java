package com.bot.base.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import com.bot.game.dao.entity.BotNotice;
import com.bot.game.dao.entity.BotNoticeExample;
import com.bot.game.dao.mapper.BotNoticeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class NoticeTask {

    @Resource
    private BotNoticeMapper noticeMapper;

    @PostConstruct
    public void doNotice() {
        ThreadPoolManager.addBaseTask(() -> {
            while (true) {
                try {
                    List<BotNotice> noticeList = noticeMapper.selectByExample(new BotNoticeExample());
                    if (CollectionUtil.isNotEmpty(noticeList)) {
                        Date now = new Date();
                        int week = DateUtil.dayOfWeek(now) - 1;
                        noticeList.forEach(x -> {
                            if (x.getNoticeDay().contains(String.valueOf(week)) && ObjectUtil.equals(DateUtil.format(now, "HH:mm"), x.getNoticeTime())) {
                                SendMsgUtil.sendGroupMsg(x.getNoticeTargetId(), x.getNoticeContent(), "1".equals(x.getAtAllFlag()) ? "notify@all" : "");
                            }
                        });
                    }
                    Thread.sleep(60000L);
                }catch (Exception e) {
                    log.error("通知服务出现异常", e);
                }
            }
        });
    }

}
