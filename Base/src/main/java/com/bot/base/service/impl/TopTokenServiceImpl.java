package com.bot.base.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.*;
import com.bot.base.service.BaseService;
import com.bot.base.util.CmmsUtil;
import com.bot.common.enums.ENGridKeyWord;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("topTokenServiceImpl")
public class TopTokenServiceImpl implements BaseService {

    @Value("${grid.tool.url}")
    private String url;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        String[] reqs = null;
        ENGridKeyWord target = null;
        for (ENGridKeyWord enGridKeyWord : ENGridKeyWord.values()) {
            if (reqContent.startsWith(enGridKeyWord.getPrefix())) {
                target = enGridKeyWord;
                reqs = reqContent.split(StrUtil.SPACE);
                break;
            }
        }
        if (reqs == null) {
            return new CommonResp("当前模式仅支持工作指令，不支持其他功能，请检查指令是否正确。", ENRespType.TEXT.getType());
        }
        switch (target) {
            case REFUND:
                if (reqs.length != 2) {
                    return new CommonResp("退款指令格式错误，请检查指令是否正确。", ENRespType.TEXT.getType());
                }
                try {
                    String response = CmmsUtil.refund(reqs[1]);
                    return new CommonResp("操作结果：" + response, ENRespType.TEXT.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new CommonResp("退款出现异常，请联系管理员。", ENRespType.TEXT.getType());
            case BIND_ACCOUNT:
                if (reqs.length != 3) {
                    return new CommonResp("绑定大唐工号指令格式错误，请检查指令是否正确。", ENRespType.TEXT.getType());
                }
                try {
                    String response = HttpSenderUtil.postJsonData(url + target.getUrl(), JSONUtil.toJsonStr(new GridBindReq(reqs[1], reqs[2])));
                    String code = new JSONObject(response).getStr("code");
                    if ("000000".equals(code)) {
                        return new CommonResp("操作成功。", ENRespType.TEXT.getType());
                    }
                    return new CommonResp("操作失败，原因：" + new JSONObject(response).getStr("msg"), ENRespType.TEXT.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new CommonResp("绑定大唐工号出现异常，请联系管理员。", ENRespType.TEXT.getType());
            case CHECK_ACCOUNT:
                if (reqs.length != 2) {
                    return new CommonResp("检查工号指令格式错误，请检查指令是否正确。", ENRespType.TEXT.getType());
                }
                try {
                    String response = HttpSenderUtil.postJsonData(url + target.getUrl(), JSONUtil.toJsonStr(new GridCheckAccountReq(reqs[1])));
                    String code = new JSONObject(response).getStr("code");
                    if ("000000".equals(code)) {
                        return new CommonResp(new JSONObject(response).getStr("data"), ENRespType.TEXT.getType());
                    }
                    return new CommonResp("操作失败，原因：" + new JSONObject(response).getStr("msg"), ENRespType.TEXT.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new CommonResp("检查工号出现异常，请联系管理员。", ENRespType.TEXT.getType());
            case FETCH_BOSS_LOG:
                if (reqs.length != 2 && reqs.length != 3) {
                    return new CommonResp("检查工号指令格式错误，请检查指令是否正确。", ENRespType.TEXT.getType());
                }
                try {
                    String response = HttpSenderUtil.postJsonData(url + target.getUrl(),
                            JSONUtil.toJsonStr(reqs.length == 2 ? new GridFetchBossLogReq(reqs[1], null) : new GridFetchBossLogReq(reqs[1], reqs[2])));
                    String code = new JSONObject(response).getStr("code");
                    if ("000000".equals(code)) {
                        return new CommonResp(new JSONObject(response).getStr("data"), ENRespType.TEXT.getType());
                    }
                    return new CommonResp(new JSONObject(response).getStr("msg"), ENRespType.TEXT.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            case QUERY_ORDER:
                if (reqs.length != 2) {
                    return new CommonResp("查询订单指令格式错误，请检查指令是否正确。", ENRespType.TEXT.getType());
                }
                try {
                    String response = HttpSenderUtil.postJsonData(url + target.getUrl(),
                            JSONUtil.toJsonStr(new OrderQueryReq(reqs[1])));
                    String code = new JSONObject(response).getStr("code");
                    if ("000000".equals(code)) {
                        return new CommonResp(new JSONObject(response).getStr("data"), ENRespType.TEXT.getType());
                    }
                    return new CommonResp("查询失败，原因：" + new JSONObject(response).getStr("msg"), ENRespType.TEXT.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            case QUERY_SMS:
                if (reqs.length != 2) {
                    return new CommonResp("查询验证码指令格式错误，请检查指令是否正确。", ENRespType.TEXT.getType());
                }
                try {
                    String response = HttpSenderUtil.postJsonData(url + target.getUrl(),
                            JSONUtil.toJsonStr(new SmsQueryReq(reqs[1])));
                    String code = new JSONObject(response).getStr("code");
                    if ("000000".equals(code)) {
                        return new CommonResp(new JSONObject(response).getStr("data"), ENRespType.TEXT.getType());
                    }
                    return new CommonResp("查询失败，原因：" + new JSONObject(response).getStr("msg"), ENRespType.TEXT.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            default:
                return new CommonResp("当前模式仅支持工作指令，不支持其他功能，请检查指令是否正确。", ENRespType.TEXT.getType());
        }
    }

}
