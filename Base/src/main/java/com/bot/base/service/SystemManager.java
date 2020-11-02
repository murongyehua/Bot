package com.bot.base.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.UserTempInfoDTO;
import com.bot.base.service.impl.VoteServiceImpl;
import com.bot.commom.constant.BaseConsts;
import com.bot.game.service.GameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

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
        // 目前只支持刷新文本
        if (BaseConsts.SystemManager.MANAGER_CODE_RELOAD_TEXT.equals(reqContent)) {
            userTempInfo.setOutTime(DateUtil.offset(new Date(), DateField.MINUTE, 1));
            commonTextLoader.loadText();
            return BaseConsts.SystemManager.SUCCESS;
        }
        // 清空投票
        if (BaseConsts.SystemManager.CLEAR_VOTE.equals(reqContent)) {
            VoteServiceImpl.votes = new HashMap<>();
            VoteServiceImpl.voted = new LinkedList<>();
            return BaseConsts.SystemManager.SUCCESS;
        }
        // 查看票数
        if (BaseConsts.SystemManager.LOOK_VOTE.equals(reqContent)) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String name : VoteServiceImpl.votes.keySet()) {
                stringBuilder.append(name).append(":").append(VoteServiceImpl.votes.get(name)).append(StrUtil.CRLF);
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

}
