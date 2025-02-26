package com.bot.base.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRespType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 运势占卜服务
 * @author murongyehua
 * @version 1.0 2020/9/27
 */
@Service("luckServiceImpl")
public class LuckServiceImpl implements BaseService {

    public static Map<String, String> luckCacheMap = new HashMap<>();

    @Override
    public CommonResp doQueryReturn(String reqContent, String token, String groupId) {
        String key = token + StrUtil.UNDERLINE + DateUtil.today();
        String resp = luckCacheMap.computeIfAbsent(key, k -> this.getLuck());
        return new CommonResp(BaseConsts.Luck.TEXT_COMMON + resp, ENRespType.TEXT.getType());
    }

    private String getLuck() {
        int one = this.getLuckNum();
        int two = this.getLuckNum();
        int three = this.getLuckNum();
        if (isBest(one, two, three)) {
            return BaseConsts.Luck.TEXT_BEST;
        }
        if (isGood(one, two, three)) {
            return BaseConsts.Luck.TEXT_GOOD;
        }
        if (isRight(one, two, three)) {
            return BaseConsts.Luck.TEXT_RIGHT;
        }
        if (isBad(one, two, three)) {
            return BaseConsts.Luck.TEXT_BAD;
        }
        if (isTerrible(one, two, three)) {
            return BaseConsts.Luck.TEXT_TERRIBLE;
        }
        return BaseConsts.Luck.TEXT_NORMAL;
    }

    private boolean isBest(int one, int two, int three) {
        return isNumberBetween(one, 1, 20) && isNumberBetween(two, 1, 20) && isNumberBetween(three, 1, 20);
    }

    private boolean isGood(int one, int two, int three) {
        return hasTwoRight(isNumberBetween(one, 1, 20), isNumberBetween(two, 1, 20), isNumberBetween(three, 1, 20)) &&
                this.noBad(one, two, three);
    }

    private boolean isRight(int one, int two, int three) {
        return hasOneRight(isNumberBetween(one, 1, 20), isNumberBetween(two, 1, 20), isNumberBetween(three, 1, 20)) &&
                this.noBad(one, two, three);
    }

    private boolean isBad(int one, int two, int three) {
        return hasOneRight(isNumberBetween(one, 80, 100), isNumberBetween(two, 80, 100), isNumberBetween(three, 80, 100)) &&
                this.noGood(one, two, three);
    }

    private boolean isTerrible(int one, int two, int three) {
        return hasTwoRight(isNumberBetween(one, 80, 100), isNumberBetween(two, 80, 100), isNumberBetween(three, 80, 100)) &&
                this.noGood(one, two, three);
    }

    private int getLuckNum() {
        return RandomUtil.randomInt(0,100);
    }

    private boolean isNumberBetween(int number, int min, int max) {
        return number < max && number > min;
    }

    private boolean hasTwoRight(boolean boo1, boolean boo2, boolean boo3) {
        return (boo1 && boo2 && !boo3) || (boo1 && !boo2 && boo3) || (!boo1 && boo2 && boo3);
    }

    private boolean hasOneRight(boolean boo1, boolean boo2, boolean boo3) {
        return (boo1 && !boo2 && !boo3) || (!boo1 && boo2 && !boo3) || (!boo1 && !boo2 && boo3);
    }

    private boolean noBad(int one, int two, int three) {
        return !isNumberBetween(one, 80, 100) && !isNumberBetween(two, 80, 100) && !isNumberBetween(three, 80, 100);
    }

    private boolean noGood(int one, int two, int three) {
        return !isNumberBetween(one, 1, 20) && !isNumberBetween(two, 1, 20) && !isNumberBetween(three, 1, 20);
    }
}
