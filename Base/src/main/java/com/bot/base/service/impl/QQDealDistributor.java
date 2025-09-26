package com.bot.base.service.impl;

import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.RegService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
public class QQDealDistributor {

    @Resource
    private ActivityServiceImpl activityService;

    @Resource
    private UserBoxServiceImpl userBoxService;

    @Resource
    private SignServiceImpl signService;

    @Resource
    private RegService regService;

    public CommonResp req2Resp(String reqContent, String token, String groupId, String channel) {
        // 签到资格token 优先走专属逻辑 如果没有匹配上再用后续逻辑
        if (SystemConfigCache.signToken.contains(groupId == null ? token : groupId)) {
            CommonResp resp = signService.doQueryReturn(reqContent, token, groupId, channel);
            if (resp != null) {
                return resp;
            }
        }
        // 获取token
        if (BaseConsts.SystemManager.GET_TOKEN.equals(reqContent)) {
            return new CommonResp(token, ENRespType.TEXT.getType());
        }
        // 过一遍系统级指令
        CommonResp userResp = userBoxService.doQueryReturn(reqContent, token, groupId, channel);
        if (userResp != null) {
            return userResp;
        }
        // 校验资格 qq统一不校验 先过审核
//        String checkResult = this.checkUserStatus(groupId == null ? token : groupId);
//        if (checkResult != null) {
//            return new CommonResp(checkResult, ENRespType.TEXT.getType());
//        }
        // 查询到期时间
        if (reqContent.equals(BaseConsts.SystemManager.QUERY_DEADLINE_DATE)) {
            return new CommonResp(regService.queryDeadLineDate(groupId == null ? token : groupId), ENRespType.TEXT.getType());
        }
        // 剑三的功能 如果没有前缀就加上
        if (!reqContent.startsWith(BaseConsts.Activity.ACTIVITY_JX3)) {
            reqContent = "剑三 " + reqContent;
        }
        return activityService.doQueryReturn(reqContent, token, groupId, channel);
    }

    private String checkUserStatus(String activeId) {
        Date deadLineDate = SystemConfigCache.userDateMap.get(activeId);
        if (deadLineDate == null || deadLineDate.before(new Date())) {
            return BaseConsts.SystemManager.OVER_TIME_TIP;
        }
        return null;
    }

}
