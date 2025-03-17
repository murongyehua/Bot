package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import com.bot.game.dao.entity.BotDrinkRecord;
import com.bot.game.dao.entity.BotDrinkRecordExample;
import com.bot.game.dao.entity.BotUserConfig;
import com.bot.game.dao.entity.BotUserConfigExample;
import com.bot.game.dao.mapper.BotDrinkRecordMapper;
import com.bot.game.dao.mapper.BotUserConfigMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service("drinkHelpServiceImpl")
public class DrinkHelpServiceImpl implements BaseService {

    @Resource
    private BotDrinkRecordMapper drinkRecordMapper;

    @Resource
    private BotUserConfigMapper userConfigMapper;

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        String realId = groupId == null ? token : groupId;
        BotUserConfigExample example = new BotUserConfigExample();
        example.createCriteria().andUserIdEqualTo(realId);
        List<BotUserConfig> configList = userConfigMapper.selectByExample(example);
        switch (reqContent) {
            case BaseConsts.Drink.ACTIVE:
                // 开启
                if (CollectionUtil.isEmpty(configList)) {
                    BotUserConfig botUserConfig = new BotUserConfig();
                    botUserConfig.setId(IdUtil.simpleUUID());
                    botUserConfig.setUserId(realId);
                    botUserConfig.setDrinkSwitch("1");
                    userConfigMapper.insert(botUserConfig);
                }else {
                    BotUserConfig userConfig = configList.get(0);
                    userConfig.setDrinkSwitch("1");
                    userConfigMapper.updateByPrimaryKey(userConfig);
                }
                return new CommonResp(BaseConsts.Drink.ACTIVE_SUCCESS, ENRespType.TEXT.getType());
            case BaseConsts.Drink.CLOSE:
                // 关闭
                if (CollectionUtil.isEmpty(configList)) {
                    BotUserConfig botUserConfig = new BotUserConfig();
                    botUserConfig.setId(IdUtil.simpleUUID());
                    botUserConfig.setUserId(realId);
                    botUserConfig.setDrinkSwitch("0");
                    userConfigMapper.insert(botUserConfig);
                }else {
                    BotUserConfig userConfig = configList.get(0);
                    userConfig.setDrinkSwitch("0");
                    userConfigMapper.updateByPrimaryKey(userConfig);
                }
                return new CommonResp(BaseConsts.Drink.CLOSE_SUCCESS, ENRespType.TEXT.getType());
            case BaseConsts.Drink.RECORD:
                // 查询
                if (CollectionUtil.isEmpty(configList) || ObjectUtil.notEqual(configList.get(0).getDrinkSwitch(), "1")) {
                    return new CommonResp(BaseConsts.Drink.ACTIVE_TIP, ENRespType.TEXT.getType());
                }
                BotDrinkRecordExample drinkRecordExample = new BotDrinkRecordExample();
                drinkRecordExample.createCriteria().andUserIdEqualTo(token)
                        .andDrinkTimeBetween(
                        DateUtil.format(DateUtil.beginOfDay(new Date()), DatePattern.NORM_DATETIME_FORMAT),
                        DateUtil.format(DateUtil.endOfDay(new Date()), DatePattern.NORM_DATETIME_FORMAT)
                );
                List<BotDrinkRecord> recordList = drinkRecordMapper.selectByExample(drinkRecordExample);
                if (CollectionUtil.isEmpty(recordList)) {
                    return new CommonResp(BaseConsts.Drink.NO_DATA_TIP, ENRespType.TEXT.getType());
                }
                StringBuilder stringBuilder = new StringBuilder();
                AtomicInteger all = new AtomicInteger();
                recordList.forEach(x -> {
                    stringBuilder.append(String.format(BaseConsts.Drink.QUERY_RECORD, x.getDrinkTime().split(StrUtil.SPACE)[1], x.getDrinkNumber())).append(StrUtil.CRLF);
                    all.addAndGet(x.getDrinkNumber());
                });
                BigDecimal result = new BigDecimal(all.get()).divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
                stringBuilder.append(StrUtil.CRLF).append(String.format(BaseConsts.Drink.QUERY_ALL, recordList.size(), result));
                return new CommonResp(stringBuilder.toString(), ENRespType.TEXT.getType());
            default:
                // 没开启的话不允许记录
                if (CollectionUtil.isEmpty(configList) || ObjectUtil.notEqual(configList.get(0).getDrinkSwitch(), "1")) {
                    return new CommonResp(BaseConsts.Drink.ACTIVE_TIP, ENRespType.TEXT.getType());
                }
                // 记录
                String[] reqStrs = reqContent.split(StrUtil.SPACE);
                if (reqStrs.length != 2) {
                    // 不能正常出发，直接返回null，外面再判断是不是正常聊天以及要不要回复
                    return null;
                }
                int ml = 0;
                try {
                    ml = Integer.parseInt(reqStrs[1]);
                }catch (Exception e) {
                    return new CommonResp("请填写正确的摄水量。", ENRespType.TEXT.getType());
                }
                if (ml > 1000 || ml < 1) {
                    return new CommonResp("单次仅支持记录大于1小于1000的摄水量，请重新记录。", ENRespType.TEXT.getType());
                }
                BotDrinkRecord botDrinkRecord = new BotDrinkRecord();
                botDrinkRecord.setDrinkTime(DateUtil.now());
                botDrinkRecord.setGroupId(groupId);
                botDrinkRecord.setId(IdUtil.simpleUUID());
                botDrinkRecord.setDrinkNumber(ml);
                botDrinkRecord.setUserId(token);
                drinkRecordMapper.insert(botDrinkRecord);
                return new CommonResp(BaseConsts.Drink.RECORD_SUCCESS, ENRespType.TEXT.getType());
        }
    }
}
