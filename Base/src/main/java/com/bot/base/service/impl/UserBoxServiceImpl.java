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
import com.bot.common.enums.ENSystemWord;
import com.bot.common.enums.ENUserGoodType;
import com.bot.common.enums.ENWordRarity;
import com.bot.common.util.SendMsgUtil;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.*;
import com.bot.game.enums.ENWordType;
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
    
    // 用于维护用户词条筛选上下文
    private static final Map<String, WordFilterContext> USER_WORD_FILTER_CONTEXT = new HashMap<>();
    
    /**
     * 词条筛选上下文
     */
    private static class WordFilterContext {
        String filterType; // "GROUP"词组 / "RARITY"稀有度 / "ALL"全部
        String filterValue; // 具体的词组名或稀有度值
        int currentPage; // 当前页码（全部模式使用）
        
        WordFilterContext(String filterType, String filterValue, int currentPage) {
            this.filterType = filterType;
            this.filterValue = filterValue;
            this.currentPage = currentPage;
        }
    }

    /**
     * 词组进度信息
     */
    private static class GroupProgress {
        String groupName;
        String groupType;  // 词组类型
        int ownedCount;
        int totalCount;
        int bonusMerit;
        boolean isCompleted;
        
        GroupProgress(String groupName, String groupType, int ownedCount, int totalCount, int bonusMerit) {
            this.groupName = groupName;
            this.groupType = groupType;
            this.ownedCount = ownedCount;
            this.totalCount = totalCount;
            this.bonusMerit = bonusMerit;
            this.isCompleted = ownedCount >= totalCount;
        }
    }

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
            
            // 圣诞节特殊词条发放（2025年12月25日）
            String christmasReward = "";
            if ("2025-12-25".equals(DateUtil.today())) {
                christmasReward = this.grantChristmasWord(token);
            }
            
            // 跨年特殊词条发放（2025年12月31日或2026年1月1日）
            String newYearReward = "";
            String today = DateUtil.today();
            if ("2025-12-31".equals(today) || "2026-01-01".equals(today)) {
                newYearReward = this.grantNewYearWord(token);
            }
            
            String message = this.getRandomMessage();
            String response = String.format("签到成功，积分+%s\r\n\r\n%s%s%s", number, christmasReward, newYearReward, message);
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
        if (USER_WORD_VIEW_CONTEXT.containsKey(token) || USER_WORD_VIEW_CONTEXT.containsKey(token + "_SELECTED") 
                || USER_WORD_FILTER_CONTEXT.containsKey(token)) {
            // 如果用户发送其他指令，清除词条上下文
            if (reqContent.equals("开盲盒") || reqContent.equals("我的词条") 
                    || reqContent.equals("小林魅力排名") || reqContent.equals("词条库")
                    || reqContent.equals("小林积分排名") || reqContent.equals("取消")) {
                USER_WORD_VIEW_CONTEXT.remove(token);
                USER_WORD_VIEW_CONTEXT.remove(token + "_SELECTED");
                USER_WORD_FILTER_CONTEXT.remove(token);
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
            List<String> ouhuangWords = new ArrayList<>(); // 记录触发欧皇效果的词条
            List<String> ouhuangFailedWords = new ArrayList<>(); // 记录欧皇效果失败的词条
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
                        // 记录是否触发了欧皇效果
                        if (wordResult.isOuhuangTriggered) {
                            ouhuangWords.add(wordResult.word.getWord());
                        }
                    } else if (wordResult.isDuplicate) {
                        // 重复词条，记录名称并统计返还次数
                        duplicateWords.add(wordResult.duplicateWordName);
                        refundCount++;
                        // 检查是否是欧皇效果失败（该稀有度所有词条都已拥有）
                        if (wordResult.isOuhuangTriggered) {
                            ouhuangFailedWords.add(wordResult.duplicateWordName);
                        }
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
                    
                    // 检查是否触发了欧皇效果
                    if (ouhuangWords.contains(word.getWord())) {
                        message.append("🌟【欧皇】效果触发！\n");
                    }
                    
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
                    String wordName = entry.getKey();
                    int wordCount = entry.getValue();
                    
                    // 检查是否是欧皇效果失败
                    if (ouhuangFailedWords.contains(wordName)) {
                        if (wordCount > 1) {
                            message.append(String.format("『%s』 × %d次 🌟欧皇无法替换\n", wordName, wordCount));
                        } else {
                            message.append(String.format("『%s』 🌟欧皇无法替换\n", wordName));
                        }
                    } else {
                        if (wordCount > 1) {
                            message.append(String.format("『%s』 × %d次\n", wordName, wordCount));
                        } else {
                            message.append(String.format("『%s』\n", wordName));
                        }
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
        boolean isOuhuangTriggered; // 是否触发了欧皇效果
        
        DrawWordResult(BotBaseWord word) {
            this.word = word;
            this.isDuplicate = false;
            this.isOuhuangTriggered = false;
        }
        
        DrawWordResult(String duplicateWordName) {
            this.word = null;
            this.isDuplicate = true;
            this.duplicateWordName = duplicateWordName;
            this.isOuhuangTriggered = false;
        }
        
        DrawWordResult(BotBaseWord word, boolean isOuhuangTriggered) {
            this.word = word;
            this.isDuplicate = false;
            this.isOuhuangTriggered = isOuhuangTriggered;
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
                // 检查是否佩戴了「欧皇」词条
                String currentWord = SystemConfigCache.userWordMap.get(userId);
                boolean isOuhuangActive = "欧皇".equals(currentWord);
                
                if (isOuhuangActive) {
                    // 触发欧皇效果：尝试替换为同等稀有度未拥有的词条
                    BotBaseWord replacedWord = tryReplaceWithSameRarity(userId, drawnWord.getRarity(), rarityWords, availableWords);
                    
                    if (replacedWord != null) {
                        // 成功替换，保存新词条
                        drawnWord = replacedWord;
                        ownedCount = 0; // 重置拥有标记，继续执行新词条保存逻辑
                    } else {
                        // 该稀有度所有词条都已拥有，返还2积分，不计入次数
                        userScore.setScore(userScore.getScore() + 2);
                        // 返回重复词条信息（带欧皇触发标记）
                        DrawWordResult result = new DrawWordResult(drawnWord.getWord());
                        result.isOuhuangTriggered = true;
                        return result;
                    }
                } else {
                    // 未佩戴欧皇，正常返还2积分，不计入次数
                    userScore.setScore(userScore.getScore() + 2);
                    // 重复词条不记录盲盒记录，不占用开盲盒次数
                    // 返回重复词条信息
                    return new DrawWordResult(drawnWord.getWord());
                }
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
            
            // 检查是否触发了欧皇效果
            String currentWord = SystemConfigCache.userWordMap.get(userId);
            boolean wasOuhuangTriggered = "欧皇".equals(currentWord) && ownedCount == 0;
            
            return new DrawWordResult(drawnWord, wasOuhuangTriggered);
            
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
                // 检查是否佩戴了「欧皇」词条
                String currentWord = SystemConfigCache.userWordMap.get(userId);
                boolean isOuhuangActive = "欧皇".equals(currentWord);
                
                if (isOuhuangActive) {
                    // 触发欧皇效果：尝试替换为同等稀有度未拥有的词条
                    BotBaseWord replacedWord = tryReplaceWithSameRarity(userId, drawnWord.getRarity(), rarityWords, availableWords);
                    
                    if (replacedWord != null) {
                        // 成功替换，保存新词条
                        drawnWord = replacedWord;
                        ownedCount = 0; // 重置拥有标记，继续执行新词条保存逻辑
                    } else {
                        // 该稀有度所有词条都已拥有，返还2积分
                        userScore.setScore(userScore.getScore() + 2);
                        gameUserScoreMapper.updateByPrimaryKey(userScore);
                        
                        String rarityLabel = ENWordRarity.getLabelByValue(drawnWord.getRarity());
                        StringBuilder message = new StringBuilder();
                        message.append("━━━━━━━━━━━━\n");
                        message.append("✨ 开启盲盒 ✨\n");
                        message.append("━━━━━━━━━━━━\n\n");
                        message.append("🌟【欧皇】效果触发！\n\n");
                        message.append(String.format("🎁 抽到词条：『%s』\n", drawnWord.getWord()));
                        message.append(String.format("✨ 稀有度：%s\n\n", rarityLabel));
                        message.append("⚠️ 该稀有度所有词条均已拥有\n");
                        message.append("   无法替换，已返还2积分\n");
                        message.append(String.format("💰 当前积分：%d", userScore.getScore()));
                        
                        return new CommonResp(message.toString(), ENRespType.TEXT.getType());
                    }
                } else {
                    // 未佩戴欧皇，正常返还2积分
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
            
            // 检查是否触发了欧皇效果（通过检查ownedCount是否从1变回了0）
            String currentWord = SystemConfigCache.userWordMap.get(userId);
            boolean wasOuhuangTriggered = "欧皇".equals(currentWord) && ownedCount == 0;
            
            StringBuilder message = new StringBuilder();
            message.append("━━━━━━━━━━━━\n");
            message.append("✨ 开启盲盒 ✨\n");
            message.append("━━━━━━━━━━━━\n\n");
            
            if (wasOuhuangTriggered) {
                message.append("🌟【欧皇】效果触发！\n");
                message.append("   重复词条已替换为同等稀有度新词条\n\n");
            }
            
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
     * 尝试替换为同等稀有度未拥有的词条（欧皇效果）
     * @param userId 用户ID
     * @param rarity 稀有度
     * @param rarityWords 当前稀有度的词条列表
     * @param availableWords 所有可用词条列表
     * @return 替换后的词条，如果没有可替换的返回null
     */
    private BotBaseWord tryReplaceWithSameRarity(String userId, String rarity, 
                                                   List<BotBaseWord> rarityWords, 
                                                   List<BotBaseWord> availableWords) {
        try {
            // 1. 查询用户已拥有的词条ID
            BotUserWordExample userWordExample = new BotUserWordExample();
            userWordExample.createCriteria().andUserIdEqualTo(userId);
            List<BotUserWord> userWords = userWordMapper.selectByExample(userWordExample);
            Set<Long> ownedWordIds = userWords.stream()
                    .map(BotUserWord::getWordId)
                    .collect(Collectors.toSet());
            
            // 2. 筛选同等稀有度且未拥有的词条
            List<BotBaseWord> unownedRarityWords = availableWords.stream()
                    .filter(w -> w.getRarity().equals(rarity))
                    .filter(w -> !ownedWordIds.contains(w.getId()))
                    .collect(Collectors.toList());
            
            if (CollectionUtil.isEmpty(unownedRarityWords)) {
                // 该稀有度所有词条都已拥有
                return null;
            }
            
            // 3. 从未拥有的词条中按概率抽取一个
            return drawWordByProbability(unownedRarityWords);
            
        } catch (Exception e) {
            log.error("欧皇效果替换词条异常", e);
            return null;
        }
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
            
            // 4. 查询词组完成情况
            Map<String, GroupProgress> groupProgressMap = calculateGroupProgress(userId);
            
            // 5. 根据词条数量决定展示模式
            if (sortedWords.size() <= 10) {
                // 词条较少，直接全部展示
                return buildSimpleWordList(userId, sortedWords, totalMerit, groupProgressMap);
            } else {
                // 词条较多，展示分类菜单 + 最近10条
                return buildAdvancedWordList(userId, sortedWords, totalMerit, groupProgressMap);
            }
            
        } catch (Exception e) {
            log.error("查询我的词条异常", e);
            return new CommonResp("查询失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }

    /**
     * 计算词组完成进度
     */
    private Map<String, GroupProgress> calculateGroupProgress(String userId) {
        Map<String, GroupProgress> progressMap = new HashMap<>();
        try {
            // 查询所有词条（包括已过期的），用于统计词组进度
            BotBaseWordExample allWordExample = new BotBaseWordExample();
            List<BotBaseWord> allBaseWords = baseWordMapper.selectByExample(allWordExample);
            
            // 查询当前时间可抽取的词条（用于筛选展示哪些词组）
            String currentTime = DateUtil.now();
            Set<Long> availableWordIds = allBaseWords.stream()
                    .filter(w -> w.getBeginDate() != null && w.getEndDate() != null)
                    .filter(w -> w.getBeginDate().compareTo(currentTime) <= 0 && w.getEndDate().compareTo(currentTime) >= 0)
                    .map(BotBaseWord::getId)
                    .collect(Collectors.toSet());
            
            // 查询用户已拥有的词条
            BotUserWordExample userWordExample = new BotUserWordExample();
            userWordExample.createCriteria().andUserIdEqualTo(userId);
            List<BotUserWord> userWords = userWordMapper.selectByExample(userWordExample);
            Set<Long> ownedWordIds = userWords.stream()
                    .map(BotUserWord::getWordId)
                    .collect(Collectors.toSet());
            
            // 按词组分组（所有词条）
            Map<String, List<BotBaseWord>> allGroupWordsMap = allBaseWords.stream()
                    .filter(w -> w.getGroupFlag() != null && !w.getGroupFlag().trim().isEmpty())
                    .collect(Collectors.groupingBy(BotBaseWord::getGroupFlag));
            
            for (Map.Entry<String, List<BotBaseWord>> entry : allGroupWordsMap.entrySet()) {
                String groupName = entry.getKey();
                List<BotBaseWord> groupWords = entry.getValue();
                
                // 特殊处理：系统奖励始终展示，不受时间和拥有数量限制
                boolean isSystemReward = "系统奖励".equals(groupName);
                
                // 如果不是系统奖励，只统计当前可抽取的词条
                List<BotBaseWord> effectiveWords;
                if (isSystemReward) {
                    effectiveWords = groupWords; // 系统奖励不受时间限制
                } else {
                    effectiveWords = groupWords.stream()
                            .filter(w -> availableWordIds.contains(w.getId()))
                            .collect(Collectors.toList());
                    
                    // 如果没有可抽取的词条，跳过该词组
                    if (effectiveWords.isEmpty()) {
                        continue;
                    }
                }
                
                // 获取词组类型（取第一个词条的type）
                String groupType = effectiveWords.get(0).getType();
                
                // 统计用户已拥有的词条数
                int ownedCount = (int) effectiveWords.stream()
                        .filter(w -> ownedWordIds.contains(w.getId()))
                        .count();
                
                int totalCount = effectiveWords.size();
                
                // 过滤规则：如果用户一个都没拥有，不展示该分组（系统奖励除外）
                if (!isSystemReward && ownedCount == 0) {
                    continue;
                }
                
                // 计算奖励（系统奖励分组不参与额外奖励）
                int bonusMerit = 0;
                if (isSystemReward) {
                    // 系统赠送词条，固定为+0
                    bonusMerit = 0;
                } else if (ownedCount >= totalCount) {
                    int totalGroupMerit = effectiveWords.stream()
                            .mapToInt(BotBaseWord::getMerit)
                            .sum();
                    bonusMerit = (int) (totalGroupMerit * 0.05);
                }
                
                progressMap.put(groupName, new GroupProgress(groupName, groupType, ownedCount, totalCount, bonusMerit));
            }
            
            // 构建所有词条的Map，用于快速查找
            Map<Long, BotBaseWord> baseWordMap = allBaseWords.stream()
                    .collect(Collectors.toMap(BotBaseWord::getId, w -> w));
            
            // 统计未分组词条（排除"系统奖励"分组的词条）
            long ungroupedCount = userWords.stream()
                    .filter(uw -> {
                        BotBaseWord baseWord = baseWordMap.get(uw.getWordId());
                        if (baseWord == null) {
                            return true; // 词条信息不存在，归为未分组
                        }
                        String groupFlag = baseWord.getGroupFlag();
                        // 排除系统奖励，只统计真正的未分组（group_flag为空）
                        return groupFlag == null || groupFlag.trim().isEmpty();
                    })
                    .count();
            
            if (ungroupedCount > 0) {
                progressMap.put("未分组", new GroupProgress("未分组", "1", (int)ungroupedCount, (int)ungroupedCount, 0));
            }
            
        } catch (Exception e) {
            log.error("计算词组进度异常", e);
        }
        return progressMap;
    }
    
    /**
     * 构建简单词条列表（词条<=10时）
     */
    private CommonResp buildSimpleWordList(String userId, List<BotUserWord> sortedWords, 
                                          int totalMerit, Map<String, GroupProgress> groupProgressMap) {
        StringBuilder message = new StringBuilder();
        message.append("━━━━━━━━━━━━\n");
        message.append("📚 我的词条收藏 📚\n");
        message.append("━━━━━━━━━━━━\n\n");
        message.append(String.format("✨ 总魅力值：%d\n", totalMerit));
        message.append(String.format("📖 词条数量：%d\n\n", sortedWords.size()));
        
        for (int i = 0; i < sortedWords.size(); i++) {
            BotUserWord word = sortedWords.get(i);
            String rarityLabel = ENWordRarity.getLabelByValue(word.getRarity());
            message.append(String.format("%d. 『%s』 [%s] 魅力+%d\n", 
                    i + 1, word.getWordContent(), rarityLabel, word.getMerit()));
        }
        
        message.append("\n━━━━━━━━━━━━\n");
        message.append("💡 回复【序号】查看详情\n");
        message.append("💡 回复【取消】退出");
        
        // 保存上下文
        USER_WORD_VIEW_CONTEXT.put(userId, sortedWords);
        
        return new CommonResp(message.toString(), ENRespType.TEXT.getType());
    }
    
    /**
     * 构建高级词条列表（词条>10时）
     */
    private CommonResp buildAdvancedWordList(String userId, List<BotUserWord> sortedWords,
                                            int totalMerit, Map<String, GroupProgress> groupProgressMap) {
        StringBuilder message = new StringBuilder();
        message.append("━━━━━━━━━━━━\n");
        message.append("📚 我的词条收藏 📚\n");
        message.append("━━━━━━━━━━━━\n\n");
        message.append(String.format("✨ 总魅力值：%d\n", totalMerit));
        message.append(String.format("📖 词条总数：%d\n\n", sortedWords.size()));
        
        // 显示词组收集进度
        if (!groupProgressMap.isEmpty()) {
            message.append("【词组收集进度】\n");
            // 按完成度排序（未完成的在前，已完成的在后）
            List<GroupProgress> sortedProgress = groupProgressMap.values().stream()
                    .sorted((a, b) -> {
                        if (a.isCompleted != b.isCompleted) {
                            return a.isCompleted ? 1 : -1;
                        }
                        return b.ownedCount - a.ownedCount;
                    })
                    .collect(Collectors.toList());
            
            for (GroupProgress progress : sortedProgress) {
                // 获取词组类型图标和标签
                String typeIcon = ENWordType.getIconByValue(progress.groupType);
                String typeLabel = ENWordType.getLabelByValue(progress.groupType);
                
                if (progress.groupName.equals("未分组")) {
                    message.append(String.format("%s [%d条]\n", progress.groupName, progress.ownedCount));
                } else if (progress.isCompleted) {
                    message.append(String.format("%s %s [%d/%d] ✅ +%d [%s]\n", 
                            typeIcon, progress.groupName, progress.ownedCount, progress.totalCount, progress.bonusMerit, typeLabel));
                } else {
                    message.append(String.format("%s %s [%d/%d] ⏳ [%s]\n", 
                            typeIcon, progress.groupName, progress.ownedCount, progress.totalCount, typeLabel));
                }
            }
            message.append("\n");
        }
        
        // 显示稀有度分类
        Map<String, Long> rarityCount = sortedWords.stream()
                .collect(Collectors.groupingBy(BotUserWord::getRarity, Collectors.counting()));
        
        message.append("【快捷筛选】\n");
        message.append(String.format("🔸 传说(%d)  🔸 史诗(%d)\n", 
                rarityCount.getOrDefault("4", 0L).intValue(),
                rarityCount.getOrDefault("3", 0L).intValue()));
        message.append(String.format("🔸 稀有(%d)  🔸 普通(%d)\n\n", 
                rarityCount.getOrDefault("2", 0L).intValue(),
                rarityCount.getOrDefault("1", 0L).intValue()));
        
        // 显示最近获得的5条
        message.append("【最近获得】\n");
        // 按获得时间排序，取最新5条
        List<BotUserWord> recentWords = sortedWords.stream()
                .sorted((a, b) -> b.getFetchDate().compareTo(a.getFetchDate()))
                .limit(5)
                .collect(Collectors.toList());
        
        for (int i = 0; i < recentWords.size(); i++) {
            BotUserWord word = recentWords.get(i);
            String rarityLabel = ENWordRarity.getLabelByValue(word.getRarity());
            
            // 查询词组信息
            BotBaseWord baseWord = baseWordMapper.selectByPrimaryKey(word.getWordId());
            String groupName = (baseWord != null && baseWord.getGroupFlag() != null && !baseWord.getGroupFlag().trim().isEmpty()) 
                    ? baseWord.getGroupFlag() : "未分组";
            
            message.append(String.format("%d. 『%s』[%s·%s] +%d\n", 
                    i + 1, word.getWordContent(), rarityLabel, groupName, word.getMerit()));
        }
        
        message.append("\n━━━━━━━━━━━━\n");
        message.append("💡 回复【序号】查看详情\n");
        message.append("💡 回复【词组名】查看词组词条\n");
        message.append("💡 回复【传说/史诗/稀有/普通】筛选\n");
        message.append("💡 回复【全部】查看所有(分页)\n");
        message.append("💡 回复【返回】返回主列表\n");
        message.append("💡 回复【取消】退出");
        
        // 保存上下文
        USER_WORD_VIEW_CONTEXT.put(userId, recentWords);
        USER_WORD_FILTER_CONTEXT.put(userId, new WordFilterContext("RECENT", null, 1));
        
        return new CommonResp(message.toString(), ENRespType.TEXT.getType());
    }

    /**
     * 处理词条操作（查看详情和佩戴）
     */
    private CommonResp handleWordOperation(String userId, String instruction, String groupId) {
        try {
            String trimmedInstruction = instruction.trim();
            
            // 先检查是否是佩戴指令
            if (trimmedInstruction.equals("佩戴")) {
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
                USER_WORD_FILTER_CONTEXT.remove(userId);

                // 更新缓存
                List<BotGameUserScore> userScoreList = gameUserScoreMapper.selectByExample(new BotGameUserScoreExample());
                SystemConfigCache.userWordMap.clear();
                SystemConfigCache.userWordMap.putAll(userScoreList.stream().filter(x -> StrUtil.isNotEmpty(x.getCurrentWord())).collect(Collectors.toMap(BotGameUserScore::getUserId, BotGameUserScore::getCurrentWord)));
                
                return new CommonResp(String.format("✨ 已将『%s』设为展示词条！", wordToWear.getWordContent()), 
                        ENRespType.TEXT.getType());
            }
            
            // 处理返回主列表
            if (trimmedInstruction.equals("返回")) {
                USER_WORD_VIEW_CONTEXT.remove(userId + "_SELECTED");
                USER_WORD_FILTER_CONTEXT.remove(userId);
                return handleMyWords(userId);
            }
            
            // 检查是否是稀有度筛选
            if (trimmedInstruction.equals("传说") || trimmedInstruction.equals("史诗") || 
                trimmedInstruction.equals("稀有") || trimmedInstruction.equals("普通")) {
                return handleRarityFilter(userId, trimmedInstruction);
            }
            
            // 检查是否是全部筛选
            if (trimmedInstruction.equals("全部")) {
                return handleAllWordsFilter(userId, 1);
            }
            
            // 检查是否是词组名筛选
            WordFilterContext filterContext = USER_WORD_FILTER_CONTEXT.get(userId);
            if (filterContext != null && filterContext.filterType.equals("ALL")) {
                // 在全部模式下，可能是翻页指令
                try {
                    int page = Integer.parseInt(trimmedInstruction);
                    return handleAllWordsFilter(userId, page);
                } catch (NumberFormatException e) {
                    // 不是数字，可能是词组名
                }
            }
            
            // 尝试作为词组名处理
            CommonResp groupFilterResp = tryHandleGroupFilter(userId, trimmedInstruction);
            if (groupFilterResp != null) {
                return groupFilterResp;
            }
            
            // 处理查看序号
            List<BotUserWord> userWords = USER_WORD_VIEW_CONTEXT.get(userId);
            
            if (CollectionUtil.isEmpty(userWords)) {
                USER_WORD_VIEW_CONTEXT.remove(userId);
                USER_WORD_FILTER_CONTEXT.remove(userId);
                return null;
            }
            
            // 尝试解析序号
            try {
                int index = Integer.parseInt(trimmedInstruction);
                
                if (index < 1 || index > userWords.size()) {
                    USER_WORD_VIEW_CONTEXT.remove(userId);
                    USER_WORD_FILTER_CONTEXT.remove(userId);
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
                // 不是数字，也不是已知指令，清除上下文
                USER_WORD_VIEW_CONTEXT.remove(userId);
                USER_WORD_VIEW_CONTEXT.remove(userId + "_SELECTED");
                USER_WORD_FILTER_CONTEXT.remove(userId);
                return null;
            }
            
        } catch (Exception e) {
            log.error("处理词条操作异常", e);
            USER_WORD_VIEW_CONTEXT.remove(userId);
            USER_WORD_VIEW_CONTEXT.remove(userId + "_SELECTED");
            USER_WORD_FILTER_CONTEXT.remove(userId);
            return new CommonResp("操作失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }
    
    /**
     * 处理稀有度筛选
     */
    private CommonResp handleRarityFilter(String userId, String rarityName) {
        try {
            // 查询用户所有词条
            BotUserWordExample wordExample = new BotUserWordExample();
            wordExample.createCriteria().andUserIdEqualTo(userId);
            List<BotUserWord> allUserWords = userWordMapper.selectByExample(wordExample);
            
            if (CollectionUtil.isEmpty(allUserWords)) {
                return new CommonResp("你还没有任何词条~", ENRespType.TEXT.getType());
            }
            
            // 根据稀有度名称获取稀有度值
            String rarityValue = null;
            for (ENWordRarity rarity : ENWordRarity.values()) {
                if (rarity.getLabel().equals(rarityName)) {
                    rarityValue = rarity.getValue();
                    break;
                }
            }
            
            if (rarityValue == null) {
                return new CommonResp("稀有度类型错误~", ENRespType.TEXT.getType());
            }
            
            // 筛选出该稀有度的词条
            final String targetRarity = rarityValue;
            List<BotUserWord> filteredWords = allUserWords.stream()
                    .filter(w -> w.getRarity().equals(targetRarity))
                    .sorted((a, b) -> b.getMerit().compareTo(a.getMerit()))
                    .collect(Collectors.toList());
            
            if (CollectionUtil.isEmpty(filteredWords)) {
                return new CommonResp(String.format("你还没有%s稀有度的词条~", rarityName), ENRespType.TEXT.getType());
            }
            
            // 构建消息
            StringBuilder message = new StringBuilder();
            message.append("━━━━━━━━━━━━\n");
            message.append(String.format("📚 %s词条 (%d条) 📚\n", rarityName, filteredWords.size()));
            message.append("━━━━━━━━━━━━\n\n");
            
            for (int i = 0; i < filteredWords.size(); i++) {
                BotUserWord word = filteredWords.get(i);
                
                // 查询词组信息
                BotBaseWord baseWord = baseWordMapper.selectByPrimaryKey(word.getWordId());
                String groupName = (baseWord != null && baseWord.getGroupFlag() != null && !baseWord.getGroupFlag().trim().isEmpty()) 
                        ? baseWord.getGroupFlag() : "未分组";
                
                message.append(String.format("%d. 『%s』[%s] +%d\n", 
                        i + 1, word.getWordContent(), groupName, word.getMerit()));
            }
            
            message.append("\n━━━━━━━━━━━━\n");
            message.append("💡 回复【序号】查看详情\n");
            message.append("💡 回复【返回】返回主列表\n");
            message.append("💡 回复【取消】退出");
            
            // 保存上下文
            USER_WORD_VIEW_CONTEXT.put(userId, filteredWords);
            USER_WORD_FILTER_CONTEXT.put(userId, new WordFilterContext("RARITY", rarityName, 1));
            
            return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            
        } catch (Exception e) {
            log.error("处理稀有度筛选异常", e);
            return new CommonResp("操作失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }
    
    /**
     * 处理全部筛选（分页）
     */
    private CommonResp handleAllWordsFilter(String userId, int page) {
        try {
            // 查询用户所有词条
            BotUserWordExample wordExample = new BotUserWordExample();
            wordExample.createCriteria().andUserIdEqualTo(userId);
            List<BotUserWord> allUserWords = userWordMapper.selectByExample(wordExample);
            
            if (CollectionUtil.isEmpty(allUserWords)) {
                return new CommonResp("你还没有任何词条~", ENRespType.TEXT.getType());
            }
            
            // 按稀有度和魅力值排序
            List<BotUserWord> sortedWords = allUserWords.stream()
                    .sorted((a, b) -> {
                        int rarityCompare = b.getRarity().compareTo(a.getRarity());
                        if (rarityCompare != 0) {
                            return rarityCompare;
                        }
                        return b.getMerit().compareTo(a.getMerit());
                    })
                    .collect(Collectors.toList());
            
            // 分页处理
            int pageSize = 20;
            int totalPages = (int) Math.ceil(sortedWords.size() * 1.0 / pageSize);
            
            if (page < 1 || page > totalPages) {
                return new CommonResp(String.format("页码错误，请输入1-%d之间的页码~", totalPages), ENRespType.TEXT.getType());
            }
            
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, sortedWords.size());
            List<BotUserWord> pageWords = sortedWords.subList(startIndex, endIndex);
            
            // 构建消息
            StringBuilder message = new StringBuilder();
            message.append("━━━━━━━━━━━━\n");
            message.append(String.format("📚 全部词条 (第%d/%d页) 📚\n", page, totalPages));
            message.append("━━━━━━━━━━━━\n\n");
            
            for (int i = 0; i < pageWords.size(); i++) {
                BotUserWord word = pageWords.get(i);
                String rarityLabel = ENWordRarity.getLabelByValue(word.getRarity());
                
                // 查询词组信息
                BotBaseWord baseWord = baseWordMapper.selectByPrimaryKey(word.getWordId());
                String groupName = (baseWord != null && baseWord.getGroupFlag() != null && !baseWord.getGroupFlag().trim().isEmpty()) 
                        ? baseWord.getGroupFlag() : "未分组";
                
                message.append(String.format("%d. 『%s』[%s·%s] +%d\n", 
                        i + 1, word.getWordContent(), rarityLabel, groupName, word.getMerit()));
            }
            
            message.append("\n━━━━━━━━━━━━\n");
            message.append("💡 回复【序号】查看详情\n");
            if (page < totalPages) {
                message.append(String.format("💡 回复【%d】查看下一页\n", page + 1));
            }
            message.append("💡 回复【返回】返回主列表\n");
            message.append("💡 回复【取消】退出");
            
            // 保存上下文
            USER_WORD_VIEW_CONTEXT.put(userId, pageWords);
            USER_WORD_FILTER_CONTEXT.put(userId, new WordFilterContext("ALL", null, page));
            
            return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            
        } catch (Exception e) {
            log.error("处理全部筛选异常", e);
            return new CommonResp("操作失败，请稍后再试~", ENRespType.TEXT.getType());
        }
    }
    
    /**
     * 尝试处理词组名筛选
     */
    private CommonResp tryHandleGroupFilter(String userId, String groupName) {
        try {
            // 查询用户所有词条
            BotUserWordExample wordExample = new BotUserWordExample();
            wordExample.createCriteria().andUserIdEqualTo(userId);
            List<BotUserWord> allUserWords = userWordMapper.selectByExample(wordExample);
            
            if (CollectionUtil.isEmpty(allUserWords)) {
                return null;
            }
            
            // 筛选出该词组的词条
            List<BotUserWord> groupWords = new ArrayList<>();
            for (BotUserWord userWord : allUserWords) {
                BotBaseWord baseWord = baseWordMapper.selectByPrimaryKey(userWord.getWordId());
                if (baseWord != null) {
                    String wordGroup = (baseWord.getGroupFlag() != null && !baseWord.getGroupFlag().trim().isEmpty()) 
                            ? baseWord.getGroupFlag() : "未分组";
                    if (wordGroup.equals(groupName)) {
                        groupWords.add(userWord);
                    }
                }
            }
            
            if (CollectionUtil.isEmpty(groupWords)) {
                return null; // 没有该词组的词条，不是有效的词组名
            }
            
            // 按稀有度和魅力值排序
            groupWords = groupWords.stream()
                    .sorted((a, b) -> {
                        int rarityCompare = b.getRarity().compareTo(a.getRarity());
                        if (rarityCompare != 0) {
                            return rarityCompare;
                        }
                        return b.getMerit().compareTo(a.getMerit());
                    })
                    .collect(Collectors.toList());
            
            // 构建消息
            StringBuilder message = new StringBuilder();
            message.append("━━━━━━━━━━━━\n");
            message.append(String.format("📚 %s (%d条) 📚\n", groupName, groupWords.size()));
            message.append("━━━━━━━━━━━━\n\n");
            
            for (int i = 0; i < groupWords.size(); i++) {
                BotUserWord word = groupWords.get(i);
                String rarityLabel = ENWordRarity.getLabelByValue(word.getRarity());
                
                message.append(String.format("%d. 『%s』[%s] +%d\n", 
                        i + 1, word.getWordContent(), rarityLabel, word.getMerit()));
            }
            
            message.append("\n━━━━━━━━━━━━\n");
            message.append("💡 回复【序号】查看详情\n");
            message.append("💡 回复【返回】返回主列表\n");
            message.append("💡 回复【取消】退出");
            
            // 保存上下文
            USER_WORD_VIEW_CONTEXT.put(userId, groupWords);
            USER_WORD_FILTER_CONTEXT.put(userId, new WordFilterContext("GROUP", groupName, 1));
            
            return new CommonResp(message.toString(), ENRespType.TEXT.getType());
            
        } catch (Exception e) {
            log.error("处理词组筛选异常", e);
            return null;
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
                
                // 获取词组类型（取第一个词条的type，同一词组类型应该一致）
                String wordType = words.get(0).getType();
                String typeIcon = ENWordType.getIconByValue(wordType);
                String typeLabel = ENWordType.getLabelByValue(wordType);
                
                // 计算结束时间
                String timeInfo = calculateTimeInfo(words);
                
                message.append("【").append(typeIcon).append(" ").append(groupName).append("】\n");
                message.append(timeInfo).append(" [").append(typeLabel).append("]\n\n");
                
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
    
    /**
     * 圣诞节词条发放（2025年12月25日签到获得"金勾拜"词条）
     * @param userId 用户ID
     * @return 奖励提示文本
     */
    private String grantChristmasWord(String userId) {
        try {
            // 1. 检查用户是否已经拥有该词条
            BotUserWordExample checkExample = new BotUserWordExample();
            checkExample.createCriteria()
                    .andUserIdEqualTo(userId)
                    .andWordIdEqualTo(ENSystemWord.CHRISTMAS.getId());
            int existCount = userWordMapper.countByExample(checkExample);
                
            if (existCount > 0) {
                // 已经拥有，不重复发放
                return "";
            }
                
            // 2. 发放词条
            BotUserWord userWord = new BotUserWord();
            userWord.setUserId(userId);
            userWord.setWordId(ENSystemWord.CHRISTMAS.getId());
            userWord.setWordContent(ENSystemWord.CHRISTMAS.getWord());
            userWord.setRarity(ENSystemWord.CHRISTMAS.getRariy());
            userWord.setMerit(ENSystemWord.CHRISTMAS.getMerit());
            userWord.setFetchDate(DateUtil.now());
            userWordMapper.insert(userWord);
                
            // 3. 增加用户魅力值
            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
            scoreExample.createCriteria().andUserIdEqualTo(userId);
            List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(scoreExample);
                
            if (!CollectionUtil.isEmpty(scores)) {
                BotGameUserScore userScore = scores.get(0);
                int currentMerit = userScore.getAccumulateMerit() != null ? userScore.getAccumulateMerit() : 0;
                userScore.setAccumulateMerit(currentMerit + ENSystemWord.CHRISTMAS.getMerit());
                gameUserScoreMapper.updateByPrimaryKey(userScore);
            }
                
            // 4. 返回奖励提示
            String rarityLabel = ENWordRarity.getLabelByValue(ENSystemWord.CHRISTMAS.getRariy());
            return String.format("🎄圣诞快乐！获得特殊词条『%s』[%s] 魅力+%d\r\n\r\n",
                    ENSystemWord.CHRISTMAS.getWord(),
                    rarityLabel,
                    ENSystemWord.CHRISTMAS.getMerit());
                        
        } catch (Exception e) {
            log.error("发放圣诞词条异常", e);
            return "";
        }
    }
    
    /**
     * 跨年词条发放（2025年12月31日至2026年1月1日签到获得"夜未央"词条）
     * @param userId 用户ID
     * @return 奖励提示文本
     */
    private String grantNewYearWord(String userId) {
        try {
            // 1. 检查用户是否已经拥有该词条
            BotUserWordExample checkExample = new BotUserWordExample();
            checkExample.createCriteria()
                    .andUserIdEqualTo(userId)
                    .andWordIdEqualTo(ENSystemWord.NEW_YEAR.getId());
            int existCount = userWordMapper.countByExample(checkExample);
                
            if (existCount > 0) {
                // 已经拥有，不重复发放
                return "";
            }
                
            // 2. 发放词条
            BotUserWord userWord = new BotUserWord();
            userWord.setUserId(userId);
            userWord.setWordId(ENSystemWord.NEW_YEAR.getId());
            userWord.setWordContent(ENSystemWord.NEW_YEAR.getWord());
            userWord.setRarity(ENSystemWord.NEW_YEAR.getRariy());
            userWord.setMerit(ENSystemWord.NEW_YEAR.getMerit());
            userWord.setFetchDate(DateUtil.now());
            userWordMapper.insert(userWord);
                
            // 3. 增加用户魅力值
            BotGameUserScoreExample scoreExample = new BotGameUserScoreExample();
            scoreExample.createCriteria().andUserIdEqualTo(userId);
            List<BotGameUserScore> scores = gameUserScoreMapper.selectByExample(scoreExample);
                
            if (!CollectionUtil.isEmpty(scores)) {
                BotGameUserScore userScore = scores.get(0);
                int currentMerit = userScore.getAccumulateMerit() != null ? userScore.getAccumulateMerit() : 0;
                userScore.setAccumulateMerit(currentMerit + ENSystemWord.NEW_YEAR.getMerit());
                gameUserScoreMapper.updateByPrimaryKey(userScore);
            }
                
            // 4. 返回奖励提示
            String rarityLabel = ENWordRarity.getLabelByValue(ENSystemWord.NEW_YEAR.getRariy());
            return String.format("🎆跨年快乐！获得特殊词条『%s』[%s] 魅力+%d\r\n\r\n",
                    ENSystemWord.NEW_YEAR.getWord(),
                    rarityLabel,
                    ENSystemWord.NEW_YEAR.getMerit());
                        
        } catch (Exception e) {
            log.error("发放跨年词条异常", e);
            return "";
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
