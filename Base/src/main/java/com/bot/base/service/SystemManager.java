package com.bot.base.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.UserTempInfoDTO;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.dto.ActivityAwardDTO;
import com.bot.common.enums.ENRegDay;
import com.bot.common.enums.ENYesOrNo;
import com.bot.common.loader.CommonTextLoader;
import com.bot.common.util.SendMsgUtil;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.BotActivityAwardMapper;
import com.bot.game.dao.mapper.BotActivityMapper;
import com.bot.game.dao.mapper.BotAwardMapper;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import com.bot.game.service.GameHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 系统管理
 *
 * @author murongyehua
 * @version 1.0 2020/9/28
 */
@Slf4j
@Service
public class SystemManager {

    /**
     * 当前操作用户，同一时间只允许一个人进行系统管理
     */
    public static volatile UserTempInfoDTO userTempInfo = null;

    /**
     * 公告模式
     */
    public static volatile String noticeModel = null;

    /**
     * 小程序模式
     */
    public static volatile String appletModel = null;

    @Value("${system.manager.password}")
    private String managerPassword;

    @Autowired
    private CommonTextLoader commonTextLoader;

    @Autowired
    private GameHandler gameHandler;

    @Resource
    private BotActivityMapper botActivityMapper;

    @Resource
    private BotActivityAwardMapper activityAwardMapper;

    @Resource
    private BotAwardMapper awardMapper;

    @Value("${notice.path}")
    private String picPath;

    @Resource
    private BotUserConfigMapper userConfigMapper;

    /**
     * 尝试进入管理模式
     *
     * @param token
     * @return
     */
    public static String tryIntoManager(String token) {
        if (userTempInfo == null) {
            userTempInfo = new UserTempInfoDTO(token);
            return BaseConsts.SystemManager.TRY_INTO_MANAGER_SUCCESS;
        }
        if (userTempInfo.getToken().equals(token)) {
            return BaseConsts.SystemManager.TRY_INTO_MANAGER_REPEAT;
        }
        return BaseConsts.SystemManager.TRY_INTO_MANAGER_FAIL;
    }

