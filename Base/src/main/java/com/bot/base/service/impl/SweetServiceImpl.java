package com.bot.base.service.impl;

import com.bot.base.dto.CommonResp;
import com.bot.base.dto.SpeechIdDTO;
import com.bot.base.service.BaseService;
import com.bot.common.enums.ENRespType;
import com.bot.common.util.HttpSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 土味情话服务
 * @author murongyehua
 * @version 1.0 2020/10/9
 */
@Service("sweetServiceImpl")
public class SweetServiceImpl implements BaseService {

    @Value("${sweet.url}")
    private String url;

    @Resource(name = "defaultChatServiceImpl")
    private BaseService defaultChatService;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        String result = HttpSenderUtil.get(url, null);
        // 如果在语音，这里用按读一下处理，语音步数+1
        SpeechIdDTO speechIdDTO = DefaultChatServiceImpl.TOKEN_2_SPEECH_ID_MAP.get(groupId == null ? token : groupId);
        if (speechIdDTO != null) {
            // 步数+1
            speechIdDTO.setStep(speechIdDTO.getStep() + 1);
            DefaultChatServiceImpl.TOKEN_2_SPEECH_ID_MAP.put(groupId == null ? token : groupId, speechIdDTO);
            return defaultChatService.doQueryReturn("读一下" + result, token, groupId, null);
        }
        return new CommonResp(result, ENRespType.TEXT.getType());
    }

}
