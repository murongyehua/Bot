package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.CommonResp;
import com.bot.base.service.BaseService;
import com.bot.common.config.SystemConfigCache;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENRegStatus;
import com.bot.common.enums.ENRegType;
import com.bot.common.enums.ENRespType;
import com.bot.common.enums.ENUserGoodType;
import com.bot.common.enums.ENWordRarity;
import com.bot.common.util.SendMsgUtil;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.*;
import com.bot.game.service.SystemConfigHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("userBoxServiceImpl")
public class UserBoxServiceImpl implements BaseService {

    @Resource
    private BotUserBoxMapper userBoxMapper;

    @Resource
    private BotUserConfigMapper userConfigMapper;

    @Resource
    private BotUserMapper userMapper;

    @Resource
    private SystemConfigHolder systemConfigHolder;

    @Value("${sign.group.url}")
    private String signGroup;

    @Value("${add.bot.url}")
    private String addBot;

    @Resource
    private UserBindMapper userBindMapper;

    @Resource
    private BotGameUserScoreMapper gameUserScoreMapper;

    @Resource
    private BotUserSignMapper userSignMapper;

    @Resource
    private BotBaseWordMapper baseWordMapper;

    @Resource
    private BotUserWordMapper userWordMapper;

    @Resource
    private BotUserBlindBoxMapper userBlindBoxMapper;