    /**
     * 管理模式分发指令
     *
     * @param reqContent
     * @return
     */
    public String managerDistribute(String reqContent) {
        if (!userTempInfo.getActive()) {
            if (reqContent.equals(managerPassword)) {
                // 密码正确
                userTempInfo.setActive(true);
                userTempInfo.setOutTime(DateUtil.offset(new Date(), DateField.MINUTE, 1));
                return BaseConsts.SystemManager.MANAGER_PASSWORD_RIGHT;
            }
            // 密码错误
            userTempInfo = null;
            return BaseConsts.SystemManager.MANAGER_PASSWORD_ERROR;
        }
        // 处于公告模式则直接发送
        if ("1".equals(noticeModel)) {
            send2AllUser(reqContent);
            noticeModel = null;
            return BaseConsts.SystemManager.SUCCESS;
        }
        // 退出管理模式
        if (BaseConsts.SystemManager.TRY_OUT_MANAGER_INFO.equals(reqContent)) {
            userTempInfo = null;
            return BaseConsts.SystemManager.SUCCESS;
        }
        // 刷新文本
        if (BaseConsts.SystemManager.MANAGER_CODE_RELOAD_TEXT.equals(reqContent)) {
            userTempInfo.setOutTime(DateUtil.offset(new Date(), DateField.MINUTE, 1));
            commonTextLoader.loadText();
            return BaseConsts.SystemManager.SUCCESS;
        }
        // 发布公告
        if (reqContent.equals(BaseConsts.SystemManager.SEND_NOTICE_FORMAT)) {
            noticeModel = "1";
            return "请发送公告内容";
        }
        // 发布图片公告
        if (reqContent.startsWith(BaseConsts.SystemManager.PIC_SEND_NOTICE_FORMAT)) {
            String[] contentArr = reqContent.split(StrUtil.SPACE);
            if (contentArr.length != 2) {
                return BaseConsts.SystemManager.ILL_CODE;
            }
            String noticePicName = contentArr[1];
            send2AllUserPic(noticePicName);
            return BaseConsts.SystemManager.SUCCESS;
        }
        // 发送朋友圈
        if (reqContent.startsWith(BaseConsts.SystemManager.PUSH_FRIEND_FORMAT)) {
            String[] contentArr = reqContent.split(StrUtil.SPACE);
            if (contentArr.length != 3) {
                return BaseConsts.SystemManager.ILL_CODE;
            }
            String content = contentArr[1];
            String picNames = contentArr[2];
            // 多个图片用#分隔
            StringBuilder pics = new StringBuilder();
            if (picNames.contains("#")) {
                String[] picArray = picNames.split("#");
                for (String pic : picArray) {
                    pics.append(picPath).append(pic).append(";");
                }
            }else {
                // 为了最后能一起裁剪字符串，这里也加个分号
                pics.append(picPath).append(picNames).append(";");
            }
            SendMsgUtil.snsSendImage(content, pics.substring(0, pics.length() - 1));
            return BaseConsts.SystemManager.SUCCESS;
        }
        // 生成邀请码
        if (reqContent.startsWith(BaseConsts.SystemManager.CREATE_INVITE_CODE)) {
            // 生成邀请码 类型 数量
            String[] contentArr = reqContent.split(StrUtil.SPACE);
            if (contentArr.length != 3) {
                return BaseConsts.SystemManager.ILL_CODE;
            }
            ENRegDay enRegDay = ENRegDay.getRegDayByType(contentArr[1]);
            if (enRegDay == null) {
                return BaseConsts.SystemManager.ILL_CODE;
            }
            if (!NumberUtil.isNumber(contentArr[2])) {
                return BaseConsts.SystemManager.ILL_CODE;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int index = 0; index < Integer.parseInt(contentArr[2]); index++) {
                String inviteCode = IdUtil.nanoId(8);
                SystemConfigCache.tempInviteCode.put(inviteCode, enRegDay);
                stringBuilder.append(inviteCode).append(StrUtil.CRLF);
            }
            return stringBuilder.toString();
        }
        // 游戏管理
        if (reqContent.startsWith(BaseConsts.SystemManager.GAME_MANAGER)) {
            return gameHandler.manage(reqContent.substring(2));
        }
        // 抽奖管理
        if (reqContent.startsWith(BaseConsts.Activity.ACTIVITY_JX3)) {
            String[] reqs = reqContent.split(StrUtil.SPACE);
            if (reqs.length != 3) {
                return BaseConsts.SystemManager.UN_KNOW_MANAGER_CODE;
            }
            // 开始
            if (reqs[1].equals(BaseConsts.Activity.ACTIVITY_START)) {
                if (CollectionUtil.isNotEmpty(SystemConfigCache.activityAwardList)) {
                    return "当前抽奖未结束，请先结束。";
                }
                BotActivity botActivity = new BotActivity();
                botActivity.setStatus(ENYesOrNo.YES.getValue());
                BotActivityExample example = new BotActivityExample();
                example.createCriteria().andNameEqualTo(reqs[2]);
                int resultCount = botActivityMapper.updateByExampleSelective(botActivity, example);
                if (resultCount == 1) {
                    // 开启成功的情况下，更新奖品缓存
                    BotActivity activity = botActivityMapper.selectByExample(example).get(0);
                    BotActivityAwardExample activityAwardExample = new BotActivityAwardExample();
                    activityAwardExample.createCriteria().andActivityIdEqualTo(activity.getId());
                    List<BotActivityAward> activityAwards = activityAwardMapper.selectByExample(activityAwardExample);
                    // 转缓存实体
                    Map<String, String> botAwardMap = awardMapper.selectByExample(new BotAwardExample()).stream().collect(Collectors.toMap(BotAward::getId, BotAward::getName));
                    SystemConfigCache.activityAwardList.addAll(activityAwards.stream().map(x -> {
                        ActivityAwardDTO activityAwardDTO = new ActivityAwardDTO();
                        activityAwardDTO.setId(x.getId());
                        activityAwardDTO.setActivityId(x.getActivityId());
                        activityAwardDTO.setPercent(x.getPercent());
                        activityAwardDTO.setPrefix(x.getPrefix());
                        activityAwardDTO.setType(x.getType());
                        activityAwardDTO.setAwardName(botAwardMap.get(x.getAwardId()));
                        activityAwardDTO.setNumber(Integer.parseInt(x.getNumber()));
                        return activityAwardDTO;
                    }).collect(Collectors.toList()));
                    return BaseConsts.SystemManager.SUCCESS;
                } else {
                    return "开启条数不为1，可能存在问题，请检查数据“;                            ";
                }
            }
            // 结束
            if (reqs[1].equals(BaseConsts.Activity.ACTIVITY_FINISH)) {
                BotActivity botActivity = new BotActivity();
                botActivity.setStatus(ENYesOrNo.NO.getValue());
                BotActivityExample example = new BotActivityExample();
                example.createCriteria().andNameEqualTo(reqs[2]);
                int resultCount = botActivityMapper.updateByExampleSelective(botActivity, example);
                if (resultCount == 1) {
                    SystemConfigCache.activityAwardList.clear();
                    return BaseConsts.SystemManager.SUCCESS;
                } else {
                    return "结束条数不为1，可能存在问题，请检查数据“;                            ";
                }
            }
        }
        // 手动发送日报
        if (reqContent.startsWith(BaseConsts.SystemManager.SEND_DAILY)) {
            Map<String, String> userWorkDailySendMap = userConfigMapper.selectByExample(new BotUserConfigExample()).stream().filter(botUserConfig -> StrUtil.isNotBlank(botUserConfig.getWorkDailyConfig())).collect(Collectors.toMap(BotUserConfig::getUserId, BotUserConfig::getWorkDailyConfig));
            String today = DateUtil.today();
            for(String token : SystemConfigCache.userWorkDaily) {
                try {
                    String lastSendDate = userWorkDailySendMap.get(token);
                    if (ObjectUtil.notEqual(today, lastSendDate)) {
                        // 当日没有发送 执行发送
                        for (int index=1;index<6;index++) {
                            SendMsgUtil.sendImg(token, picPath + "/daily/" + DateUtil.today() + "/" + index + ".png");
                        }
                    }
                    // 更新最后发送日期
                    BotUserConfig botUserConfig = new BotUserConfig();
                    botUserConfig.setWorkDailyConfig(today);
                    BotUserConfigExample example = new BotUserConfigExample();
                    example.createCriteria().andUserIdEqualTo(token);
                    userConfigMapper.updateByExampleSelective(botUserConfig, example);
                }catch (Exception e) {
                    // do nothing 不做任何处理 一个人推送异常不影响其他人接收
                    log.error("摸鱼日报发送失败！！！", e);
                }
            }
            return BaseConsts.SystemManager.SUCCESS;
        }
        if (reqContent.startsWith("小程序")) {
            appletModel = "1";
            return "请发送小程序消息";
        }
        userTempInfo.setOutTime(DateUtil.offset(new Date(), DateField.MINUTE, 1));
        return BaseConsts.SystemManager.UN_KNOW_MANAGER_CODE;
    }

    private void send2AllUser(String content) {
        for (String token : SystemConfigCache.userDateMap.keySet()) {
            if (token.contains("@chatroom")) {
                SendMsgUtil.sendGroupMsg(token, content, null);
                continue;
            }
            SendMsgUtil.sendMsg(token, content);
        }
    }

    private void send2AllUserPic(String fileName) {
        for (String token : SystemConfigCache.userDateMap.keySet()) {
            SendMsgUtil.sendImg(token, picPath + fileName);
        }
    }

}
