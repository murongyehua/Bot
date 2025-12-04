package com.bot.base.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.EmojiDistributor;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.util.SendMsgUtil;
import com.bot.game.dao.entity.BotEmoji;
import com.bot.game.dao.entity.BotEmojiExample;
import com.bot.game.dao.mapper.BotEmojiMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class EmojiDistributorImpl implements EmojiDistributor {

    @Resource
    private BotEmojiMapper emojiMapper;

    private static Long LAST_FETCH_TIME = 0L;

    @Override
    public CommonResp dealEmoji(String md5, int length, Long msgId, String token, String groupId) {
        // 获取开关
        if (!SystemConfigCache.emojiUser.contains(groupId != null ? groupId : token)) {
            // 未开启 不予回复 直接返回
            return null;
        }
        // 先尝试保存
        this.insertFilter(md5, length);
        // 有80%的概率不予回复
        if (Math.random() < 0.8) {
            return null;
        }
        // 距离上次获取已经超过5min
//        if (System.currentTimeMillis() - LAST_FETCH_TIME > 5 * 60 * 1000) {
//            // 随机50%的可能性
//            if (Math.random() < 0.5) {
//                String pic = this.getEmojiPic();
//                if (pic != null) {
//                    SendMsgUtil.sendImg(groupId != null ? groupId : token, pic);
//                    return null;
//                }
//            }
//        }
        // 从数据库随机一个发送
        BotEmojiExample example = new BotEmojiExample();
        List<BotEmoji> emojis = emojiMapper.selectByExample(example);
        if (emojis.size() > 0) {
            BotEmoji emoji = emojis.get((int) (Math.random() * emojis.size()));
            // 判断md5是以http开头的则按图片发送
            if (emoji.getMd5().startsWith("http")) {
                SendMsgUtil.sendImg(groupId != null ? groupId : token, emoji.getMd5());
                return null;
            }
            SendMsgUtil.sendEmoji(groupId != null ? groupId : token, emoji.getMd5(), Integer.parseInt(emoji.getImgsize()));
            return null;
        }
        return null;
    }

    private void insertFilter(String md5, int length) {
        BotEmojiExample example = new BotEmojiExample();
        example.createCriteria().andMd5EqualTo(md5);
        if (emojiMapper.countByExample(example) == 0) {
            BotEmoji emoji = new BotEmoji();
            emoji.setMd5(md5);
            emoji.setImgsize(String.valueOf(length));
            emoji.setCreateDate(DateUtil.now());
            emojiMapper.insertSelective(emoji);
        }
    }

    private String getEmojiPic() {
        // 获取表情并存到数据库 此接口每5分钟只能调一次
        try {
            log.info("获取在线表情包");
            String result = HttpUtil.get("https://cn.apihz.cn/api/img/apihzbqb.php?id=88888888&key=88888888&type=1&limit=10");
            // 更新LAST_FETCH_TIME为当前时间毫秒数
            LAST_FETCH_TIME = System.currentTimeMillis();
            log.info("获取结果{}", result);
            JSONObject jsonObject = JSONUtil.parseObj(result);
            Integer code = jsonObject.getInt("code");
            if (code == 200) {
                JSONArray data = jsonObject.getJSONArray("res");
                for (int i = 0; i < data.size(); i++) {
                    String json = data.getStr(i);
                    insertFilter(json, 0);
                }
                return data.getStr(0);
            }
            return null;
        }catch (Exception e) {
            log.info("获取在线表情包出现异常", e);
            return null;
        }

    }

}