    // 用于维护用户等待查看词条详情的状态
    private static final Map<String, List<BotUserWord>> USER_WORD_VIEW_CONTEXT = new HashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResp doQueryReturn(String reqContent, String token, String groupId, String channel) {
        if (ObjectUtil.equals("签到", reqContent)) {
            if (groupId == null) {
                return new CommonResp("只有在群聊内可以签到~", ENRespType.TEXT.getType());
            }
            BotUserSignExample signExample = new BotUserSignExample();
            signExample.createCriteria().andSignDateEqualTo(DateUtil.today()).andUserIdEqualTo(token);
            int todayCount = userSignMapper.countByExample(signExample);
            if (todayCount > 0) {
                return new CommonResp(BaseConsts.Sign.SIGN_FAIL, ENRespType.TEXT.getType());
            }
            // 插入签到记录
            BotUserSign userSign = new BotUserSign();
            userSign.setId(IdUtil.simpleUUID());
            userSign.setSignDate(DateUtil.today());
            userSign.setUserId(token);
            userSignMapper.insert(userSign);
            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
            scoreExample.createCriteria().andUserIdEqualTo(token);
            List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(scoreExample);
            // 随机1、2、3中的一个数字
            int number = (int) (Math.random() * 3 + 1);
            if (CollectionUtil.isEmpty(scores)) {
                // 首次插入
                BotGameUserScore score = new BotGameUserScore();
                score.setUserId(token);
                score.setNickname(SendMsgUtil.getGroupNickName(groupId, token));
                score.setScore(number);
                gameUserScoreMapper.insert(score);
            }else {
                // 更新
                BotGameUserScore score = scores.get(0);
                score.setScore(score.getScore() + number);
                gameUserScoreMapper.updateByPrimaryKey(score);
            }
            String message = this.getRandomMessage();
            String response = String.format("签到成功，积分+%s\r\n\r\n%s", number, message);
            SendMsgUtil.sendGroupMsgForGame(groupId, response, token);
            return new CommonResp(null, ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("签到群", reqContent)) {
            return new CommonResp(signGroup, ENRespType.IMG.getType());
        }
        if (ObjectUtil.equals("引入小林", reqContent) || ObjectUtil.equals("引入", reqContent)) {
            return new CommonResp(addBot, ENRespType.IMG.getType());
        }
        if (ObjectUtil.equals("我的背包", reqContent)) {
            BotUserBoxExample userBoxExample = new BotUserBoxExample();
            // 这里不查碎玉，碎玉根据是否绑定了跨平台账号单独查
            userBoxExample.createCriteria().andUserIdEqualTo(token).andGoodTypeNotEqualTo(ENUserGoodType.MONEY.getValue());
            List<BotUserBox> userBoxList = userBoxMapper.selectByExample(userBoxExample);
            // 查碎玉
            BotUserBox moneyBox = this.getMoney(token, channel);
            if (moneyBox != null) {
                userBoxList.add(moneyBox);
            }
            if (CollectionUtil.isEmpty(userBoxList)) {
                return new CommonResp("你的背包是空的~", ENRespType.TEXT.getType());
            }
            List<String> boxList = new ArrayList<>();
            for (BotUserBox userBox : userBoxList) {
                String boxBuilder = ENUserGoodType.getLabelByValue(userBox.getGoodType()) +
                        " x " + userBox.getNumber();
                boxList.add(boxBuilder);
            }
            return new CommonResp(CollectionUtil.join(boxList, "\r\n"), ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("碎玉商店", reqContent)) {
            if ("wx".equals(channel)) {
                String wxShop = "碎玉商店\r\n个人资格    6碎玉\r\n群聊资格    18碎玉\r\n\r\n发送“兑换”+空格+物品名称可使用碎玉兑换";
                return new CommonResp(wxShop, ENRespType.TEXT.getType());
            }else {
                String qqShop = "碎玉商店\r\n群聊资格    30碎玉\r\n\r\n发送“兑换”+空格+物品名称可使用碎玉兑换";
                return new CommonResp(qqShop, ENRespType.TEXT.getType());
            }

        }
        if (ObjectUtil.equals("兑换 个人资格", reqContent)) {
            if ("qq".equals(channel)) {
                return new CommonResp("QQ暂不支持兑换个人资格，如有需要请至微信使用", ENRespType.TEXT.getType());
            }
            BotUserBox userBox = this.getMoney(token, channel);
            if (userBox == null) {
                return new CommonResp("碎玉不足，兑换失败", ENRespType.TEXT.getType());
            }
            if (userBox.getNumber() < 6) {
                return new CommonResp("碎玉不足，兑换失败", ENRespType.TEXT.getType());
            }
            userBox.setNumber(userBox.getNumber() - 6);
            userBoxMapper.updateByPrimaryKey(userBox);
            BotUserBoxExample userBoxExample = new BotUserBoxExample();
            userBoxExample.createCriteria().andGoodTypeEqualTo(ENUserGoodType.PERSONAL.getValue()).andUserIdEqualTo(token);
            List<BotUserBox> userBoxList1 = userBoxMapper.selectByExample(userBoxExample);
            if (CollectionUtil.isEmpty(userBoxList1)) {
                // 新增
                BotUserBox userBox1 = new BotUserBox();
                userBox1.setUserId(token);
                userBox1.setId(IdUtil.simpleUUID());
                userBox1.setNumber(1);
                userBox1.setGoodType(ENUserGoodType.PERSONAL.getValue());
                userBoxMapper.insert(userBox1);
                return new CommonResp("兑换成功，可到需要使用的聊天界面使用。", ENRespType.TEXT.getType());
            }
            // 修改
            BotUserBox userBox1 = userBoxList1.get(0);
            userBox1.setNumber(userBox1.getNumber() + 1);
            userBoxMapper.updateByPrimaryKey(userBox1);
            return new CommonResp("兑换成功，可到需要使用的聊天界面使用。", ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("兑换 群聊资格", reqContent)) {
            BotUserBox userBox = this.getMoney(token, channel);
            if (userBox == null) {
                return new CommonResp("碎玉不足，兑换失败", ENRespType.TEXT.getType());
            }
            if ("wx".equals(channel)) {
                if (userBox.getNumber() < 18) {
                    return new CommonResp("碎玉不足，兑换失败", ENRespType.TEXT.getType());
                }
                userBox.setNumber(userBox.getNumber() - 18);
            }else {
                if (userBox.getNumber() < 30) {
                    return new CommonResp("碎玉不足，兑换失败", ENRespType.TEXT.getType());
                }
                userBox.setNumber(userBox.getNumber() - 30);
            }
            userBoxMapper.updateByPrimaryKey(userBox);
            BotUserBoxExample userBoxExample = new BotUserBoxExample();
            userBoxExample.clear();
            userBoxExample.createCriteria().andGoodTypeEqualTo(ENUserGoodType.GROUP.getValue()).andUserIdEqualTo(token);
            List<BotUserBox> userBoxList1 = userBoxMapper.selectByExample(userBoxExample);
            if (CollectionUtil.isEmpty(userBoxList1)) {
                // 新增
                BotUserBox userBox1 = new BotUserBox();
                userBox1.setUserId(token);
                userBox1.setId(IdUtil.simpleUUID());
                userBox1.setNumber(1);
                userBox1.setGoodType(ENUserGoodType.GROUP.getValue());
                userBoxMapper.insert(userBox1);
                return new CommonResp("兑换成功，可到需要使用的聊天界面使用。", ENRespType.TEXT.getType());
            }
            // 修改
            BotUserBox userBox1 = userBoxList1.get(0);
            userBox1.setNumber(userBox1.getNumber() + 1);
            userBoxMapper.updateByPrimaryKey(userBox1);
            return new CommonResp("兑换成功，可到需要使用的聊天界面使用。", ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("使用 个人资格", reqContent)) {
            BotUserBoxExample userBoxExample = new BotUserBoxExample();
            userBoxExample.createCriteria().andUserIdEqualTo(token).andGoodTypeEqualTo(ENUserGoodType.PERSONAL.getValue());
            List<BotUserBox> userBoxList = userBoxMapper.selectByExample(userBoxExample);
            if (CollectionUtil.isEmpty(userBoxList)) {
                return new CommonResp("没有可用资格，使用失败", ENRespType.TEXT.getType());
            }
            BotUserBox userBox = userBoxList.get(0);
            if (userBox.getNumber() < 1) {
                return new CommonResp("没有可用资格，使用失败", ENRespType.TEXT.getType());
            }
            if (userBox.getNumber() == 1) {
                userBoxMapper.deleteByPrimaryKey(userBox.getId());
            }else {
                userBox.setNumber(userBox.getNumber() - 1);
                userBoxMapper.updateByPrimaryKey(userBox);
            }
            this.reg(token, ENRegType.PERSONNEL.getValue());
            return new CommonResp("使用成功，有效期延长30天！可以发送“到期时间”查询~", ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("使用 群聊资格", reqContent)) {
            log.info("1---" + token + "----" + groupId + "----" + channel);
            if (groupId == null) {
                return new CommonResp("请在需要使用群聊资格的群内进行操作", ENRespType.TEXT.getType());
            }
            BotUserBoxExample userBoxExample = new BotUserBoxExample();
            userBoxExample.createCriteria().andUserIdEqualTo(token).andGoodTypeEqualTo(ENUserGoodType.GROUP.getValue());
            List<BotUserBox> userBoxList = userBoxMapper.selectByExample(userBoxExample);
            log.info("2---" + token + "----" + groupId + "----" + channel);
            if (CollectionUtil.isEmpty(userBoxList)) {
                return new CommonResp("没有可用资格，使用失败", ENRespType.TEXT.getType());
            }
            BotUserBox userBox = userBoxList.get(0);
            if (userBox.getNumber() < 1) {
                return new CommonResp("没有可用资格，使用失败", ENRespType.TEXT.getType());
            }
            log.info("3---" + token + "----" + groupId + "----" + channel);
            if (userBox.getNumber() == 1) {
                userBoxMapper.deleteByPrimaryKey(userBox.getId());
            }else {
                userBox.setNumber(userBox.getNumber() - 1);
                userBoxMapper.updateByPrimaryKey(userBox);
            }
            log.info("4---" + token + "----" + groupId + "----" + channel);
            this.reg(groupId, ENRegType.GROUP.getValue());
            return new CommonResp("使用成功，有效期延长30天！可以发送“到期时间”查询~", ENRespType.TEXT.getType());
        }
        if (ObjectUtil.equals("嘻嘻哈哈乌拉乌拉", reqContent)) {
            if (groupId != null) {
                this.reg(groupId, ENRegType.GROUP.getValue());
            }else {
                this.reg(token, ENRegType.PERSONNEL.getValue());
            }
            return new CommonResp("暗号正确，有效期延长90天！可以发送“到期时间”查询。", ENRespType.TEXT.getType());
        }

        if (reqContent.startsWith("设置回复频率")) {
            String[] split = reqContent.split(StrUtil.SPACE);
            if (split.length != 2) {
                return new CommonResp("格式错误，请按照格式发送“设置回复频率 0-1的两位小数”", ENRespType.TEXT.getType());
            }
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId != null ? groupId : token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setChatFrequency(split[1]);
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("设置成功，回复频率为：" + split[1], ENRespType.TEXT.getType());
        }

        if (reqContent.equals("禁止表情包")) {
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId != null ? groupId : token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setEmojiSwitch("0");
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("设置成功，将不再收集后续的表情包，也不会回复表情包。", ENRespType.TEXT.getType());
        }
        if (reqContent.equals("开启表情包")) {
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId != null ? groupId : token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setEmojiSwitch("1");
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("设置成功，将随机回复表情包，也会收集后续的表情包", ENRespType.TEXT.getType());
        }
        if (reqContent.equals("开启漂流瓶推送")) {
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId != null ? groupId : token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setBottleAutoSwitch("1");
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("开启成功。", ENRespType.TEXT.getType());
        }

        if (reqContent.equals("关闭漂流瓶推送")) {
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId != null ? groupId : token);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setBottleAutoSwitch("0");
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("关闭成功。", ENRespType.TEXT.getType());
        }
        if(reqContent.startsWith("设置欢迎语")) {
            if (groupId == null) {
                return new CommonResp("请在群聊内使用该指令。", ENRespType.TEXT.getType());
            }
            String content = reqContent.replaceFirst("设置欢迎语 ", "");
            BotUserConfigExample userConfigExample = new BotUserConfigExample();
            userConfigExample.createCriteria().andUserIdEqualTo(groupId);
            List<BotUserConfig> userConfigList = userConfigMapper.selectByExample(userConfigExample);
            BotUserConfig userConfig = userConfigList.get(0);
            userConfig.setWelcomeContent(content);
            userConfigMapper.updateByPrimaryKeySelective(userConfig);
            systemConfigHolder.loadUserConfig();
            return new CommonResp("设置成功。", ENRespType.TEXT.getType());
        }
        if (reqContent.equals("开盲盒") || reqContent.startsWith("开盲盒 ")) {
            // 解析次数
            int count = 1;
            if (reqContent.startsWith("开盲盒 ")) {
                try {
                    String countStr = reqContent.substring(4).trim();
                    count = Integer.parseInt(countStr);
                    if (count < 1 || count > 20) {
                        return new CommonResp("开启次数需要在1-20之间~", ENRespType.TEXT.getType());
                    }
                } catch (NumberFormatException e) {
                    return new CommonResp("请输入正确的数字，例如：开盲盒 10", ENRespType.TEXT.getType());
                }
            }
            return handleOpenBlindBox(token, groupId, count);
        }
        if (reqContent.equals("我的词条")) {
            return handleMyWords(token);
        }
        if (reqContent.equals("小林魅力排名")) {
            return handleMeritRank(token);
        }
        if (reqContent.equals("词条库")) {
            return handleWordLibrary(token);
        }
        // 处理词条查看和佩戴
        if (USER_WORD_VIEW_CONTEXT.containsKey(token) || USER_WORD_VIEW_CONTEXT.containsKey(token + "_SELECTED")) {
            // 如果用户发送其他指令，清除词条上下文
            if (reqContent.equals("开盲盒") || reqContent.equals("我的词条") 
                    || reqContent.equals("小林魅力排名") || reqContent.equals("词条库")
                    || reqContent.equals("小林积分排名") || reqContent.equals("取消")) {
                USER_WORD_VIEW_CONTEXT.remove(token);
                USER_WORD_VIEW_CONTEXT.remove(token + "_SELECTED");
                // 如果是取消指令，直接返回
                if (reqContent.equals("取消")) {
                    return new CommonResp("已取消词条操作~", ENRespType.TEXT.getType());
                }
            } else {
                return handleWordOperation(token, reqContent, groupId);
            }
        }
        if (reqContent.equals("小林积分排名")) {
            // 查询所有用户积分
            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
            List<BotGameUserScore> allScores = gameUserScoreMapper.selectByExample(scoreExample);
            
            if (CollectionUtil.isEmpty(allScores)) {
                return new CommonResp("暂无积分数据~", ENRespType.TEXT.getType());
            }
            
            // 在内存中排序（按积分降序）
            List<BotGameUserScore> sortedScores = allScores.stream()
                    .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                    .collect(Collectors.toList());
            
            // 获取前10名
            int topCount = Math.min(10, sortedScores.size());
            List<BotGameUserScore> topTen = sortedScores.subList(0, topCount);
            
            // 构建排行榜消息
            StringBuilder message = new StringBuilder();
            message.append("─────────────\n");
            message.append("🏆 小林积分排行榜\n");
            message.append("─────────────\n\n");
            
            for (int i = 0; i < topTen.size(); i++) {
                BotGameUserScore score = topTen.get(i);
                String displayName = score.getNickname() != null && !score.getNickname().trim().isEmpty() 
                        ? score.getNickname() : score.getUserId();
                
                // 佩戴词条展示：用「」括号紧跟名字
                if (score.getCurrentWord() != null && !score.getCurrentWord().trim().isEmpty()) {
                    displayName = displayName + "「" + score.getCurrentWord() + "」";
                }
                
                // 前三名使用特殊图标
                String icon;
                if (i == 0) {
                    icon = "🥇";
                } else if (i == 1) {
                    icon = "🥈";
                } else if (i == 2) {
                    icon = "🥉";
                } else {
                    icon = String.format("%d.", i + 1);
                }
                
                message.append(String.format("%s %s  %d分\n", icon, displayName, score.getScore()));
            }
            
            // 查询当前用户的积分和排名
            message.append("\n─────────────\n");
            
            // 查找当前用户的积分记录
            BotGameUserScoreExample userExample = new BotGameUserScoreExample();
            userExample.createCriteria().andUserIdEqualTo(token);
            List<BotGameUserScore> userScores = gameUserScoreMapper.selectByExample(userExample);
            
            if (CollectionUtil.isEmpty(userScores)) {
                message.append("📊 我的积分：0，暂无排名");
            } else {
                BotGameUserScore myScore = userScores.get(0);
                // 在内存中计算排名
                int myRank = 1;
                for (BotGameUserScore s : sortedScores) {
                    if (s.getUserId().equals(token)) {
                        break;
                    }
                    myRank++;
                }
                
                message.append(String.format("📊 我的积分：%d，排名：%d", myScore.getScore(), myRank));
            }
            
            return new CommonResp(message.toString(), ENRespType.TEXT.getType());
        }
        return null;
    }

    /**
     * 处理开盲盒指令
     */
    private CommonResp handleOpenBlindBox(String userId, String groupId, int count) {
        try {
            // 1. 查询用户积分
            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
            scoreExample.createCriteria().andUserIdEqualTo(userId);
            List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(scoreExample);
            
            int needScore = count * 2;
            if (CollectionUtil.isEmpty(scores) || scores.get(0).getScore() < needScore) {
                return new CommonResp(String.format("积分不足，开启%d次盲盒需要%d积分~", count, needScore), ENRespType.TEXT.getType());
            }
            
            BotGameUserScore userScore = scores.get(0);
            
            // 2. 检查当日抽取次数（查询今天的记录）
            BotUserBlindBoxExample boxExample = new BotUserBlindBoxExample();
            boxExample.createCriteria()
                    .andUserIdEqualTo(userId)
                    .andFetchDateEqualTo(DateUtil.today());
            int todayCount = userBlindBoxMapper.countByExample(boxExample);
            
            if (todayCount >= 20) {
                return new CommonResp("今日开盲盒次数已达上限(20次)，明天再来吧~", ENRespType.TEXT.getType());
            }
            
            // 检查剩余次数是否足够
            int remainCount = 20 - todayCount;
            if (count > remainCount) {
                return new CommonResp(String.format("今日剩余次数不足，仅剩%d次，请调整开启次数~", remainCount), ENRespType.TEXT.getType());
            }
            
            // 批量开启
            if (count > 1) {
                return handleBatchOpenBlindBox(userId, groupId, count, userScore);
            }
            
            // 3. 扣除2积分
            int originalScore = userScore.getScore();
            userScore.setScore(userScore.getScore() - 2);
            
            // 4. 随机抽取盲盒内容
            double rand = Math.random();
            String boxContent;
            int scoreReward = 0;
            boolean isWord = false;
            
            if (rand < 0.25) {
                // 25% 空
                boxContent = "空";
            } else if (rand < 0.45) {
                // 20% 1积分
                boxContent = "1积分";
                scoreReward = 1;
            } else if (rand < 0.65) {
                // 20% 2积分
                boxContent = "2积分";
                scoreReward = 2;
            } else if (rand < 0.80) {
                // 15% 3积分
                boxContent = "3积分";
                scoreReward = 3;
            } else if (rand < 0.90) {
                // 10% 5积分
                boxContent = "5积分";
                scoreReward = 5;
            } else if (rand < 0.95) {
                // 5% 8积分
                boxContent = "8积分";
                scoreReward = 8;
            } else {
                // 5% 词条
                boxContent = "词条";
                isWord = true;
            }
            
            // 5. 处理积分奖励
            if (scoreReward > 0) {
                userScore.setScore(userScore.getScore() + scoreReward);
                gameUserScoreMapper.updateByPrimaryKey(userScore);
                
                // 记录盲盒记录
                BotUserBlindBox blindBox = new BotUserBlindBox();
                blindBox.setUserId(userId);
                blindBox.setBoxContent(boxContent);
                blindBox.setFetchDate(DateUtil.today());
                userBlindBoxMapper.insert(blindBox);
                
                StringBuilder message = new StringBuilder();
                message.append("━━━━━━━━━━━━\n");
                message.append("✨ 开启盲盒 ✨\n");
                message.append("━━━━━━━━━━━━\n\n");
                message.append(String.format("💸 消耗积分：2\n"));
                message.append(String.format("🎁 恭喜获得：%s\n", boxContent));
                message.append(String.format("💰 当前积分：%d", userScore.getScore()));
                
                return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            }
            
            // 6. 处理空盲盒
            if (!isWord) {
                gameUserScoreMapper.updateByPrimaryKey(userScore);
                
                // 记录盲盒记录
                BotUserBlindBox blindBox = new BotUserBlindBox();
                blindBox.setUserId(userId);
                blindBox.setBoxContent(boxContent);
                blindBox.setFetchDate(DateUtil.today());
                userBlindBoxMapper.insert(blindBox);
                
                StringBuilder message = new StringBuilder();
                message.append("━━━━━━━━━━━━\n");
                message.append("✨ 开启盲盒 ✨\n");
                message.append("━━━━━━━━━━━━\n\n");
                message.append(String.format("💸 消耗积分：2\n"));
                message.append("💨 很遗憾，什么都没有~\n");
                message.append(String.format("💰 当前积分：%d", userScore.getScore()));
                
                return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            }
            
            // 7. 处理词条抽取
            return handleWordDraw(userId, groupId, userScore, originalScore);
            
        } catch (Exception e) {
            log.error("开盲盒异常", e);
            return new CommonResp("开盲盒失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }

    /**
     * 批量开启盲盒
     */
    private CommonResp handleBatchOpenBlindBox(String userId, String groupId, int count, BotGameUserScore userScore) {
        try {
            // 统计结果
            Map<String, Integer> resultMap = new HashMap<>();
            List<BotBaseWord> drawnWords = new ArrayList<>();
            List<String> duplicateWords = new ArrayList<>(); // 记录重复的词条名称
            int refundCount = 0; // 返还次数统计
            int totalCost = count * 2;
            int originalScore = userScore.getScore();
            
            // 执行批量开启
            for (int i = 0; i < count; i++) {
                // 扣除2积分
                userScore.setScore(userScore.getScore() - 2);
                
                // 随机抽取盲盒内容
                double rand = Math.random();
                String boxContent;
                int scoreReward = 0;
                boolean isWord = false;
                
                if (rand < 0.25) {
                    boxContent = "空";
                } else if (rand < 0.45) {
                    boxContent = "1积分";
                    scoreReward = 1;
                } else if (rand < 0.65) {
                    boxContent = "2积分";
                    scoreReward = 2;
                } else if (rand < 0.80) {
                    boxContent = "3积分";
                    scoreReward = 3;
                } else if (rand < 0.90) {
                    boxContent = "5积分";
                    scoreReward = 5;
                } else if (rand < 0.95) {
                    boxContent = "8积分";
                    scoreReward = 8;
                } else {
                    boxContent = "词条";
                    isWord = true;
                }
                
                // 处理积分奖励
                if (scoreReward > 0) {
                    userScore.setScore(userScore.getScore() + scoreReward);
                    resultMap.put(boxContent, resultMap.getOrDefault(boxContent, 0) + 1);
                    
                    // 记录盲盒记录
                    BotUserBlindBox blindBox = new BotUserBlindBox();
                    blindBox.setUserId(userId);
                    blindBox.setBoxContent(boxContent);
                    blindBox.setFetchDate(DateUtil.today());
                    userBlindBoxMapper.insert(blindBox);
                } else if (!isWord) {
                    // 空盲盒
                    resultMap.put("空", resultMap.getOrDefault("空", 0) + 1);
                    
                    // 记录盲盒记录
                    BotUserBlindBox blindBox = new BotUserBlindBox();
                    blindBox.setUserId(userId);
                    blindBox.setBoxContent(boxContent);
                    blindBox.setFetchDate(DateUtil.today());
                    userBlindBoxMapper.insert(blindBox);
                } else {
                    // 抽到词条
                    DrawWordResult wordResult = drawWordFromPoolWithInfo(userId, userScore);
                    if (wordResult.word != null) {
                        drawnWords.add(wordResult.word);
                    } else if (wordResult.isDuplicate) {
                        // 重复词条，记录名称并统计返还次数
                        duplicateWords.add(wordResult.duplicateWordName);
                        refundCount++;
                    }
                }
            }
            
            // 更新用户积分
            gameUserScoreMapper.updateByPrimaryKey(userScore);
            
            // 构建返回消息
            StringBuilder message = new StringBuilder();
            message.append("━━━━━━━━━━━━\n");
            message.append(String.format("✨ 开启%d次盲盒 ✨\n", count));
            message.append("━━━━━━━━━━━━\n\n");
            message.append(String.format("💸 消耗积分：%d\n\n", totalCost));
            
            // 按顺序展示非词条结果
            if (!resultMap.isEmpty()) {
                message.append("【获得奖励】\n");
                // 按照空、1积分、2积分...的顺序展示
                String[] order = {"空", "1积分", "2积分", "3积分", "5积分", "8积分"};
                for (String key : order) {
                    if (resultMap.containsKey(key)) {
                        message.append(String.format("%s × %d次\n", key, resultMap.get(key)));
                    }
                }
                message.append("\n");
            }
            
            // 词条结果
            if (!drawnWords.isEmpty()) {
                message.append(String.format("🎊 抽到词条 × %d次！\n\n", drawnWords.size()));
                for (BotBaseWord word : drawnWords) {
                    String rarityLabel = ENWordRarity.getLabelByValue(word.getRarity());
                    String groupInfo = (word.getGroupFlag() != null && !word.getGroupFlag().trim().isEmpty()) 
                            ? word.getGroupFlag() : "无分组";
                    
                    message.append("━━━━━━━━━━━━\n");
                    message.append(String.format("『%s』\n", word.getWord()));
                    message.append(String.format("✨ 稀有度：%s\n", rarityLabel));
                    message.append(String.format("💫 魅力值：+%d\n", word.getMerit()));
                    message.append(String.format("📂 所属组：%s\n", groupInfo));
                    message.append(String.format("📖 说明：%s\n", word.getMemo()));
                }
            }
            
            // 重复词条提示
            if (!duplicateWords.isEmpty()) {
                message.append("\n⚠️ 重复词条（已返还）\n");
                // 统计重复词条的次数
                Map<String, Integer> duplicateCount = new HashMap<>();
                for (String wordName : duplicateWords) {
                    duplicateCount.put(wordName, duplicateCount.getOrDefault(wordName, 0) + 1);
                }
                // 展示重复词条
                for (Map.Entry<String, Integer> entry : duplicateCount.entrySet()) {
                    if (entry.getValue() > 1) {
                        message.append(String.format("『%s』 × %d次\n", entry.getKey(), entry.getValue()));
                    } else {
                        message.append(String.format("『%s』\n", entry.getKey()));
                    }
                }
                message.append(String.format("已返还开盲盒次数：%d次（返还积分：%d）\n", refundCount, refundCount * 2));
            }
            
            message.append("\n━━━━━━━━━━━━\n");
            message.append(String.format("💰 原积分：%d → 现积分：%d\n", originalScore, userScore.getScore()));
            if (userScore.getAccumulateMerit() != null && userScore.getAccumulateMerit() > 0) {
                message.append(String.format("✨ 总魅力值：%d", userScore.getAccumulateMerit()));
            }
            
            return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            
        } catch (Exception e) {
            log.error("批量开盲盒异常", e);
            return new CommonResp("开盲盒失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }

    /**
     * 词条抽取结果包装类
     */
    private static class DrawWordResult {
        BotBaseWord word; // 抽到的新词条
        boolean isDuplicate; // 是否为重复词条
        String duplicateWordName; // 重复词条的名称
        
        DrawWordResult(BotBaseWord word) {
            this.word = word;
            this.isDuplicate = false;
        }
        
        DrawWordResult(String duplicateWordName) {
            this.word = null;
            this.isDuplicate = true;
            this.duplicateWordName = duplicateWordName;
        }
    }

    /**
     * 从词条池抽取词条（批量开启时使用，返回详细信息）
     */
    private DrawWordResult drawWordFromPoolWithInfo(String userId, BotGameUserScore userScore) {
        try {
            // 1. 查询当前时间可抽取的词条
            String currentTime = DateUtil.now();
            BotBaseWordExample wordExample = new BotBaseWordExample();
            wordExample.createCriteria()
                    .andBeginDateLessThanOrEqualTo(currentTime)
                    .andEndDateGreaterThanOrEqualTo(currentTime);
            List<BotBaseWord> availableWords = baseWordMapper.selectByExample(wordExample);
            
            if (CollectionUtil.isEmpty(availableWords)) {
                // 没有可抽取的词条，返还2积分
                userScore.setScore(userScore.getScore() + 2);
                // 记录空盲盒
                BotUserBlindBox blindBox = new BotUserBlindBox();
                blindBox.setUserId(userId);
                blindBox.setBoxContent("词条-未抽中");
                blindBox.setFetchDate(DateUtil.today());
                userBlindBoxMapper.insert(blindBox);
                return new DrawWordResult((BotBaseWord) null);
            }
            
            // 2. 先按稀有度抽取
            String rarity = drawRarity();
            
            // 3. 筛选对应稀有度的词条
            List<BotBaseWord> rarityWords = availableWords.stream()
                    .filter(w -> w.getRarity().equals(rarity))
                    .collect(Collectors.toList());
            
            if (CollectionUtil.isEmpty(rarityWords)) {
                // 该稀有度没有词条，返还2积分
                userScore.setScore(userScore.getScore() + 2);
                // 记录空盲盒
                BotUserBlindBox blindBox = new BotUserBlindBox();
                blindBox.setUserId(userId);
                blindBox.setBoxContent("词条-未抽中");
                blindBox.setFetchDate(DateUtil.today());
                userBlindBoxMapper.insert(blindBox);
                return new DrawWordResult((BotBaseWord) null);
            }
            
            // 4. 按照每个词条配置的概率抽取
            BotBaseWord drawnWord = drawWordByProbability(rarityWords);
            
            if (drawnWord == null) {
                // 抽取失败，返还2积分
                userScore.setScore(userScore.getScore() + 2);
                // 记录空盲盒
                BotUserBlindBox blindBox = new BotUserBlindBox();
                blindBox.setUserId(userId);
                blindBox.setBoxContent("词条-未抽中");
                blindBox.setFetchDate(DateUtil.today());
                userBlindBoxMapper.insert(blindBox);
                return new DrawWordResult((BotBaseWord) null);
            }
            
            // 5. 检查是否已拥有该词条
            BotUserWordExample userWordExample = new BotUserWordExample();
            userWordExample.createCriteria()
                    .andUserIdEqualTo(userId)
                    .andWordIdEqualTo(drawnWord.getId());
            int ownedCount = userWordMapper.countByExample(userWordExample);
            
            if (ownedCount > 0) {
                // 已拥有，返还2积分，不计入次数
                userScore.setScore(userScore.getScore() + 2);
                // 记录空盲盒
                BotUserBlindBox blindBox = new BotUserBlindBox();
                blindBox.setUserId(userId);
                blindBox.setBoxContent("词条-" + drawnWord.getWord() + "(已拥有)");
                blindBox.setFetchDate(DateUtil.today());
                userBlindBoxMapper.insert(blindBox);
                // 返回重复词条信息
                return new DrawWordResult(drawnWord.getWord());
            }
            
            // 6. 新词条，保存到用户词条表
            BotUserWord userWord = new BotUserWord();
            userWord.setUserId(userId);
            userWord.setWordId(drawnWord.getId());
            userWord.setWordContent(drawnWord.getWord());
            userWord.setRarity(drawnWord.getRarity());
            userWord.setMerit(drawnWord.getMerit());
            userWord.setFetchDate(DateUtil.now());
            userWordMapper.insert(userWord);
            
            // 7. 更新用户总魅力值
            Integer currentMerit = userScore.getAccumulateMerit() != null ? userScore.getAccumulateMerit() : 0;
            userScore.setAccumulateMerit(currentMerit + drawnWord.getMerit());
            
            // 8. 检查是否集齐了该组所有词条
            int bonusMerit = 0;
            if (drawnWord.getGroupFlag() != null && !drawnWord.getGroupFlag().trim().isEmpty()) {
                bonusMerit = checkGroupComplete(userId, drawnWord.getGroupFlag());
                if (bonusMerit > 0) {
                    userScore.setAccumulateMerit(userScore.getAccumulateMerit() + bonusMerit);
                }
            }
            
            // 9. 记录盲盒记录
            BotUserBlindBox blindBox = new BotUserBlindBox();
            blindBox.setUserId(userId);
            blindBox.setBoxContent("词条-" + drawnWord.getWord());
            blindBox.setFetchDate(DateUtil.today());
            userBlindBoxMapper.insert(blindBox);
            
            return new DrawWordResult(drawnWord);
            
        } catch (Exception e) {
            log.error("抽取词条异常", e);
            return new DrawWordResult((BotBaseWord) null);
        }
    }

    /**
     * 从词条池抽取词条（批量开启时使用）
     * @deprecated 使用 drawWordFromPoolWithInfo 代替
     */
    private BotBaseWord drawWordFromPool(String userId, BotGameUserScore userScore) {
        try {
            // 1. 查询当前时间可抽取的词条
            String currentTime = DateUtil.now();
            BotBaseWordExample wordExample = new BotBaseWordExample();
            wordExample.createCriteria()
                    .andBeginDateLessThanOrEqualTo(currentTime)
                    .andEndDateGreaterThanOrEqualTo(currentTime);
            List<BotBaseWord> availableWords = baseWordMapper.selectByExample(wordExample);
            
            if (CollectionUtil.isEmpty(availableWords)) {
                // 没有可抽取的词条，返还2积分
                userScore.setScore(userScore.getScore() + 2);
                // 记录空盲盒
                BotUserBlindBox blindBox = new BotUserBlindBox();
                blindBox.setUserId(userId);
                blindBox.setBoxContent("词条-未抽中");
                blindBox.setFetchDate(DateUtil.today());
                userBlindBoxMapper.insert(blindBox);
                return null;
            }
            
            // 2. 先按稀有度抽取
            String rarity = drawRarity();
            
            // 3. 筛选对应稀有度的词条
            List<BotBaseWord> rarityWords = availableWords.stream()
                    .filter(w -> w.getRarity().equals(rarity))
                    .collect(Collectors.toList());
            
            if (CollectionUtil.isEmpty(rarityWords)) {
                // 该稀有度没有词条，返还2积分
                userScore.setScore(userScore.getScore() + 2);
                // 记录空盲盒
                BotUserBlindBox blindBox = new BotUserBlindBox();
                blindBox.setUserId(userId);
                blindBox.setBoxContent("词条-未抽中");
                blindBox.setFetchDate(DateUtil.today());
                userBlindBoxMapper.insert(blindBox);
                return null;
            }
            
            // 4. 按照每个词条配置的概率抽取
            BotBaseWord drawnWord = drawWordByProbability(rarityWords);
            
            if (drawnWord == null) {
                // 抽取失败，返还2积分
                userScore.setScore(userScore.getScore() + 2);
                // 记录空盲盒
                BotUserBlindBox blindBox = new BotUserBlindBox();
                blindBox.setUserId(userId);
                blindBox.setBoxContent("词条-未抽中");
                blindBox.setFetchDate(DateUtil.today());
                userBlindBoxMapper.insert(blindBox);
                return null;
            }
            
            // 5. 检查是否已拥有该词条
            BotUserWordExample userWordExample = new BotUserWordExample();
            userWordExample.createCriteria()
                    .andUserIdEqualTo(userId)
                    .andWordIdEqualTo(drawnWord.getId());
            int ownedCount = userWordMapper.countByExample(userWordExample);
            
            if (ownedCount > 0) {
                // 已拥有，返还2积分，不计入次数
                userScore.setScore(userScore.getScore() + 2);
                // 记录空盲盒
                BotUserBlindBox blindBox = new BotUserBlindBox();
                blindBox.setUserId(userId);
                blindBox.setBoxContent("词条-" + drawnWord.getWord() + "(已拥有)");
                blindBox.setFetchDate(DateUtil.today());
                userBlindBoxMapper.insert(blindBox);
                return null;
            }
            
            // 6. 新词条，保存到用户词条表
            BotUserWord userWord = new BotUserWord();
            userWord.setUserId(userId);
            userWord.setWordId(drawnWord.getId());
            userWord.setWordContent(drawnWord.getWord());
            userWord.setRarity(drawnWord.getRarity());
            userWord.setMerit(drawnWord.getMerit());
            userWord.setFetchDate(DateUtil.now());
            userWordMapper.insert(userWord);
            
            // 7. 更新用户总魅力值
            Integer currentMerit = userScore.getAccumulateMerit() != null ? userScore.getAccumulateMerit() : 0;
            userScore.setAccumulateMerit(currentMerit + drawnWord.getMerit());
            
            // 8. 检查是否集齐了该组所有词条
            int bonusMerit = 0;
            if (drawnWord.getGroupFlag() != null && !drawnWord.getGroupFlag().trim().isEmpty()) {
                bonusMerit = checkGroupComplete(userId, drawnWord.getGroupFlag());
                if (bonusMerit > 0) {
                    userScore.setAccumulateMerit(userScore.getAccumulateMerit() + bonusMerit);
                }
            }
            
            // 9. 记录盲盒记录
            BotUserBlindBox blindBox = new BotUserBlindBox();
            blindBox.setUserId(userId);
            blindBox.setBoxContent("词条-" + drawnWord.getWord());
            blindBox.setFetchDate(DateUtil.today());
            userBlindBoxMapper.insert(blindBox);
            
            return drawnWord;
            
        } catch (Exception e) {
            log.error("抽取词条异常", e);
            return null;
        }
    }

    /**
     * 处理词条抽取
     */
    private CommonResp handleWordDraw(String userId, String groupId, BotGameUserScore userScore, int originalScore) {
        try {
            // 1. 查询当前时间可抽取的词条
            String currentTime = DateUtil.now();
            BotBaseWordExample wordExample = new BotBaseWordExample();
            wordExample.createCriteria()
                    .andBeginDateLessThanOrEqualTo(currentTime)
                    .andEndDateGreaterThanOrEqualTo(currentTime);
            List<BotBaseWord> availableWords = baseWordMapper.selectByExample(wordExample);
            
            if (CollectionUtil.isEmpty(availableWords)) {
                // 没有可抽取的词条，返还2积分
                userScore.setScore(userScore.getScore() + 2);
                gameUserScoreMapper.updateByPrimaryKey(userScore);
                return new CommonResp("暂无可抽取的词条，已返还2积分~", ENRespType.TEXT.getType());
            }
            
            // 2. 先按稀有度抽取
            String rarity = drawRarity();
            
            // 3. 筛选对应稀有度的词条
            List<BotBaseWord> rarityWords = availableWords.stream()
                    .filter(w -> w.getRarity().equals(rarity))
                    .collect(Collectors.toList());
            
            if (CollectionUtil.isEmpty(rarityWords)) {
                // 该稀有度没有词条，返还2积分
                userScore.setScore(userScore.getScore() + 2);
                gameUserScoreMapper.updateByPrimaryKey(userScore);
                return new CommonResp("暂无可抽取的词条，已返还2积分~", ENRespType.TEXT.getType());
            }
            
            // 4. 按照每个词条配置的概率抽取
            BotBaseWord drawnWord = drawWordByProbability(rarityWords);
            
            if (drawnWord == null) {
                // 抽取失败，返还2积分
                userScore.setScore(userScore.getScore() + 2);
                gameUserScoreMapper.updateByPrimaryKey(userScore);
                return new CommonResp("抽取失败，已返还2积分~", ENRespType.TEXT.getType());
            }
            
            // 5. 检查是否已拥有该词条
            BotUserWordExample userWordExample = new BotUserWordExample();
            userWordExample.createCriteria()
                    .andUserIdEqualTo(userId)
                    .andWordIdEqualTo(drawnWord.getId());
            int ownedCount = userWordMapper.countByExample(userWordExample);
            
            if (ownedCount > 0) {
                // 已拥有，返还2积分，不计入次数
                userScore.setScore(userScore.getScore() + 2);
                gameUserScoreMapper.updateByPrimaryKey(userScore);
                
                String rarityLabel = ENWordRarity.getLabelByValue(drawnWord.getRarity());
                StringBuilder message = new StringBuilder();
                message.append("━━━━━━━━━━━━\n");
                message.append("✨ 开启盲盒 ✨\n");
                message.append("━━━━━━━━━━━━\n\n");
                message.append(String.format("🎁 抽到词条：『%s』\n", drawnWord.getWord()));
                message.append(String.format("✨ 稀有度：%s\n\n", rarityLabel));
                message.append("⚠️ 该词条已拥有，已返还2积分\n");
                message.append(String.format("💰 当前积分：%d", userScore.getScore()));
                
                return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            }
            
            // 6. 新词条，保存到用户词条表
            BotUserWord userWord = new BotUserWord();
            userWord.setUserId(userId);
            userWord.setWordId(drawnWord.getId());
            userWord.setWordContent(drawnWord.getWord());
            userWord.setRarity(drawnWord.getRarity());
            userWord.setMerit(drawnWord.getMerit());
            userWord.setFetchDate(DateUtil.now());
            userWordMapper.insert(userWord);
            
            // 7. 更新用户总魅力值
            Integer currentMerit = userScore.getAccumulateMerit() != null ? userScore.getAccumulateMerit() : 0;
            userScore.setAccumulateMerit(currentMerit + drawnWord.getMerit());
            
            // 8. 检查是否集齐了该组所有词条
            int bonusMerit = 0;
            if (drawnWord.getGroupFlag() != null && !drawnWord.getGroupFlag().trim().isEmpty()) {
                bonusMerit = checkGroupComplete(userId, drawnWord.getGroupFlag());
                if (bonusMerit > 0) {
                    userScore.setAccumulateMerit(userScore.getAccumulateMerit() + bonusMerit);
                }
            }
            
            // 9. 更新用户积分和魅力值
            gameUserScoreMapper.updateByPrimaryKey(userScore);
            
            // 10. 记录盲盒记录
            BotUserBlindBox blindBox = new BotUserBlindBox();
            blindBox.setUserId(userId);
            blindBox.setBoxContent("词条-" + drawnWord.getWord());
            blindBox.setFetchDate(DateUtil.today());
            userBlindBoxMapper.insert(blindBox);
            
            // 11. 构建返回消息
            String rarityLabel = ENWordRarity.getLabelByValue(drawnWord.getRarity());
            String groupInfo = (drawnWord.getGroupFlag() != null && !drawnWord.getGroupFlag().trim().isEmpty()) 
                    ? drawnWord.getGroupFlag() : "无分组";
            
            StringBuilder message = new StringBuilder();
            message.append("━━━━━━━━━━━━\n");
            message.append("✨ 开启盲盒 ✨\n");
            message.append("━━━━━━━━━━━━\n\n");
            message.append(String.format("💸 消耗积分：2\n\n"));
            message.append(String.format("🎊 恭喜获得词条：『%s』\n\n", drawnWord.getWord()));
            message.append(String.format("✨ 稀有度：%s\n", rarityLabel));
            message.append(String.format("💫 魅力值：+%d\n", drawnWord.getMerit()));
            message.append(String.format("📂 所属组：%s\n", groupInfo));
            message.append(String.format("📖 说明：%s\n", drawnWord.getMemo()));
            
            if (bonusMerit > 0) {
                message.append(String.format("\n🎉 集齐『%s』全部词条！\n", groupInfo));
                message.append(String.format("🎁 额外魅力值：+%d\n", bonusMerit));
            }
            
            message.append(String.format("\n💰 原积分：%d → 现积分：%d\n", originalScore, userScore.getScore()));
            message.append(String.format("✨ 总魅力值：%d", userScore.getAccumulateMerit()));
            
            return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            
        } catch (Exception e) {
            log.error("抽取词条异常", e);
            // 异常时返还2积分
            userScore.setScore(userScore.getScore() + 2);
            gameUserScoreMapper.updateByPrimaryKey(userScore);
            return new CommonResp("抽取词条失败，已返还2积分~", ENRespType.TEXT.getType());
        }
    }

    /**
     * 按稀有度抽取
     */
    private String drawRarity() {
        double rand = Math.random();
        double cumulative = 0.0;
        
        for (ENWordRarity rarity : ENWordRarity.values()) {
            cumulative += Double.parseDouble(rarity.getProbability());
            if (rand < cumulative) {
                return rarity.getValue();
            }
        }
        
        // 默认返回普通
        return ENWordRarity.COMMON.getValue();
    }

    /**
     * 按词条概率抽取
     */
    private BotBaseWord drawWordByProbability(List<BotBaseWord> words) {
        if (CollectionUtil.isEmpty(words)) {
            return null;
        }
        
        // 计算总概率
        double totalProb = words.stream()
                .mapToDouble(w -> Double.parseDouble(w.getProbability()))
                .sum();
        
        if (totalProb <= 0) {
            // 如果总概率为0，随机返回一个
            return words.get((int) (Math.random() * words.size()));
        }
        
        // 按概率抽取
        double rand = Math.random() * totalProb;
        double cumulative = 0.0;
        
        for (BotBaseWord word : words) {
            cumulative += Double.parseDouble(word.getProbability());
            if (rand < cumulative) {
                return word;
            }
        }
        
        // 兜底返回最后一个
        return words.get(words.size() - 1);
    }

    /**
     * 检查分组是否集齐，返回奖励魅力值
     */
    private int checkGroupComplete(String userId, String groupFlag) {
        try {
            // 1. 查询该组所有词条
            BotBaseWordExample groupExample = new BotBaseWordExample();
            groupExample.createCriteria().andGroupFlagEqualTo(groupFlag);
            List<BotBaseWord> groupWords = baseWordMapper.selectByExample(groupExample);
            
            if (CollectionUtil.isEmpty(groupWords)) {
                return 0;
            }
            
            // 2. 查询用户已拥有的该组词条
            List<Long> groupWordIds = groupWords.stream()
                    .map(BotBaseWord::getId)
                    .collect(Collectors.toList());
            
            BotUserWordExample userWordExample = new BotUserWordExample();
            userWordExample.createCriteria()
                    .andUserIdEqualTo(userId)
                    .andWordIdIn(groupWordIds);
            List<BotUserWord> userWords = userWordMapper.selectByExample(userWordExample);
            
            // 3. 检查是否集齐
            if (userWords.size() == groupWords.size()) {
                // 集齐了，计算奖励
                int totalMerit = groupWords.stream()
                        .mapToInt(BotBaseWord::getMerit)
                        .sum();
                return (int) (totalMerit * 0.05);
            }
            
            return 0;
        } catch (Exception e) {
            log.error("检查分组完成度异常", e);
            return 0;
        }
    }

    /**
     * 处理我的词条指令
     */
    private CommonResp handleMyWords(String userId) {
        try {
            // 1. 查询用户所有词条
            BotUserWordExample wordExample = new BotUserWordExample();
            wordExample.createCriteria().andUserIdEqualTo(userId);
            List<BotUserWord> userWords = userWordMapper.selectByExample(wordExample);
            
            if (CollectionUtil.isEmpty(userWords)) {
                return new CommonResp("你还没有任何词条，快去开盲盒吧~", ENRespType.TEXT.getType());
            }
            
            // 2. 按稀有度和魅力值排序
            List<BotUserWord> sortedWords = userWords.stream()
                    .sorted((a, b) -> {
                        // 先按稀有度排序（传说>史诗>稀有>普通）
                        int rarityCompare = b.getRarity().compareTo(a.getRarity());
                        if (rarityCompare != 0) {
                            return rarityCompare;
                        }
                        // 稀有度相同按魅力值排序
                        return b.getMerit().compareTo(a.getMerit());
                    })
                    .collect(Collectors.toList());
            
            // 3. 查询用户总魅力值
            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
            scoreExample.createCriteria().andUserIdEqualTo(userId);
            List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(scoreExample);
            int totalMerit = (scores != null && !scores.isEmpty() && scores.get(0).getAccumulateMerit() != null) 
                    ? scores.get(0).getAccumulateMerit() : 0;
            
            // 4. 构建消息
            StringBuilder message = new StringBuilder();
            message.append("━━━━━━━━━━━━\n");
            message.append("📚 我的词条收藏 📚\n");
            message.append("━━━━━━━━━━━━\n\n");
            message.append(String.format("✨ 总魅力值：%d\n", totalMerit));
            message.append(String.format("📖 词条数量：%d\n\n", userWords.size()));
            
            for (int i = 0; i < sortedWords.size(); i++) {
                BotUserWord word = sortedWords.get(i);
                String rarityLabel = ENWordRarity.getLabelByValue(word.getRarity());
                message.append(String.format("%d. 『%s』 [%s] 魅力+%d\n", 
                        i + 1, word.getWordContent(), rarityLabel, word.getMerit()));
            }
            
            message.append("\n━━━━━━━━━━━━\n");
            message.append("💡 回复序号查看详情，回复【取消】退出");
            
            // 5. 保存上下文
            USER_WORD_VIEW_CONTEXT.put(userId, sortedWords);
            
            return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            
        } catch (Exception e) {
            log.error("查询我的词条异常", e);
            return new CommonResp("查询失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }

    /**
     * 处理词条操作（查看详情和佩戴）
     */
    private CommonResp handleWordOperation(String userId, String instruction, String groupId) {
        try {
            // 先检查是否是佩戴指令
            if (instruction.trim().equals("佩戴")) {
                List<BotUserWord> selectedWords = USER_WORD_VIEW_CONTEXT.get(userId + "_SELECTED");
                if (CollectionUtil.isEmpty(selectedWords)) {
                    return new CommonResp("请先回复序号查看词条详情~", ENRespType.TEXT.getType());
                }
                
                BotUserWord wordToWear = selectedWords.get(0);
                
                // 更新用户佩戴词条
                BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
                scoreExample.createCriteria().andUserIdEqualTo(userId);
                List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(scoreExample);
                
                if (CollectionUtil.isEmpty(scores)) {
                    // 如果用户积分记录不存在，创建一个
                    BotGameUserScore newScore = new BotGameUserScore();
                    newScore.setUserId(userId);
                    newScore.setNickname(groupId != null ? SendMsgUtil.getGroupNickName(groupId, userId) : userId);
                    newScore.setScore(0);
                    newScore.setCurrentWord(wordToWear.getWordContent());
                    newScore.setAccumulateMerit(wordToWear.getMerit());
                    gameUserScoreMapper.insert(newScore);
                } else {
                    BotGameUserScore userScore = scores.get(0);
                    userScore.setCurrentWord(wordToWear.getWordContent());
                    gameUserScoreMapper.updateByPrimaryKey(userScore);
                }
                
                // 清除上下文
                USER_WORD_VIEW_CONTEXT.remove(userId);
                USER_WORD_VIEW_CONTEXT.remove(userId + "_SELECTED");

                // 更新缓存
                List<BotGameUserScore> userScoreList = gameUserScoreMapper.selectByExample(new BotGameUserScoreExample());
                SystemConfigCache.userWordMap.clear();
                SystemConfigCache.userWordMap.putAll(userScoreList.stream().filter(x -> StrUtil.isNotEmpty(x.getCurrentWord())).collect(Collectors.toMap(BotGameUserScore::getUserId, BotGameUserScore::getCurrentWord)));
                
                return new CommonResp(String.format("✨ 已将『%s』设为展示词条！", wordToWear.getWordContent()), 
                        ENRespType.TEXT.getType());
            }
            
            // 处理查看序号
            List<BotUserWord> userWords = USER_WORD_VIEW_CONTEXT.get(userId);
            
            if (CollectionUtil.isEmpty(userWords)) {
                USER_WORD_VIEW_CONTEXT.remove(userId);
                return null;
            }
            
            // 尝试解析序号
            try {
                int index = Integer.parseInt(instruction.trim());
                
                if (index < 1 || index > userWords.size()) {
                    USER_WORD_VIEW_CONTEXT.remove(userId);
                    return null;
                }
                
                // 显示词条详情
                BotUserWord selectedWord = userWords.get(index - 1);
                
                // 查询词条详细信息
                BotBaseWord baseWord = baseWordMapper.selectByPrimaryKey(selectedWord.getWordId());
                if (baseWord == null) {
                    return new CommonResp("词条信息不存在~", ENRespType.TEXT.getType());
                }
                
                String rarityLabel = ENWordRarity.getLabelByValue(selectedWord.getRarity());
                String groupInfo = (baseWord.getGroupFlag() != null && !baseWord.getGroupFlag().trim().isEmpty()) 
                        ? baseWord.getGroupFlag() : "无分组";
                
                StringBuilder message = new StringBuilder();
                message.append("━━━━━━━━━━━━\n");
                message.append(String.format("📜 『%s』\n", selectedWord.getWordContent()));
                message.append("━━━━━━━━━━━━\n\n");
                message.append(String.format("✨ 稀有度：%s\n", rarityLabel));
                message.append(String.format("💫 魅力值：%d\n", selectedWord.getMerit()));
                message.append(String.format("📂 所属组：%s\n", groupInfo));
                message.append(String.format("📖 说明：%s\n", baseWord.getMemo()));
                message.append(String.format("🕐 获得时间：%s\n\n", selectedWord.getFetchDate()));
                message.append("━━━━━━━━━━━━\n");
                message.append("💡 回复序号查看详情，回复【佩戴】可佩戴词条，回复【取消】退出");
                
                // 临时保存当前选中的词条（用于佩戴）
                List<BotUserWord> tempList = new ArrayList<>();
                tempList.add(selectedWord);
                USER_WORD_VIEW_CONTEXT.put(userId + "_SELECTED", tempList);
                
                return new CommonResp(message.toString(), ENRespType.TEXT.getType());
                
            } catch (NumberFormatException e) {
                // 不是数字，也不是佩戴指令，清除上下文
                USER_WORD_VIEW_CONTEXT.remove(userId);
                USER_WORD_VIEW_CONTEXT.remove(userId + "_SELECTED");
                return null;
            }
            
        } catch (Exception e) {
            log.error("处理词条操作异常", e);
            USER_WORD_VIEW_CONTEXT.remove(userId);
            USER_WORD_VIEW_CONTEXT.remove(userId + "_SELECTED");
            return new CommonResp("操作失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }

    /**
     * 处理小林魅力排名
     */
    private CommonResp handleMeritRank(String userId) {
        try {
            // 查询所有用户积分（包含魅力值）
            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
            List<BotGameUserScore> allScores = gameUserScoreMapper.selectByExample(scoreExample);
            
            if (CollectionUtil.isEmpty(allScores)) {
                return new CommonResp("暂无魅力值数据~", ENRespType.TEXT.getType());
            }
            
            // 在内存中排序（按魅力值降序，魅力值为null的视为0）
            List<BotGameUserScore> sortedScores = allScores.stream()
                    .sorted((a, b) -> {
                        int meritA = a.getAccumulateMerit() != null ? a.getAccumulateMerit() : 0;
                        int meritB = b.getAccumulateMerit() != null ? b.getAccumulateMerit() : 0;
                        return Integer.compare(meritB, meritA);
                    })
                    .filter(s -> s.getAccumulateMerit() != null && s.getAccumulateMerit() > 0)
                    .collect(Collectors.toList());
            
            if (CollectionUtil.isEmpty(sortedScores)) {
                return new CommonResp("暂无魅力值数据~", ENRespType.TEXT.getType());
            }
            
            // 获取前10名
            int topCount = Math.min(10, sortedScores.size());
            List<BotGameUserScore> topTen = sortedScores.subList(0, topCount);
            
            // 构建排行榜消息
            StringBuilder message = new StringBuilder();
            message.append("━━━━━━━━━━━━\n");
            message.append("✨ 魅力值排行榜 ✨\n");
            message.append("━━━━━━━━━━━━\n\n");
            
            for (int i = 0; i < topTen.size(); i++) {
                BotGameUserScore score = topTen.get(i);
                String displayName = score.getNickname() != null && !score.getNickname().trim().isEmpty() 
                        ? score.getNickname() : score.getUserId();
                
                // 佩戴词条展示：用「」括号紧跟名字
                if (score.getCurrentWord() != null && !score.getCurrentWord().trim().isEmpty()) {
                    displayName = displayName + "「" + score.getCurrentWord() + "」";
                }
                
                // 前三名使用特殊图标
                String icon;
                if (i == 0) {
                    icon = "🥇";
                } else if (i == 1) {
                    icon = "🥈";
                } else if (i == 2) {
                    icon = "🥉";
                } else {
                    icon = String.format("%d.", i + 1);
                }
                
                message.append(String.format("%s %s  %d✨\n", 
                        icon, displayName, score.getAccumulateMerit()));
            }
            
            // 查询当前用户的魅力值和排名
            message.append("\n━━━━━━━━━━━━\n");
            
            BotGameUserScoreExample userExample = new BotGameUserScoreExample();
            userExample.createCriteria().andUserIdEqualTo(userId);
            List<BotGameUserScore> userScores = gameUserScoreMapper.selectByExample(userExample);
            
            if (CollectionUtil.isEmpty(userScores) || userScores.get(0).getAccumulateMerit() == null 
                    || userScores.get(0).getAccumulateMerit() == 0) {
                message.append("💫 我的魅力值：0，暂无排名");
            } else {
                BotGameUserScore myScore = userScores.get(0);
                // 在内存中计算排名
                int myRank = 1;
                for (BotGameUserScore s : sortedScores) {
                    if (s.getUserId().equals(userId)) {
                        break;
                    }
                    myRank++;
                }
                
                message.append(String.format("💫 我的魅力值：%d，排名：%d", 
                        myScore.getAccumulateMerit(), myRank));
            }
            
            return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            
        } catch (Exception e) {
            log.error("查询魅力值排名异常", e);
            return new CommonResp("查询失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }

    /**
     * 处理词条库查询（需要传入用户ID以标识已拥有的词条）
     */
    private CommonResp handleWordLibrary(String userId) {
        try {
            // 查询当前时间可抽取的所有词条
            String currentTime = DateUtil.now();
            BotBaseWordExample wordExample = new BotBaseWordExample();
            wordExample.createCriteria()
                    .andBeginDateLessThanOrEqualTo(currentTime)
                    .andEndDateGreaterThanOrEqualTo(currentTime);
            List<BotBaseWord> availableWords = baseWordMapper.selectByExample(wordExample);
            
            if (CollectionUtil.isEmpty(availableWords)) {
                return new CommonResp("当前暂无可抽取的词条~", ENRespType.TEXT.getType());
            }
            
            // 查询用户已拥有的词条ID集合
            BotUserWordExample userWordExample = new BotUserWordExample();
            userWordExample.createCriteria().andUserIdEqualTo(userId);
            List<BotUserWord> userWords = userWordMapper.selectByExample(userWordExample);
            Set<Long> ownedWordIds = userWords.stream()
                    .map(BotUserWord::getWordId)
                    .collect(Collectors.toSet());
            
            // 按分组归类
            Map<String, List<BotBaseWord>> groupedWords = availableWords.stream()
                    .collect(Collectors.groupingBy(w -> {
                        String group = w.getGroupFlag();
                        return (group != null && !group.trim().isEmpty()) ? group : "未分组";
                    }));
            
            // 构建消息
            StringBuilder message = new StringBuilder();
            message.append("━━━━━━━━━━━━\n");
            message.append("📚 词条库 📚\n");
            message.append("━━━━━━━━━━━━\n\n");
            
            // 遍历每个分组
            for (Map.Entry<String, List<BotBaseWord>> groupEntry : groupedWords.entrySet()) {
                String groupName = groupEntry.getKey();
                List<BotBaseWord> words = groupEntry.getValue();
                
                // 计算结束时间
                String timeInfo = calculateTimeInfo(words);
                
                message.append("【").append(groupName).append("】\n");
                message.append(timeInfo).append("\n\n");
                
                // 按稀有度分组
                Map<String, List<BotBaseWord>> rarityGroups = words.stream()
                        .collect(Collectors.groupingBy(BotBaseWord::getRarity));
                
                // 按稀有度顺序展示（传说 > 史诗 > 稀有 > 普通）
                String[] rarityOrder = {"4", "3", "2", "1"};
                
                for (String rarityValue : rarityOrder) {
                    if (rarityGroups.containsKey(rarityValue)) {
                        List<BotBaseWord> rarityWords = rarityGroups.get(rarityValue);
                        String rarityLabel = ENWordRarity.getLabelByValue(rarityValue);
                        
                        // 收集词条名称，已拥有的加上✓标记
                        List<String> wordNames = rarityWords.stream()
                                .map(w -> {
                                    String name = w.getWord();
                                    // 如果用户已拥有该词条，添加✓标记
                                    if (ownedWordIds.contains(w.getId())) {
                                        return name + "✓";
                                    }
                                    return name;
                                })
                                .collect(Collectors.toList());
                        
                        message.append(rarityLabel).append("：")
                               .append(String.join("、", wordNames))
                               .append("\n");
                    }
                }
                
                message.append("\n");
            }
            
            message.append("━━━━━━━━━━━━\n");
            message.append("💫 开盲盒即可抽取哦~\n");
            message.append("💡 标记✓表示已拥有");
            
            return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            
        } catch (Exception e) {
            log.error("查询词条库异常", e);
            return new CommonResp("查询失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }

    /**
     * 计算时间信息（永久或剩余天数）
     */
    private String calculateTimeInfo(List<BotBaseWord> words) {
        if (CollectionUtil.isEmpty(words)) {
            return "(永久)";
        }
        
        // 取第一个词条的结束时间（同一分组的结束时间应该一致）
        String endDateStr = words.get(0).getEndDate();
        
        try {
            // 检查是否是永久（2099年）
            if (endDateStr.startsWith("2099")) {
                return "(永久)";
            }
            
            // 解析结束时间
            Date endDate = DateUtil.parse(endDateStr, "yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            
            // 计算剩余天数
            long diffMillis = endDate.getTime() - now.getTime();
            long daysLeft = diffMillis / (1000 * 60 * 60 * 24);
            
            if (daysLeft <= 0) {
                return "(即将结束)";
            } else if (daysLeft == 1) {
                return "(限时 还剩1天)";
            } else {
                return String.format("(限时 还剩%d天)", daysLeft);
            }
            
        } catch (Exception e) {
            log.error("解析时间异常", e);
            return "(永久)";
        }
    }

    private String getRandomMessage() {
        try{
            String response = HttpUtil.get("https://v.api.aa1.cn/api/api-wenan-wangyiyunreping/index.php?aa1=json");
            JSONArray jsonArray = JSONUtil.parseArray(response);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            return jsonObject.getStr("wangyiyunreping").split("——")[0];
        }catch (Exception e) {
            return "愿你开心每一天~";
        }

    }

    private void reg(String token, String regType) {
        if (SystemConfigCache.userDateMap.containsKey(token)) {
            log.info("5---" + token + "----" + regType);
            // 用过 需要根据之前的过期时间来判断从哪个时间上加
            // 之前未到期，续期
            if (SystemConfigCache.userDateMap.get(token).after(new Date())) {
                BotUser botUser = new BotUser();
                botUser.setId(token);
                botUser.setStatus(ENRegStatus.FOREVER.getValue());
                botUser.setDeadLineDate(DateUtil.offsetDay(SystemConfigCache.userDateMap.get(token), 90));
                userMapper.updateByPrimaryKeySelective(botUser);
                systemConfigHolder.loadUsers();
                return;
            }
            // 已到期，新开通
            BotUser botUser = new BotUser();
            botUser.setId(token);
            botUser.setStatus(ENRegStatus.FOREVER.getValue());
            botUser.setDeadLineDate(DateUtil.offsetDay(new Date(), 90));
            userMapper.updateByPrimaryKeySelective(botUser);
            systemConfigHolder.loadUsers();
            return;
        }
        log.info("6---" + token + "----" + regType);
        // 没用过 直接加
        BotUser botUser = new BotUser();
        botUser.setSignDay(0);
        botUser.setId(token);
        botUser.setStatus(ENRegStatus.FOREVER.getValue());
        botUser.setType(regType);
        botUser.setDeadLineDate(DateUtil.offsetDay(new Date(), 90));
        userMapper.insert(botUser);
        log.info("7---" + token + "----" + regType);
        BotUserConfig botUserConfig = new BotUserConfig();
        botUserConfig.setId(IdUtil.simpleUUID());
        botUserConfig.setUserId(token);
        userConfigMapper.insert(botUserConfig);
        log.info("8---" + token + "----" + regType);
        systemConfigHolder.loadUsers();
        log.info("9---" + token + "----" + regType);
    }

    private BotUserBox getMoney(String token, String channel) {
        UserBindExample userBindExample = new UserBindExample();
        if ("qq".equals(channel)) {
            userBindExample.createCriteria().andQqUserTokenEqualTo(token);
        }else {
            userBindExample.createCriteria().andWxUserTokenEqualTo(token);
        }
        List<UserBind> userBindList = userBindMapper.selectByExample(userBindExample);
        BotUserBoxExample userBoxExample = new BotUserBoxExample();
        if (CollectionUtil.isNotEmpty(userBindList)) {
            UserBind userBind = userBindList.get(0);
            userBoxExample.createCriteria().andGoodTypeEqualTo(ENUserGoodType.MONEY.getValue()).andUserIdEqualTo(userBind.getQqUserToken());
            BotUserBox wxBox = userBoxMapper.selectByExample(userBoxExample).get(0);
            userBoxExample.clear();
            userBoxExample.createCriteria().andGoodTypeEqualTo(ENUserGoodType.MONEY.getValue()).andUserIdEqualTo(userBind.getWxUserToken());
            BotUserBox qqBox = userBoxMapper.selectByExample(userBoxExample).get(0);
            if (wxBox.getNumber() > qqBox.getNumber()) {
                return wxBox;
            }else {
                return qqBox;
            }
        }
        userBoxExample.createCriteria().andUserIdEqualTo(token).andGoodTypeEqualTo(ENUserGoodType.MONEY.getValue());
        List<BotUserBox> userBoxList = userBoxMapper.selectByExample(userBoxExample);
        if (CollectionUtil.isEmpty(userBoxList)) {
            return null;
        }
        return userBoxList.get(0);
    }

}
