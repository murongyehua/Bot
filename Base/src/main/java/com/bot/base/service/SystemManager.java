package com.bot.base.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.UserTempInfoDTO;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRegDay;
import com.bot.common.loader.CommonTextLoader;
import com.bot.common.util.SendMsgUtil;
import com.bot.game.service.GameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 系统管理
 * @author murongyehua
 * @version 1.0 2020/9/28
 */
@Service
public class SystemManager {

    /**
     * 当前操作用户，同一时间只允许一个人进行系统管理
     */
    public static volatile UserTempInfoDTO userTempInfo = null;

    @Value("${system.manager.password}")
    private String managerPassword;

    @Autowired
    private CommonTextLoader commonTextLoader;

    @Autowired
    private GameHandler gameHandler;

    /**
     * 尝试进入管理模式
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
        if (reqContent.startsWith(BaseConsts.SystemManager.SEND_NOTICE_FORMAT)) {
            String[] contentArr = reqContent.split(StrUtil.SPACE);
            if (contentArr.length != 2) {
                return BaseConsts.SystemManager.ILL_CODE;
            }
            String noticeContent = contentArr[1];
            send2AllUser(noticeContent);
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
        userTempInfo.setOutTime(DateUtil.offset(new Date(), DateField.MINUTE, 1));
        return BaseConsts.SystemManager.UN_KNOW_MANAGER_CODE;
    }

    private void send2AllUser(String content) {
        for(String token : SystemConfigCache.userDateMap.keySet()) {
            if (token.contains("@chatroom")) {
                SendMsgUtil.sendGroupMsg(token, content, null);
                continue;
            }
            SendMsgUtil.sendMsg(token, content);
        }
    }

}
