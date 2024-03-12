package com.bot.base.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRegType;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import com.bot.game.dao.entity.BotNotice;
import com.bot.game.dao.entity.BotNoticeExample;
import com.bot.game.dao.entity.BotUser;
import com.bot.game.dao.entity.BotUserExample;
import com.bot.game.dao.mapper.BotNoticeMapper;
import com.bot.game.dao.mapper.BotUserMapper;
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
    private BotUserMapper botUserMapper;

    @PostConstruct
    public void doNotice() {
        ThreadPoolManager.addBaseTask(() -> {
            while (true) {
                try {
                    // 如果在下午14:00-16:00，并且到期时间不足24小时
                    if (DateUtil.thisHour(true) > 14 && DateUtil.thisHour(true) < 16) {
                        List<BotUser> botUsers = botUserMapper.selectByExample(new BotUserExample());
                        if (CollectionUtil.isNotEmpty(botUsers)) {
                            // 当前时间到未来二十四小时
                            Date now = new Date();
                            Date future = DateUtil.offsetHour(now, 24);
                            for (BotUser user : botUsers) {
                                if (user.getDeadLineDate().getTime() > now.getTime() &&
                                user.getDeadLineDate().getTime() < future.getTime()) {
                                    if (ENRegType.PERSONNEL.getValue().equals(user.getType())) {
                                        SendMsgUtil.sendMsg(user.getId(), BaseConsts.SystemManager.CONTINUE_TIPS);
                                    }else {
                                        SendMsgUtil.sendGroupMsg(user.getId(), BaseConsts.SystemManager.CONTINUE_TIPS, null);
                                    }
                                }
                            }
                        }
                    }
                    // 每2小时轮询
                    Thread.sleep(1000 * 60 * 60 * 2);
                }catch (Exception e) {
                    log.error("通知服务出现异常", e);
                }
            }
        });
    }

}
