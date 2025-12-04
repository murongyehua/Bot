package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifeGameStatus;
import com.bot.life.dao.entity.LifeMap;
import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifeWorldBoss;
import com.bot.life.dao.entity.LifeRealmConfig;
import com.bot.life.dao.entity.LifeMonsterSkill;
import com.bot.life.dao.entity.LifeBattleState;
import com.bot.life.dao.entity.LifeShop;
import com.bot.life.dao.entity.LifePlayerItem;
import com.bot.life.dao.entity.LifeItem;
import com.bot.life.dao.entity.LifeMonsterDrop;
import com.bot.life.dao.mapper.LifeGameStatusMapper;
import com.bot.life.dao.mapper.LifeRealmConfigMapper;
import com.bot.life.dao.mapper.LifeMonsterSkillMapper;
import com.bot.life.dao.mapper.LifeBattleStateMapper;
import com.bot.life.dao.mapper.LifePlayerSkillMapper;
import com.bot.life.dao.mapper.LifeShopMapper;
import com.bot.life.dao.mapper.LifePlayerItemMapper;
import com.bot.life.dao.mapper.LifeMonsterDropMapper;
import com.bot.life.enums.ENAttribute;
import com.bot.life.enums.ENGameMode;
import com.bot.life.enums.ENItemType;
import com.bot.life.service.ImageGenerationService;
import com.bot.life.service.LifeHandler;
import com.bot.life.service.PlayerService;
import com.bot.life.service.MapService;
import com.bot.life.service.ExplorationService;
import com.bot.life.service.InventoryService;
import com.bot.life.service.AchievementService;
import com.bot.life.service.FriendService;
import com.bot.life.service.HealthRecoveryService;
import com.bot.life.service.WorldBossService;
import com.bot.life.service.MarketService;
import com.bot.life.service.MailService;
import com.bot.life.service.SkillService;
import com.bot.life.service.TeamService;
import com.bot.life.service.RealmService;
import com.bot.life.service.SigninService;
import com.bot.life.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 浮生卷游戏处理实现
 * @author Assistant
 */
@Service
public class LifeHandlerImpl implements LifeHandler {
    
    @Autowired
    private LifeGameStatusMapper gameStatusMapper;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private ImageGenerationService imageGenerationService;
    
    @Autowired
    private MapService mapService;
    
    @Autowired
    private ExplorationService explorationService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private FriendService friendService;
    
    @Autowired
    private WorldBossService worldBossService;
    
    @Autowired
    private HealthRecoveryService healthRecoveryService;
    
    @Autowired
    private MarketService marketService;
    
    @Autowired
    private MailService mailService;
    
    @Autowired
    private SkillService skillService;
    
    @Autowired
    private TeamService teamService;
    
    @Autowired
    private RealmService realmService;
    
    @Autowired
    private SigninService signinService;
    
    @Autowired
    private BattleService battleService;
    
    @Autowired
    private LifeRealmConfigMapper realmConfigMapper;
    
    @Autowired
    private LifeMonsterSkillMapper monsterSkillMapper;
    
    @Autowired
    private LifeBattleStateMapper battleStateMapper;
    
    @Autowired
    private LifePlayerSkillMapper playerSkillMapper;
    
    @Autowired
    private LifeShopMapper shopMapper;
    
    @Autowired
    private LifePlayerItemMapper playerItemMapper;
    
    @Autowired
    private LifeMonsterDropMapper monsterDropMapper;
    
    @Override
    public String exit(String userId) {
        // 重置游戏状态
        LifeGameStatus gameStatus = getOrCreateGameStatus(userId);
        gameStatus.setGameMode(ENGameMode.NOT_ENTERED.getCode());
        gameStatus.setCurrentMenu(null);
        gameStatus.setContextData(null);
        gameStatus.setUpdateTime(new Date());
        gameStatusMapper.updateByPrimaryKey(gameStatus);
        
        // 清理可能存在的战斗状态
        LifePlayer player = playerService.getPlayerByUserId(userId);
        if (player != null) {
            finishBattle(player.getId());
        }
        
        return "已退出浮生卷游戏模式";
    }
    
    @Override
    public String play(String reqContent, String userId) {
        LifeGameStatus gameStatus = getOrCreateGameStatus(userId);
        ENGameMode currentMode = ENGameMode.getByCode(gameStatus.getGameMode());
        
        switch (currentMode) {
            case NOT_ENTERED:
                return handleGameEntry(reqContent, userId, gameStatus);
            case PREPARE:
                return handlePrepareMode(reqContent, userId, gameStatus);
            case IN_GAME:
                return handleInGameMode(reqContent, userId, gameStatus);
            case GHOST_MARKET:
                return handleGhostMarketMode(reqContent, userId, gameStatus);
            case BATTLE:
                return handleBattleMode(reqContent, userId, gameStatus);
            default:
                return "游戏状态异常，请重新进入";
        }
    }
    
    @Override
    public String manage(String reqContent) {
        // TODO: 实现管理员功能
        return "管理员功能待实现";
    }
    
    private String handleGameEntry(String reqContent, String userId, LifeGameStatus gameStatus) {
        if ("浮生卷".equals(reqContent.trim())) {
            // 进入预备游戏模式
            gameStatus.setGameMode(ENGameMode.PREPARE.getCode());
            gameStatus.setUpdateTime(new Date());
            gameStatusMapper.updateByPrimaryKey(gameStatus);
            
            String imagePath = imageGenerationService.generateGameImage(
                "『欢迎来到浮生卷』\n\n" +
                "这是一个修仙文字游戏\n" +
                "在这里你将踏上修仙之路\n" +
                "体验不同的修仙人生\n\n" +
                "发送『1』正式进入游戏\n" +
                "发送其他内容返回\n" +
                "游戏中可随时发送『退出』返回正常模式"
            );
            
            return imagePath;
        }
        
        return null; // 不处理其他内容
    }
    
    private String handlePrepareMode(String reqContent, String userId, LifeGameStatus gameStatus) {
        if ("1".equals(reqContent.trim())) {
            // 正式进入游戏
            gameStatus.setGameMode(ENGameMode.IN_GAME.getCode());
            gameStatus.setUpdateTime(new Date());
            gameStatusMapper.updateByPrimaryKey(gameStatus);
            
            // 检查是否有角色
            LifePlayer player = playerService.getPlayerByUserId(userId);
            if (player == null) {
                return showCharacterCreation();
            } else {
                return showMainMenu(userId);
            }
        } else {
            // 退出预备状态
            gameStatus.setGameMode(ENGameMode.NOT_ENTERED.getCode());
            gameStatus.setUpdateTime(new Date());
            gameStatusMapper.updateByPrimaryKey(gameStatus);
            
            return "已取消进入游戏";
        }
    }
    
    private String handleInGameMode(String reqContent, String userId, LifeGameStatus gameStatus) {
        if ("退出".equals(reqContent.trim())) {
            return exit(userId);
        }
        
        LifePlayer player = playerService.getPlayerByUserId(userId);
        
        // 如果没有角色，处理角色创建
        if (player == null) {
            return handleCharacterCreation(reqContent, userId);
        }
        
        // 检查是否有当前菜单状态
        String currentMenu = gameStatus.getCurrentMenu();
        if ("TELEPORT_SELECT".equals(currentMenu)) {
            return handleTeleportSelect(reqContent, userId, gameStatus, player);
        }
        
        // 检查是否是使用道具指令
        if (reqContent.startsWith("使用")) {
            return handleUseItem(reqContent, userId, player);
        }
        
        // 检查是否是好友相关指令
        if (reqContent.startsWith("添加好友")) {
            return handleAddFriend(reqContent, userId, player);
        }
        if (reqContent.startsWith("同意好友")) {
            return handleAcceptFriend(reqContent, userId, player);
        }
        if (reqContent.startsWith("拒绝好友")) {
            return handleRejectFriend(reqContent, userId, player);
        }
        
        // 检查是否是世界BOSS相关指令
        if (reqContent.equals("挑战世界BOSS")) {
            return handleChallengeWorldBoss(reqContent, userId, player);
        }
        
        // 检查是否是战斗相关指令
        if (reqContent.equals("战斗")) {
            return handleBattle(userId, player);
        }
        if (reqContent.equals("逃跑")) {
            return handleEscape(userId, player);
        }
        
        // 检查是否是邮件相关指令
        if (reqContent.startsWith("读取邮件")) {
            return handleReadMail(reqContent, userId, player);
        }
        if (reqContent.startsWith("领取附件")) {
            return handleReceiveMailAttachment(reqContent, userId, player);
        }
        if (reqContent.startsWith("发送邮件")) {
            return handleSendMail(reqContent, userId, player);
        }
        if (reqContent.startsWith("删除邮件")) {
            return handleDeleteMail(reqContent, userId, player);
        }
        
        // 检查是否是技能相关指令
        if (reqContent.equals("可学技能")) {
            return handleAvailableSkills(reqContent, userId, player);
        }
        if (reqContent.startsWith("学习技能")) {
            return handleLearnSkill(reqContent, userId, player);
        }
        
        // 检查是否是组队相关指令
        if (reqContent.equals("查看队伍")) {
            return handleViewAvailableTeams(reqContent, userId, player);
        }
        if (reqContent.startsWith("创建队伍")) {
            return handleCreateTeam(reqContent, userId, player);
        }
        if (reqContent.startsWith("加入队伍")) {
            return handleJoinTeam(reqContent, userId, player);
        }
        if (reqContent.startsWith("同意队员")) {
            return handleAcceptTeamMember(reqContent, userId, player);
        }
        if (reqContent.startsWith("拒绝队员")) {
            return handleRejectTeamMember(reqContent, userId, player);
        }
        if (reqContent.equals("离开队伍")) {
            return handleLeaveTeam(reqContent, userId, player);
        }
        if (reqContent.equals("解散队伍")) {
            return handleDisbandTeam(reqContent, userId, player);
        }
        
        // 处理游戏指令
        return handleGameCommand(reqContent, userId, player);
    }
    
    private String showCharacterCreation() {
        StringBuilder content = new StringBuilder();
        content.append("『角色创建』\n\n");
        content.append("请输入角色信息，格式：昵称-派系\n");
        content.append("例如：张三-金\n\n");
        content.append("『派系介绍』\n\n");
        content.append("『金』：以强大的破坏力闻名，擅长一力破万法\n");
        content.append("攻击力★★★★★ 防御力★★ 养成难度★★★★\n\n");
        content.append("『木』：拥有强大的恢复能力，治疗和用毒都是一绝\n");
        content.append("攻击力★★★ 防御力★★★★ 养成难度★★★\n\n");
        content.append("『水』：流水不争先但滔滔不绝，各个领域都有涉及\n");
        content.append("攻击力★★★★ 防御力★★★★ 养成难度★★★★\n\n");
        content.append("『火』：擅长持续造成伤害，也拥有可观的爆发力\n");
        content.append("攻击力★★★★ 防御力★★★ 养成难度★★★\n\n");
        content.append("『土』：能为自己和队友提供超强的防御，也有不俗的控制能力\n");
        content.append("攻击力★★★ 防御力★★★★★ 养成难度★★★★★");
        
        return imageGenerationService.generateGameImage(content.toString());
    }
    
    private String handleCharacterCreation(String reqContent, String userId) {
        String[] parts = reqContent.trim().split("-");
        if (parts.length != 2) {
            return imageGenerationService.generateGameImage("格式错误！请按照格式输入：昵称-派系");
        }
        
        String nickname = parts[0].trim();
        String attributeStr = parts[1].trim();
        
        // 验证昵称
        if (nickname.length() > 7) {
            return imageGenerationService.generateGameImage("昵称不能超过7个字！");
        }
        
        if (!nickname.matches("[\\u4e00-\\u9fa5]+")) {
            return imageGenerationService.generateGameImage("昵称只能包含中文！");
        }
        
        if (!playerService.isNicknameAvailable(nickname)) {
            return imageGenerationService.generateGameImage("昵称已被使用，请选择其他昵称！");
        }
        
        // 验证派系
        Integer attribute = getAttributeByName(attributeStr);
        if (attribute == null) {
            return imageGenerationService.generateGameImage("派系错误！请选择：金、木、水、火、土");
        }
        
        // 创建角色
        boolean success = playerService.createPlayer(userId, nickname, attribute);
        if (success) {
            String content = String.format("『角色创建成功！』\n\n欢迎『%s』踏上修仙之路！\n派系：『%s』\n\n开始你的修仙之旅吧！\n\n输入任意内容进入游戏主菜单", 
                                          nickname, attributeStr);
            String imagePath = imageGenerationService.generateGameImage(content);
            
            // 延迟显示主菜单
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return imagePath;
        } else {
            return imageGenerationService.generateGameImage("角色创建失败，请重试！");
        }
    }
    
    private String showMainMenu(String userId) {
        LifePlayer player = playerService.getPlayerByUserId(userId);
        // 只做必要的数据更新，不显示多余信息
        LifeRealmConfig currentRealm = realmConfigMapper.selectByLevel(player.getLevel());
        Long maxCultivation = currentRealm != null ? currentRealm.getMaxCultivation() : null;
        
        // 清除菜单状态
        LifeGameStatus gameStatus = getOrCreateGameStatus(userId);
        gameStatus.setCurrentMenu(null);
        gameStatus.setUpdateTime(new Date());
        gameStatusMapper.updateByPrimaryKey(gameStatus);
        
        // 检查血量自动恢复
        int oldHealth = player.getHealth();
        healthRecoveryService.checkAndRecoverHealth(player.getId());
        // 重新获取玩家数据查看是否有血量恢复
        player = playerService.getPlayerByUserId(userId);
        
        long gainedCultivation = player.gainCultivation(maxCultivation);
        player.recoverStamina();
        playerService.updatePlayer(player);
        
        StringBuilder content = new StringBuilder();
        content.append("『浮生卷主菜单』\n\n");
        
        // 显示血量恢复提示
        if (player.getHealth() > oldHealth) {
            int recovered = player.getHealth() - oldHealth;
            content.append(String.format("『自动恢复』血量恢复了%d点！当前血量：%d/%d\n\n", 
                recovered, player.getHealth(), player.getMaxHealth()));
        }
        content.append("『 功能菜单 』\n");
        content.append("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓\n");
        content.append("┃ 0. 每日签到    │ 1. 角色信息    │ 2. 游历探索    ┃\n");
        content.append("┃ 3. 地图传送    │ 4. 查看背包    │ 5. 鬼市交易    ┃\n");
        content.append("┃ 6. 好友系统    │ 7. 邮件中心    │ 8. 成就记录    ┃\n");
        content.append("┃ 9. 技能修炼    │ 11. 境界突破   │              ┃\n");
        content.append("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n\n");
        
        // 显示世界BOSS信息（如果有）
        String worldBossInfo = worldBossService.getWorldBossInfo(player.getCurrentMapId());
        if (!worldBossInfo.isEmpty()) {
            content.append(worldBossInfo).append("\n");
        }
        
        content.append("请输入对应数字选择操作");
        
        return imageGenerationService.generateGameImageWithStatus(content.toString(), player);
    }
    
    private String handleGameCommand(String reqContent, String userId, LifePlayer player) {
        String command = reqContent.trim();
        
        switch (command) {
            case "0":
                return handleSignin(userId, player);
            case "1":
                return imageGenerationService.generatePlayerStatusImage(userId);
            case "2":
                return handleExplore(userId, player);
            case "3":
                return handleTeleport(userId, player);
            case "4":
                return handleInventory(userId, player);
            case "5":
                return handleMarket(userId, player);
            case "6":
                return handleFriends(userId, player);
            case "7":
                return handleMail(userId, player);
            case "8":
                return handleAchievements(userId, player);
            case "9":
                return handleSkills(userId, player);
            case "11":
                return handleRealm(userId, player);
            default:
                // 检查是否是境界相关命令
                if ("境界".equals(command)) {
                    return handleRealm(userId, player);
                } else if ("突破".equals(command)) {
                    return handleBreakthrough(userId, player);
                } else if ("签到".equals(command)) {
                    return handleSignin(userId, player);
                }
                return showMainMenu(userId);
        }
    }
    
    private String handleExplore(String userId, LifePlayer player) {
        String result = explorationService.explore(player);
        return imageGenerationService.generateGameImage(result);
    }
    
    private String handleTeleport(String userId, LifePlayer player) {
        StringBuilder content = new StringBuilder();
        content.append("『传送功能』\n\n");
        content.append("选择要传送的地图：\n\n");
        
        List<LifeMap> availableMaps = mapService.getAvailableMaps(player);
        if (availableMaps.isEmpty()) {
            content.append("暂无可传送的地图");
        } else {
            for (int i = 0; i < availableMaps.size(); i++) {
                LifeMap map = availableMaps.get(i);
                content.append(String.format("%d. %s（需要%d级）\n", 
                             i + 1, map.getName(), map.getMinLevel()));
            }
            content.append("\n发送对应数字进行传送\n发送『返回』回到主菜单");
        }
        
        // 设置当前菜单状态为传送选择
        LifeGameStatus gameStatus = getOrCreateGameStatus(userId);
        gameStatus.setCurrentMenu("TELEPORT_SELECT");
        gameStatus.setUpdateTime(new Date());
        gameStatusMapper.updateByPrimaryKey(gameStatus);
        
        return imageGenerationService.generateGameImageWithStatus(content.toString(), player);
    }
    
    private String handleTeleportSelect(String reqContent, String userId, LifeGameStatus gameStatus, LifePlayer player) {
        String command = reqContent.trim();
        
        // 处理返回命令
        if ("返回".equals(command)) {
            gameStatus.setCurrentMenu(null);
            gameStatus.setUpdateTime(new Date());
            gameStatusMapper.updateByPrimaryKey(gameStatus);
            return showMainMenu(userId);
        }
        
        // 处理数字选择
        try {
            int choice = Integer.parseInt(command);
            List<LifeMap> availableMaps = mapService.getAvailableMaps(player);
            
            if (choice > 0 && choice <= availableMaps.size()) {
                LifeMap selectedMap = availableMaps.get(choice - 1);
                
                // 检查等级要求
                if (player.getLevel() < selectedMap.getMinLevel()) {
                    return imageGenerationService.generateGameImageWithStatus(
                        String.format("『传送失败』\n\n%s需要%d级才能进入！\n你当前等级：%d\n\n发送『返回』回到主菜单", 
                        selectedMap.getName(), selectedMap.getMinLevel(), player.getLevel()), player);
                }
                
                // 执行传送
                String teleportResult = mapService.teleportToMap(player, selectedMap.getId());
                
                // 清除菜单状态
                gameStatus.setCurrentMenu(null);
                gameStatus.setUpdateTime(new Date());
                gameStatusMapper.updateByPrimaryKey(gameStatus);
                
                return imageGenerationService.generateGameImageWithStatus(
                    teleportResult + "\n\n输入任意内容返回主菜单", player);
            } else {
                return imageGenerationService.generateGameImageWithStatus(
                    "选择无效！请重新选择或发送『返回』回到主菜单", player);
            }
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImageWithStatus(
                "请输入数字选择地图或发送『返回』回到主菜单", player);
        }
    }
    
    private String handleInventory(String userId, LifePlayer player) {
        String inventoryDisplay = inventoryService.getInventoryDisplay(player);
        return imageGenerationService.generateGameImageWithStatus(inventoryDisplay, player);
    }
    
    private String handleMarket(String userId, LifePlayer player) {
        // 进入鬼市状态
        LifeGameStatus gameStatus = getOrCreateGameStatus(userId);
        gameStatus.setGameMode(ENGameMode.GHOST_MARKET.getCode());
        gameStatus.setUpdateTime(new Date());
        gameStatusMapper.updateByPrimaryKey(gameStatus);
        
        // 更新玩家位置到鬼市（地图ID：5）
        player.setCurrentMapId(5L);
        playerService.updatePlayer(player);
        
        // 重新获取玩家数据确保最新状态
        LifePlayer refreshedPlayer = playerService.getPlayerByUserId(userId);
        String marketMenu = marketService.getMarketMainMenu(refreshedPlayer);
        return imageGenerationService.generateGameImageWithStatus(marketMenu, refreshedPlayer);
    }
    
    private String handleFriends(String userId, LifePlayer player) {
        StringBuilder content = new StringBuilder();
        
        // 显示好友列表
        String friendList = friendService.getFriendListDisplay(player);
        content.append(friendList);
        
        // 显示待处理申请
        String pendingRequests = friendService.getPendingFriendRequests(player);
        if (!pendingRequests.contains("暂无待处理")) {
            content.append("\n\n").append(pendingRequests);
        }
        
        return imageGenerationService.generateGameImage(content.toString());
    }
    
    private String handleMail(String userId, LifePlayer player) {
        String mailMenu = mailService.getMailMainMenu(player);
        return imageGenerationService.generateGameImage(mailMenu);
    }
    
    private String handleUseItem(String reqContent, String userId, LifePlayer player) {
        try {
            String itemIdStr = reqContent.replace("使用", "").trim();
            Long itemId = Long.parseLong(itemIdStr);
            
            String result = inventoryService.useItem(player, itemId);
            return imageGenerationService.generateGameImage(result);
            
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImage("道具ID格式错误！请使用：使用+道具ID");
        }
    }
    
    private String handleAchievements(String userId, LifePlayer player) {
        String achievements = achievementService.getPlayerAchievements(player.getId());
        return imageGenerationService.generateGameImage(achievements);
    }
    
    private String handleAddFriend(String reqContent, String userId, LifePlayer player) {
        String friendNickname = reqContent.replace("添加好友", "").trim();
        if (friendNickname.isEmpty()) {
            return imageGenerationService.generateGameImage("请输入好友昵称！格式：添加好友+昵称");
        }
        
        String result = friendService.addFriend(player, friendNickname);
        return imageGenerationService.generateGameImage(result);
    }
    
    private String handleAcceptFriend(String reqContent, String userId, LifePlayer player) {
        try {
            String friendIdStr = reqContent.replace("同意好友", "").trim();
            Long friendId = Long.parseLong(friendIdStr);
            
            String result = friendService.acceptFriend(player, friendId);
            return imageGenerationService.generateGameImage(result);
            
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImage("好友ID格式错误！请使用：同意好友+好友ID");
        }
    }
    
    private String handleRejectFriend(String reqContent, String userId, LifePlayer player) {
        try {
            String friendIdStr = reqContent.replace("拒绝好友", "").trim();
            Long friendId = Long.parseLong(friendIdStr);
            
            String result = friendService.rejectFriend(player, friendId);
            return imageGenerationService.generateGameImage(result);
            
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImage("好友ID格式错误！请使用：拒绝好友+好友ID");
        }
    }
    
    private String handleChallengeWorldBoss(String reqContent, String userId, LifePlayer player) {
        // 获取当前地图的世界BOSS
        List<LifeWorldBoss> worldBosses = worldBossService.getCurrentActiveWorldBosses();
        Optional<LifeWorldBoss> currentMapBoss = worldBosses.stream()
                .filter(wb -> wb.getMapId().equals(player.getCurrentMapId()))
                .findFirst();
        
        if (!currentMapBoss.isPresent()) {
            return imageGenerationService.generateGameImage("当前地图没有世界BOSS或不在活动时间内！");
        }
        
        String result = worldBossService.challengeWorldBoss(player, currentMapBoss.get().getId());
        return imageGenerationService.generateGameImage(result);
    }
    
    private String handleReadMail(String reqContent, String userId, LifePlayer player) {
        try {
            String mailIdStr = reqContent.replace("读取邮件", "").trim();
            Long mailId = Long.parseLong(mailIdStr);
            
            String result = mailService.readMail(player, mailId);
            return imageGenerationService.generateGameImage(result);
            
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImage("邮件ID格式错误！请使用：读取邮件+邮件ID");
        }
    }
    
    private String handleReceiveMailAttachment(String reqContent, String userId, LifePlayer player) {
        try {
            String mailIdStr = reqContent.replace("领取附件", "").trim();
            Long mailId = Long.parseLong(mailIdStr);
            
            String result = mailService.receiveMailAttachment(player, mailId);
            return imageGenerationService.generateGameImage(result);
            
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImage("邮件ID格式错误！请使用：领取附件+邮件ID");
        }
    }
    
    private String handleSendMail(String reqContent, String userId, LifePlayer player) {
        try {
            String content = reqContent.replace("发送邮件", "").trim();
            String[] parts = content.split("\\+", 3);
            
            if (parts.length < 3) {
                return imageGenerationService.generateGameImage("格式错误！请使用：发送邮件+好友昵称+标题+内容");
            }
            
            String receiverNickname = parts[0].trim();
            String title = parts[1].trim();
            String mailContent = parts[2].trim();
            
            String result = mailService.sendMailToFriend(player, receiverNickname, title, mailContent);
            return imageGenerationService.generateGameImage(result);
            
        } catch (Exception e) {
            return imageGenerationService.generateGameImage("发送邮件失败！格式：发送邮件+好友昵称+标题+内容");
        }
    }
    
    private String handleDeleteMail(String reqContent, String userId, LifePlayer player) {
        try {
            String mailIdStr = reqContent.replace("删除邮件", "").trim();
            Long mailId = Long.parseLong(mailIdStr);
            
            String result = mailService.deleteMail(player, mailId);
            return imageGenerationService.generateGameImage(result);
            
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImage("邮件ID格式错误！请使用：删除邮件+邮件ID");
        }
    }
    
    private String handleSkills(String userId, LifePlayer player) {
        String skills = skillService.getPlayerSkills(player);
        return imageGenerationService.generateGameImage(skills);
    }
    
    private String handleAvailableSkills(String reqContent, String userId, LifePlayer player) {
        String availableSkills = skillService.getAvailableSkills(player);
        return imageGenerationService.generateGameImage(availableSkills);
    }
    
    private String handleLearnSkill(String reqContent, String userId, LifePlayer player) {
        try {
            String skillIdStr = reqContent.replace("学习技能", "").trim();
            Long skillId = Long.parseLong(skillIdStr);
            
            String result = skillService.learnSkill(player, skillId);
            return imageGenerationService.generateGameImage(result);
            
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImage("技能ID格式错误！请使用：学习技能+技能ID");
        }
    }
    
    private String handleTeam(String userId, LifePlayer player) {
        String teamInfo = teamService.getTeamInfo(player);
        return imageGenerationService.generateGameImage(teamInfo);
    }
    
    private String handleViewAvailableTeams(String reqContent, String userId, LifePlayer player) {
        String availableTeams = teamService.getAvailableTeams(player);
        return imageGenerationService.generateGameImage(availableTeams);
    }
    
    private String handleCreateTeam(String reqContent, String userId, LifePlayer player) {
        try {
            String teamName = reqContent.replace("创建队伍", "").trim();
            if (teamName.isEmpty()) {
                return imageGenerationService.generateGameImage("请输入队伍名称！格式：创建队伍+队伍名称");
            }
            
            String result = teamService.createTeam(player, teamName);
            return imageGenerationService.generateGameImage(result);
            
        } catch (Exception e) {
            return imageGenerationService.generateGameImage("创建队伍失败！请重试。");
        }
    }
    
    private String handleJoinTeam(String reqContent, String userId, LifePlayer player) {
        try {
            String teamIdStr = reqContent.replace("加入队伍", "").trim();
            Long teamId = Long.parseLong(teamIdStr);
            
            String result = teamService.joinTeam(player, teamId);
            return imageGenerationService.generateGameImage(result);
            
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImage("队伍ID格式错误！请使用：加入队伍+队伍ID");
        }
    }
    
    private String handleAcceptTeamMember(String reqContent, String userId, LifePlayer player) {
        try {
            String playerIdStr = reqContent.replace("同意队员", "").trim();
            Long playerId = Long.parseLong(playerIdStr);
            
            String result = teamService.acceptTeamMember(player, playerId);
            return imageGenerationService.generateGameImage(result);
            
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImage("玩家ID格式错误！请使用：同意队员+玩家ID");
        }
    }
    
    private String handleRejectTeamMember(String reqContent, String userId, LifePlayer player) {
        try {
            String playerIdStr = reqContent.replace("拒绝队员", "").trim();
            Long playerId = Long.parseLong(playerIdStr);
            
            String result = teamService.rejectTeamMember(player, playerId);
            return imageGenerationService.generateGameImage(result);
            
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImage("玩家ID格式错误！请使用：拒绝队员+玩家ID");
        }
    }
    
    private String handleLeaveTeam(String reqContent, String userId, LifePlayer player) {
        String result = teamService.leaveTeam(player);
        return imageGenerationService.generateGameImage(result);
    }
    
    private String handleDisbandTeam(String reqContent, String userId, LifePlayer player) {
        String result = teamService.disbandTeam(player);
        return imageGenerationService.generateGameImage(result);
    }
    
    private Integer getAttributeByName(String attributeName) {
        switch (attributeName) {
            case "金": return ENAttribute.METAL.getCode();
            case "木": return ENAttribute.WOOD.getCode();
            case "水": return ENAttribute.WATER.getCode();
            case "火": return ENAttribute.FIRE.getCode();
            case "土": return ENAttribute.EARTH.getCode();
            default: return null;
        }
    }
    
    private LifeGameStatus getOrCreateGameStatus(String userId) {
        LifeGameStatus gameStatus = gameStatusMapper.selectByUserId(userId);
        if (gameStatus == null) {
            gameStatus = new LifeGameStatus();
            gameStatus.setUserId(userId);
            gameStatus.setGameMode(ENGameMode.NOT_ENTERED.getCode());
            gameStatus.setCreateTime(new Date());
            gameStatus.setUpdateTime(new Date());
            gameStatusMapper.insert(gameStatus);
        }
        return gameStatus;
    }
    
    private String handleRealm(String userId, LifePlayer player) {
        String result = realmService.viewRealmInfo(player.getId());
        return imageGenerationService.generateGameImageWithStatus(result, player);
    }
    
    private String handleBreakthrough(String userId, LifePlayer player) {
        String result = realmService.attemptBreakthrough(player.getId());
        return imageGenerationService.generateGameImageWithStatus(result, player);
    }
    
    private String handleSignin(String userId, LifePlayer player) {
        String result = signinService.signin(player.getId());
        return imageGenerationService.generateGameImageWithStatus(result, player);
    }
    
    private String handleShopBrowse(String reqContent, String userId, LifeGameStatus gameStatus, LifePlayer player) {
        String command = reqContent.trim();
        
        if ("返回".equals(command)) {
            // 返回鬼市主菜单
            gameStatus.setCurrentMenu(null);
            gameStatus.setUpdateTime(new Date());
            gameStatusMapper.updateByPrimaryKey(gameStatus);
            String marketMenu = marketService.getMarketMainMenu(player);
            return imageGenerationService.generateGameImageWithStatus(marketMenu, player);
        }
        
        // 尝试解析为数字选择
        try {
            int index = Integer.parseInt(command);
            String itemDetail = marketService.getShopItemDetail(index, player);
            
            // 设置为商品详情状态，并记录选中的商品索引
            gameStatus.setCurrentMenu("SHOP_ITEM_DETAIL:" + index);
            gameStatus.setUpdateTime(new Date());
            gameStatusMapper.updateByPrimaryKey(gameStatus);
            
            return imageGenerationService.generateGameImageWithStatus(itemDetail, player);
        } catch (NumberFormatException e) {
            return imageGenerationService.generateGameImageWithStatus(
                "请输入数字选择商品或发送『返回』", player);
        }
    }
    
    private String handleShopItemDetail(String reqContent, String userId, LifeGameStatus gameStatus, LifePlayer player) {
        String command = reqContent.trim();
        
        if ("返回".equals(command)) {
            // 返回商店列表
            gameStatus.setCurrentMenu("SHOP_BROWSE");
            gameStatus.setUpdateTime(new Date());
            gameStatusMapper.updateByPrimaryKey(gameStatus);
            String shop = marketService.getMysteriousShop();
            return imageGenerationService.generateGameImageWithStatus(shop, player);
        }
        
        // 处理购买命令，格式：购买 数量
        if (command.startsWith("购买")) {
            String[] parts = command.split("\\s+");
            if (parts.length != 2) {
                return imageGenerationService.generateGameImageWithStatus(
                    "格式错误！正确格式：购买 数量\n例如：购买 1", player);
            }
            
            try {
                int quantity = Integer.parseInt(parts[1]);
                
                // 从currentMenu中提取商品索引
                String menuState = gameStatus.getCurrentMenu();
                int itemIndex = Integer.parseInt(menuState.split(":")[1]);
                
                // 获取商品列表
                List<LifeShop> shopItems = shopMapper.selectAll();
                if (itemIndex < 1 || itemIndex > shopItems.size()) {
                    return imageGenerationService.generateGameImageWithStatus("商品不存在！", player);
                }
                
                LifeShop shopItem = shopItems.get(itemIndex - 1);
                
                // 检查灵粹是否足够
                long totalCost = (long) shopItem.getCurrentPrice() * quantity;
                long playerSpirit = player.getSpirit() != null ? player.getSpirit() : 0;
                
                if (playerSpirit < totalCost) {
                    return imageGenerationService.generateGameImageWithStatus(
                        String.format("『购买失败』\n\n灵粹不足！\n需要：%d灵粹\n你有：%d灵粹", 
                        totalCost, playerSpirit), player);
                }
                
                // 扣除灵粹
                player.setSpirit(playerSpirit - totalCost);
                playerService.updatePlayer(player);
                
                // 添加道具到背包
                inventoryService.addItem(player.getId(), shopItem.getItemId(), quantity);
                
                // 返回商店列表
                gameStatus.setCurrentMenu("SHOP_BROWSE");
                gameStatus.setUpdateTime(new Date());
                gameStatusMapper.updateByPrimaryKey(gameStatus);
                
                String resultMsg = String.format("『购买成功』\n\n购买了%d个『%s』\n花费%d灵粹\n剩余灵粹：%d\n\n%s", 
                    quantity, shopItem.getItem().getName(), totalCost, player.getSpirit(), 
                    marketService.getMysteriousShop());
                return imageGenerationService.generateGameImageWithStatus(resultMsg, player);
                
            } catch (NumberFormatException e) {
                return imageGenerationService.generateGameImageWithStatus(
                    "数量格式错误！请输入正整数", player);
            }
        }
        
        return imageGenerationService.generateGameImageWithStatus(
            "未知命令！发送『购买 数量』购买或发送『返回』", player);
    }
    
    private String handleGhostMarketMode(String reqContent, String userId, LifeGameStatus gameStatus) {
        String command = reqContent.trim();
        
        if ("退出".equals(command) || "返回".equals(command)) {
            // 返回游戏主界面，恢复到新手村（地图ID：1）
            LifePlayer player = playerService.getPlayerByUserId(userId);
            if (player != null) {
                player.setCurrentMapId(1L); // 回到新手村
                playerService.updatePlayer(player);
            }
            
            gameStatus.setGameMode(ENGameMode.IN_GAME.getCode());
            gameStatus.setUpdateTime(new Date());
            gameStatusMapper.updateByPrimaryKey(gameStatus);
            return showMainMenu(userId);
        }
        
        LifePlayer player = playerService.getPlayerByUserId(userId);
        if (player == null) {
            return imageGenerationService.generateGameImage("角色不存在！");
        }
        
        // 检查是否在商店子菜单
        String currentMenu = gameStatus.getCurrentMenu();
        if ("SHOP_BROWSE".equals(currentMenu)) {
            return handleShopBrowse(reqContent, userId, gameStatus, player);
        } else if ("SHOP_ITEM_DETAIL".equals(currentMenu)) {
            return handleShopItemDetail(reqContent, userId, gameStatus, player);
        }
        
        String result;
        switch (command) {
            case "1":
                // 访问神秘商人商店
                result = marketService.getMysteriousShop();
                gameStatus.setCurrentMenu("SHOP_BROWSE");
                gameStatus.setUpdateTime(new Date());
                gameStatusMapper.updateByPrimaryKey(gameStatus);
                break;
            case "2":
                // 查看玩家摊位
                result = marketService.getAllPlayerStalls();
                break;
            case "3":
                // 创建摊位（这里简化处理，实际需要更复杂的交互）
                result = "『创建摊位』\n\n请发送格式：摆摊+道具ID+数量+单价\n例如：摆摊1+10+100";
                break;
            default:
                // 检查是否是其他鬼市相关命令
                if (command.startsWith("购买")) {
                    // 处理购买命令，格式：购买+商品ID+数量
                    String[] parts = command.split("\\+");
                    if (parts.length == 3) {
                        try {
                            long itemId = Long.parseLong(parts[1]);
                            int quantity = Integer.parseInt(parts[2]);
                            result = marketService.buyFromShop(player, itemId, quantity);
                        } catch (NumberFormatException e) {
                            result = "命令格式错误！正确格式：购买+商品ID+数量";
                        }
                    } else {
                        result = "命令格式错误！正确格式：购买+商品ID+数量";
                    }
                } else if (command.startsWith("出售")) {
                    // 处理出售命令，格式：出售+道具ID+数量
                    String[] parts = command.split("\\+");
                    if (parts.length == 3) {
                        try {
                            long itemId = Long.parseLong(parts[1]);
                            int quantity = Integer.parseInt(parts[2]);
                            result = marketService.sellToShop(player, itemId, quantity);
                        } catch (NumberFormatException e) {
                            result = "命令格式错误！正确格式：出售+道具ID+数量";
                        }
                    } else {
                        result = "命令格式错误！正确格式：出售+道具ID+数量";
                    }
                } else if (command.startsWith("摆摊")) {
                    // 处理摆摊命令，格式：摆摊+道具ID+数量+单价
                    String[] parts = command.split("\\+");
                    if (parts.length == 4) {
                        try {
                            long itemId = Long.parseLong(parts[1]);
                            int quantity = Integer.parseInt(parts[2]);
                            int unitPrice = Integer.parseInt(parts[3]);
                            result = marketService.createPlayerStall(player, "摊位", 1, itemId, quantity, unitPrice);
                        } catch (NumberFormatException e) {
                            result = "命令格式错误！正确格式：摆摊+道具ID+数量+单价";
                        }
                    } else {
                        result = "命令格式错误！正确格式：摆摊+道具ID+数量+单价";
                    }
                } else if (command.startsWith("买")) {
                    // 处理从玩家摊位购买，格式：买+摊位ID+数量
                    String[] parts = command.split("\\+");
                    if (parts.length == 3) {
                        try {
                            long stallId = Long.parseLong(parts[1]);
                            int quantity = Integer.parseInt(parts[2]);
                            result = marketService.buyFromPlayerStall(player, stallId, quantity);
                        } catch (NumberFormatException e) {
                            result = "命令格式错误！正确格式：买+摊位ID+数量";
                        }
                    } else {
                        result = "命令格式错误！正确格式：买+摊位ID+数量";
                    }
                } else {
                    // 重新显示鬼市菜单
                    result = marketService.getMarketMainMenu(player);
                }
                break;
        }
        
        return imageGenerationService.generateGameImageWithStatus(result, player);
    }
    
    private String handleBattle(String userId, LifePlayer player) {
        // 检查体力
        if (!explorationService.hasEnoughStamina(player)) {
            return imageGenerationService.generateGameImageWithStatus("体力不足，无法战斗！", player);
        }
        
        // 进入战斗模式
        LifeGameStatus gameStatus = getOrCreateGameStatus(userId);
        gameStatus.setGameMode(ENGameMode.BATTLE.getCode());
        gameStatus.setUpdateTime(new Date());
        gameStatusMapper.updateByPrimaryKey(gameStatus);
        
        // 消耗体力
        explorationService.consumeStamina(player, 1);
        
        // 创建战斗状态
        Long monsterId = 1L; // 山贼ID
        LifeBattleState battleState = new LifeBattleState();
        battleState.setPlayerId(player.getId());
        battleState.setMonsterId(monsterId);
        battleState.setCurrentTurn(1);
        battleState.setPlayerHp(player.getHealth());
        battleState.setMonsterHp(200);
        battleState.setMonsterMaxHp(200);
        battleState.setMonsterSkillCooldowns("{}"); // 初始无冷却
        battleState.setPlayerBuffs("{}");
        battleState.setMonsterBuffs("{}");
        battleState.setCreateTime(new Date());
        battleState.setUpdateTime(new Date());
        
        // 删除可能存在的旧战斗状态，插入新的
        battleStateMapper.deleteByPlayerId(player.getId());
        battleStateMapper.insert(battleState);
        
        // 重置玩家技能冷却
        resetPlayerSkillCooldowns(player.getId());
        
        // 更新最后战斗时间
        healthRecoveryService.updateLastBattleTime(player.getId());
        
        // 模拟遭遇怪物，开始战斗
        String battleStart = "『战斗开始！』\n\n遭遇『山贼』！\n\n" +
                           "敌人：血量200/200|攻击25|防御8|速度15\n" +
                           "你：血量" + player.getHealth() + "/" + player.getMaxHealth() + 
                           "|攻击" + player.getAttackPower() + 
                           "|防御" + player.getDefense() + 
                           "|速度" + player.getSpeed() + "\n\n" +
                           "回合1 请选择行动：\n1.攻击 2.防御 3.技能 4.道具 5.逃跑";
        
        return imageGenerationService.generateGameImageWithStatus(battleStart, player);
    }
    
    private String handleEscape(String userId, LifePlayer player) {
        // 简化的逃跑处理
        String result = "『逃跑成功！』\n\n你成功逃离了危险！\n\n体力-1";
        
        // 消耗体力
        player.setStamina(Math.max(0, player.getStamina() - 1));
        playerService.updatePlayer(player);
        
        return imageGenerationService.generateGameImageWithStatus(result, player);
    }
    
    private String handleBattleMode(String reqContent, String userId, LifeGameStatus gameStatus) {
        String command = reqContent.trim();
        
        LifePlayer player = playerService.getPlayerByUserId(userId);
        if (player == null) {
            return imageGenerationService.generateGameImage("角色不存在！");
        }
        
        // 战斗中不允许强制退出，必须分出胜负或逃跑
        
        // 战斗命令处理
        String result;
        switch (command) {
            case "攻击":
            case "1":
                result = handleAttack(player);
                // 检查是否战斗结束（胜利或败北）
                if (result.contains("输入任意内容返回主菜单")) {
                    finishBattleAndExitMode(player.getId(), userId);
                }
                break;
            case "防御":
            case "2":
                result = handleDefense(player);
                break;
            case "技能":
            case "3":
                result = handleBattleSkill(player);
                break;
            case "道具":
            case "4":
                result = handleBattleItem(player);
                break;
            case "逃跑":
            case "5":
                result = handleBattleEscape(userId, player, gameStatus);
                // 检查是否战斗结束（逃跑成功或败北）
                if (result.contains("输入任意内容返回主菜单") || result.contains("逃跑成功")) {
                    finishBattleAndExitMode(player.getId(), userId);
                }
                break;
            default:
                // 检查是否是使用道具命令
                if (command.startsWith("使用道具")) {
                    result = handleUseBattleItem(command, userId, player);
                } else if ("返回".equals(command)) {
                    // 从道具列表返回战斗菜单
                    LifeBattleState battleState = battleStateMapper.selectByPlayerId(player.getId());
                    if (battleState != null) {
                        result = String.format("第%d回合 请选择行动：\n1.攻击 2.防御 3.技能 4.道具 5.逃跑", 
                            battleState.getCurrentTurn());
                    } else {
                        result = "战斗状态异常！";
                    }
                } else if (command.matches("^[1-9]$") && !"1".equals(command) && !"2".equals(command) && 
                    !"3".equals(command) && !"4".equals(command) && !"5".equals(command)) {
                    // 技能选择命令
                    result = handleSkillSelection(player, Integer.parseInt(command));
                } else if ("0".equals(command)) {
                    result = "『战斗中』\n\n请选择你的行动：\n\n1. 攻击\n2. 防御\n3. 技能\n4. 道具\n5. 逃跑";
                } else {
                    result = "『战斗中！』必须分出胜负或逃跑！\n\n1.攻击 2.防御 3.技能 4.道具 5.逃跑";
                }
                break;
        }
        
        return imageGenerationService.generateGameImageWithStatus(result, player);
    }
    
    private String handleAttack(LifePlayer player) {
        // 获取当前战斗状态
        LifeBattleState battleState = battleStateMapper.selectByPlayerId(player.getId());
        if (battleState == null) {
            return "战斗状态异常，请重新开始！";
        }
        
        // 玩家攻击逻辑
        int playerDamage = Math.max(1, player.getAttackPower() - 8); // 玩家攻击力 - 怪物防御
        int newMonsterHp = Math.max(0, battleState.getMonsterHp() - playerDamage);
        battleState.setMonsterHp(newMonsterHp);
        
        StringBuilder result = new StringBuilder();
        result.append(String.format("第%d回合-你的攻击\n", battleState.getCurrentTurn()));
        result.append("伤害：").append(playerDamage).append(" | ");
        result.append("敌血：").append(newMonsterHp).append("/").append(battleState.getMonsterMaxHp()).append("\n\n");
        
        if (newMonsterHp <= 0) {
            // 战斗胜利
            result.append("『胜利！』击败山贼！\n");
            result.append("经验+50 灵粹+20\n");
            
            // 给予基础奖励
            boolean leveledUp = playerService.gainExperience(player, 50);
            Long currentSpirit = player.getSpirit() != null ? player.getSpirit() : 0L;
            player.setSpirit(currentSpirit + 20);
            
            // 处理怪物掉落
            String dropResult = processMonsterDrops(player, battleState.getMonsterId());
            
            playerService.updatePlayer(player);
            
            if (!dropResult.isEmpty()) {
                result.append(dropResult);
            }
            
            if (leveledUp) {
                result.append("『恭喜！』升级了！\n");
            }
            
            result.append("输入任意内容返回主菜单");
            
            // 清理战斗状态并退出战斗模式
            finishBattle(player.getId());
        } else {
            // 更新技能冷却
            updateSkillCooldowns(player.getId());
            
            // 怪物回合
            result.append("怪物回合\n");
            String monsterAction = performMonsterActionWithCooldown(battleState, player);
            result.append(monsterAction).append("\n\n");
            
            // 更新战斗状态
            battleState.setPlayerHp(player.getHealth());
            battleState.setCurrentTurn(battleState.getCurrentTurn() + 1);
            battleState.setUpdateTime(new Date());
            battleStateMapper.updateByPrimaryKey(battleState);
            
            // 检查玩家是否死亡
            if (player.getHealth() <= 0) {
                result.append("『败北！』\n\n");
                result.append("你被山贼击败了！\n");
                result.append("血量归1，体力-5\n\n");
                result.append("输入任意内容返回主菜单");
                
                // 失败惩罚
                player.setHealth(1);
                player.setStamina(Math.max(0, player.getStamina() - 5));
                playerService.updatePlayer(player);
                
                // 清理战斗状态
                finishBattle(player.getId());
            } else {
                result.append(String.format("『第%d回合开始』\n\n", battleState.getCurrentTurn()));
                result.append("请选择你的下一步行动：\n\n1. 攻击\n2. 防御\n3. 技能\n4. 道具\n5. 逃跑");
            }
        }
        
        return result.toString();
    }
    
    private String handleDefense(LifePlayer player) {
        // TODO: 实现防御逻辑
        return "『防御！』\n\n你进入了防御姿态！\n\n下回合受到伤害减少50%";
    }
    
    private String handleBattleSkill(LifePlayer player) {
        // 显示玩家可用技能列表
        java.util.List<com.bot.life.dao.entity.LifePlayerSkill> playerSkills = playerSkillMapper.selectByPlayerId(player.getId());
        
        if (playerSkills.isEmpty()) {
            return "『技能』\n\n你还没有学会任何技能！\n\n请选择其他行动：\n1. 攻击\n2. 防御\n4. 道具\n5. 逃跑";
        }
        
        StringBuilder skillList = new StringBuilder();
        skillList.append("『选择技能』\n\n");
        
        int index = 1;
        for (com.bot.life.dao.entity.LifePlayerSkill playerSkill : playerSkills) {
            if (playerSkill.getSkill() != null) {
                String cooldownInfo = "";
                if (playerSkill.getCurrentCooldown() > 0) {
                    cooldownInfo = String.format("(冷却中：%d回合)", playerSkill.getCurrentCooldown());
                } else {
                    cooldownInfo = String.format("(冷却：%d回合)", playerSkill.getSkill().getCooldown());
                }
                
                skillList.append(String.format("%d. %s %s\n", 
                    index++, playerSkill.getSkill().getName(), cooldownInfo));
                skillList.append("   威力：").append(playerSkill.getSkill().getPower());
                skillList.append(" | ").append(playerSkill.getSkill().getDescription()).append("\n\n");
            }
        }
        
        skillList.append("0. 返回选择其他行动");
        
        return skillList.toString();
    }
    
    private String handleBattleItem(LifePlayer player) {
        // 获取可在战斗中使用的道具列表
        List<LifePlayerItem> playerItems = inventoryService.getPlayerItems(player);
        
        StringBuilder itemList = new StringBuilder();
        itemList.append("『战斗道具』\n\n");
        
        int index = 1;
        boolean hasUsableItem = false;
        for (LifePlayerItem playerItem : playerItems) {
            LifeItem item = playerItem.getItem();
            if (item != null && item.getCanUseInBattle() == 1) {
                itemList.append(String.format("%d. %s x%d\n", index, item.getName(), playerItem.getQuantity()));
                itemList.append(String.format("   效果：%s\n", item.getDescription()));
                hasUsableItem = true;
                index++;
            }
        }
        
        if (!hasUsableItem) {
            itemList.append("没有可在战斗中使用的道具\n\n");
            itemList.append("发送『返回』继续战斗");
        } else {
            itemList.append("\n发送『使用道具+序号』使用道具\n");
            itemList.append("例如：使用道具 1\n");
            itemList.append("发送『返回』继续战斗");
        }
        
        return itemList.toString();
    }
    
    private String handleUseBattleItem(String command, String userId, LifePlayer player) {
        // 解析命令：使用道具 序号
        String[] parts = command.split("\\s+");
        if (parts.length != 2) {
            return "格式错误！正确格式：使用道具 序号";
        }
        
        try {
            int index = Integer.parseInt(parts[1]);
            
            // 获取可在战斗中使用的道具列表
            List<LifePlayerItem> playerItems = inventoryService.getPlayerItems(player);
            List<LifePlayerItem> usableItems = new java.util.ArrayList<>();
            
            for (LifePlayerItem playerItem : playerItems) {
                LifeItem item = playerItem.getItem();
                if (item != null && item.getCanUseInBattle() == 1) {
                    usableItems.add(playerItem);
                }
            }
            
            if (index < 1 || index > usableItems.size()) {
                return "道具不存在！请重新选择";
            }
            
            LifePlayerItem selectedItem = usableItems.get(index - 1);
            LifeItem item = selectedItem.getItem();
            
            // 只有恢复类道具可以在战斗中使用
            if (item.getType() != ENItemType.RECOVERY.getCode()) {
                return "该道具不能在战斗中使用！";
            }
            
            // 使用道具
            int oldHealth = player.getHealth();
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + item.getEffectValue()));
            int healed = player.getHealth() - oldHealth;
            
            // 扣除道具
            selectedItem.setQuantity(selectedItem.getQuantity() - 1);
            selectedItem.setUpdateTime(new Date());
            playerItemMapper.updateByPrimaryKey(selectedItem);
            playerService.updatePlayer(player);
            
            // 更新战斗状态
            LifeBattleState battleState = battleStateMapper.selectByPlayerId(player.getId());
            if (battleState != null) {
                battleState.setPlayerHp(player.getHealth());
                battleState.setUpdateTime(new Date());
                battleStateMapper.updateByPrimaryKey(battleState);
            }
            
            return String.format("『使用道具』\n\n使用了『%s』\n恢复血量：%d\n当前血量：%d/%d\n\n请选择下一步行动：\n1.攻击 2.防御 3.技能 4.道具 5.逃跑", 
                item.getName(), healed, player.getHealth(), player.getMaxHealth());
                
        } catch (NumberFormatException e) {
            return "序号格式错误！请输入数字";
        }
    }
    
    private String handleBattleEscape(String userId, LifePlayer player, LifeGameStatus gameStatus) {
        // 获取战斗状态
        LifeBattleState battleState = battleStateMapper.selectByPlayerId(player.getId());
        if (battleState == null) {
            return "战斗状态异常，请重新开始！";
        }
        
        // 战斗中的逃跑处理
        int playerSpeed = player.getSpeed();
        int monsterSpeed = 15; // 山贼的速度
        
        boolean escapeSuccess;
        String escapeInfo;
        
        if (playerSpeed >= monsterSpeed) {
            // 速度大于等于怪物，必定成功
            escapeSuccess = true;
            escapeInfo = String.format("『逃跑成功！』\n\n你的速度(%d)大于敌人(%d)，成功逃离！", playerSpeed, monsterSpeed);
        } else {
            // 速度低于怪物，计算概率
            int speedDiff = monsterSpeed - playerSpeed;
            int baseSuccessRate = 50; // 基础成功率50%
            int penaltyRate = (speedDiff / 10) * 2; // 每低10点速度减少2%
            int finalSuccessRate = Math.max(10, baseSuccessRate - penaltyRate); // 最低10%
            
            java.util.Random random = new java.util.Random();
            int roll = random.nextInt(100) + 1;
            escapeSuccess = roll <= finalSuccessRate;
            
            if (escapeSuccess) {
                escapeInfo = String.format("『逃跑成功！』\n\n尽管速度劣势，你还是成功逃脱了！\n成功率：%d%% (实际：%d)", finalSuccessRate, roll);
            } else {
                escapeInfo = String.format("『逃跑失败！』\n\n你试图逃跑但被敌人拦住了！\n成功率：%d%% (实际：%d)\n\n山贼对你发起了攻击！", finalSuccessRate, roll);
                
                // 逃跑失败，怪物攻击
                String monsterAction = performMonsterActionWithCooldown(battleState, player);
                escapeInfo += "\n" + monsterAction;
                
                if (player.getHealth() <= 0) {
                    escapeInfo += "\n\n『败北！』\n\n你被山贼击败了！\n血量归1，体力-5";
                    player.setHealth(1);
                    player.setStamina(Math.max(0, player.getStamina() - 5));
                    playerService.updatePlayer(player);
                    escapeSuccess = true; // 战斗结束
                } else {
                    escapeInfo += "\n\n请选择你的下一步行动：\n\n1. 攻击\n2. 防御\n3. 技能\n4. 道具\n5. 逃跑";
                }
            }
        }
        
        // 消耗体力
        player.setStamina(Math.max(0, player.getStamina() - 1));
        playerService.updatePlayer(player);
        
        if (escapeSuccess) {
            // 退出战斗模式
            gameStatus.setGameMode(ENGameMode.IN_GAME.getCode());
            gameStatus.setUpdateTime(new Date());
            gameStatusMapper.updateByPrimaryKey(gameStatus);
            escapeInfo += "\n\n输入任意内容返回主菜单";
            
            // 清理战斗状态
            finishBattle(player.getId());
        }
        
        return escapeInfo;
    }
    
    /**
     * 执行怪物行动（普通攻击或技能）
     */
    private String performMonsterAction(Long monsterId, LifePlayer player) {
        java.util.List<LifeMonsterSkill> monsterSkills = monsterSkillMapper.selectByMonsterId(monsterId);
        
        StringBuilder actionResult = new StringBuilder();
        
        if (!monsterSkills.isEmpty() && new java.util.Random().nextBoolean()) {
            // 50% 概率使用技能
            LifeMonsterSkill randomSkill = monsterSkills.get(new java.util.Random().nextInt(monsterSkills.size()));
            if (randomSkill.getSkill() != null) {
                actionResult.append("山贼使用了技能『").append(randomSkill.getSkill().getName()).append("』！\n");
                
                // 技能伤害计算（基础攻击力 + 技能威力）
                int skillDamage = Math.max(1, (25 + randomSkill.getSkill().getPower()) - player.getDefense());
                int newPlayerHp = Math.max(0, player.getHealth() - skillDamage);
                player.setHealth(newPlayerHp);
                
                actionResult.append("技能效果：").append(randomSkill.getSkill().getDescription()).append("\n");
                actionResult.append("你受到技能伤害：").append(skillDamage).append("\n");
                actionResult.append("你的血量：").append(newPlayerHp).append("/").append(player.getMaxHealth());
                
                playerService.updatePlayer(player);
                return actionResult.toString();
            }
        }
        
        // 普通攻击
        actionResult.append("山贼对你发起了普通攻击！\n");
        int normalDamage = Math.max(1, 25 - player.getDefense());
        int newPlayerHp = Math.max(0, player.getHealth() - normalDamage);
        player.setHealth(newPlayerHp);
        
        actionResult.append("你受到伤害：").append(normalDamage).append("\n");
        actionResult.append("你的血量：").append(newPlayerHp).append("/").append(player.getMaxHealth());
        
        playerService.updatePlayer(player);
        return actionResult.toString();
    }
    
    /**
     * 重置玩家技能冷却（脱离战斗时调用）
     */
    private void resetPlayerSkillCooldowns(Long playerId) {
        java.util.List<com.bot.life.dao.entity.LifePlayerSkill> playerSkills = playerSkillMapper.selectByPlayerId(playerId);
        for (com.bot.life.dao.entity.LifePlayerSkill playerSkill : playerSkills) {
            playerSkill.setCurrentCooldown(0);
            playerSkillMapper.updateByPrimaryKey(playerSkill);
        }
    }
    
    /**
     * 更新技能冷却时间（每回合-1）
     */
    private void updateSkillCooldowns(Long playerId) {
        java.util.List<com.bot.life.dao.entity.LifePlayerSkill> playerSkills = playerSkillMapper.selectByPlayerId(playerId);
        for (com.bot.life.dao.entity.LifePlayerSkill playerSkill : playerSkills) {
            if (playerSkill.getCurrentCooldown() > 0) {
                playerSkill.setCurrentCooldown(playerSkill.getCurrentCooldown() - 1);
                playerSkillMapper.updateByPrimaryKey(playerSkill);
            }
        }
    }
    
    /**
     * 使用技能并设置冷却时间
     */
    private boolean usePlayerSkill(Long playerId, Long skillId) {
        com.bot.life.dao.entity.LifePlayerSkill playerSkill = playerSkillMapper.selectByPlayerAndSkillId(playerId, skillId);
        if (playerSkill == null || playerSkill.getCurrentCooldown() > 0) {
            return false; // 技能不存在或在冷却中
        }
        
        // 设置冷却时间
        if (playerSkill.getSkill() != null) {
            playerSkill.setCurrentCooldown(playerSkill.getSkill().getCooldown());
            playerSkill.setLastUsedTime(new Date());
            playerSkillMapper.updateByPrimaryKey(playerSkill);
            return true;
        }
        return false;
    }
    
    /**
     * 怪物带冷却的行动逻辑
     */
    private String performMonsterActionWithCooldown(LifeBattleState battleState, LifePlayer player) {
        if (battleState == null || player == null) {
            return "战斗状态异常！";
        }
        
        java.util.List<LifeMonsterSkill> monsterSkills = monsterSkillMapper.selectByMonsterId(battleState.getMonsterId());
        
        StringBuilder actionResult = new StringBuilder();
        
        // 解析怪物技能冷却状态
        java.util.Map<String, Integer> cooldowns = parseMonsterSkillCooldowns(battleState.getMonsterSkillCooldowns());
        
        // 查找可用技能（不在冷却中的）
        java.util.List<LifeMonsterSkill> availableSkills = new java.util.ArrayList<>();
        for (LifeMonsterSkill skill : monsterSkills) {
            String skillKey = skill.getSkillId().toString();
            if (!cooldowns.containsKey(skillKey) || cooldowns.get(skillKey) <= 0) {
                availableSkills.add(skill);
            }
        }
        
        if (!availableSkills.isEmpty() && new java.util.Random().nextBoolean()) {
            // 50% 概率使用技能（如果有可用技能）
            LifeMonsterSkill selectedSkill = availableSkills.get(new java.util.Random().nextInt(availableSkills.size()));
            if (selectedSkill.getSkill() != null) {
                actionResult.append("敌人『").append(selectedSkill.getSkill().getName()).append("』");
                
                // 技能伤害计算（基础攻击力 + 技能威力）
                int skillDamage = Math.max(1, (25 + selectedSkill.getSkill().getPower()) - player.getDefense());
                int newPlayerHp = Math.max(0, player.getHealth() - skillDamage);
                player.setHealth(newPlayerHp);
                
                actionResult.append(" 伤害：").append(skillDamage).append(" | ");
                actionResult.append("你血：").append(newPlayerHp).append("/").append(player.getMaxHealth());
                
                // 设置技能冷却
                cooldowns.put(selectedSkill.getSkillId().toString(), selectedSkill.getSkill().getCooldown());
                
                playerService.updatePlayer(player);
            }
        } else {
            // 普通攻击
            actionResult.append("敌人普攻");
            int normalDamage = Math.max(1, 25 - player.getDefense());
            int newPlayerHp = Math.max(0, player.getHealth() - normalDamage);
            player.setHealth(newPlayerHp);
            
            actionResult.append(" 伤害：").append(normalDamage).append(" | ");
            actionResult.append("你血：").append(newPlayerHp).append("/").append(player.getMaxHealth());
            
            playerService.updatePlayer(player);
        }
        
        // 更新怪物技能冷却（每回合-1）
        updateMonsterSkillCooldowns(cooldowns);
        battleState.setMonsterSkillCooldowns(serializeMonsterSkillCooldowns(cooldowns));
        
        return actionResult.toString();
    }
    
    /**
     * 解析怪物技能冷却JSON
     */
    private java.util.Map<String, Integer> parseMonsterSkillCooldowns(String cooldownsJson) {
        java.util.Map<String, Integer> cooldowns = new java.util.HashMap<>();
        if (cooldownsJson != null && !cooldownsJson.trim().isEmpty() && !cooldownsJson.equals("{}")) {
            try {
                // 简单的JSON解析（实际项目中建议使用Jackson等库）
                String json = cooldownsJson.replace("{", "").replace("}", "").replace("\"", "");
                if (!json.trim().isEmpty()) {
                    String[] pairs = json.split(",");
                    for (String pair : pairs) {
                        String[] kv = pair.split(":");
                        if (kv.length == 2) {
                            cooldowns.put(kv[0].trim(), Integer.parseInt(kv[1].trim()));
                        }
                    }
                }
            } catch (Exception e) {
                // 解析失败，返回空Map
            }
        }
        return cooldowns;
    }
    
    /**
     * 序列化怪物技能冷却为JSON
     */
    private String serializeMonsterSkillCooldowns(java.util.Map<String, Integer> cooldowns) {
        if (cooldowns.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (java.util.Map.Entry<String, Integer> entry : cooldowns.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    /**
     * 更新怪物技能冷却（每回合-1）
     */
    private void updateMonsterSkillCooldowns(java.util.Map<String, Integer> cooldowns) {
        for (String skillId : cooldowns.keySet()) {
            int currentCooldown = cooldowns.get(skillId);
            if (currentCooldown > 0) {
                cooldowns.put(skillId, currentCooldown - 1);
            }
        }
    }
    
    /**
     * 处理怪物掉落
     */
    private String processMonsterDrops(LifePlayer player, Long monsterId) {
        List<LifeMonsterDrop> dropConfigs = monsterDropMapper.selectByMonsterId(monsterId);
        if (dropConfigs == null || dropConfigs.isEmpty()) {
            return "";
        }
        
        StringBuilder dropInfo = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (LifeMonsterDrop dropConfig : dropConfigs) {
            // 根据掉落概率判断是否掉落
            if (random.nextDouble() > dropConfig.getDropRate().doubleValue()) {
                continue;
            }
            
            if (dropConfig.getDropType() == 1) {
                // 道具掉落
                if (dropConfig.getItem() != null) {
                    int quantity = dropConfig.getMinQuantity();
                    if (dropConfig.getMaxQuantity() > dropConfig.getMinQuantity()) {
                        quantity = random.nextInt(dropConfig.getMaxQuantity() - dropConfig.getMinQuantity() + 1) 
                                  + dropConfig.getMinQuantity();
                    }
                    
                    // 添加道具到背包
                    inventoryService.addItem(player.getId(), dropConfig.getItemId(), quantity);
                    dropInfo.append(String.format("获得道具：『%s』x%d\n", 
                        dropConfig.getItem().getName(), quantity));
                }
            } else if (dropConfig.getDropType() == 2) {
                // 灵粹掉落
                int spiritDrop = dropConfig.getSpiritAmount();
                if (spiritDrop > 0) {
                    Long currentSpirit = player.getSpirit() != null ? player.getSpirit() : 0L;
                    player.setSpirit(currentSpirit + spiritDrop);
                    dropInfo.append(String.format("额外灵粹：+%d\n", spiritDrop));
                }
            }
        }
        
        return dropInfo.toString();
    }
    
    /**
     * 结束战斗，清理状态并重置冷却
     */
    private void finishBattle(Long playerId) {
        // 删除战斗状态
        battleStateMapper.deleteByPlayerId(playerId);
        
        // 重置玩家技能冷却
        resetPlayerSkillCooldowns(playerId);
    }
    
    private void finishBattleAndExitMode(Long playerId, String userId) {
        // 删除战斗状态
        battleStateMapper.deleteByPlayerId(playerId);
        
        // 重置玩家技能冷却
        resetPlayerSkillCooldowns(playerId);
        
        // 退出战斗模式
        LifeGameStatus gameStatus = getOrCreateGameStatus(userId);
        gameStatus.setGameMode(ENGameMode.IN_GAME.getCode());
        gameStatus.setUpdateTime(new Date());
        gameStatusMapper.updateByPrimaryKey(gameStatus);
    }
    
    /**
     * 处理技能选择
     */
    private String handleSkillSelection(LifePlayer player, int skillIndex) {
        java.util.List<com.bot.life.dao.entity.LifePlayerSkill> playerSkills = playerSkillMapper.selectByPlayerId(player.getId());
        
        if (skillIndex < 1 || skillIndex > playerSkills.size()) {
            return "『技能选择』\n\n无效的技能选择！\n\n请重新选择：\n1. 攻击\n2. 防御\n3. 技能\n4. 道具\n5. 逃跑";
        }
        
        com.bot.life.dao.entity.LifePlayerSkill selectedSkill = playerSkills.get(skillIndex - 1);
        
        // 检查技能是否在冷却中
        if (selectedSkill.getCurrentCooldown() > 0) {
            return String.format("『技能冷却』\n\n『%s』还有%d回合冷却时间！\n\n请选择其他行动：\n1. 攻击\n2. 防御\n3. 选择其他技能\n4. 道具\n5. 逃跑", 
                selectedSkill.getSkill().getName(), selectedSkill.getCurrentCooldown());
        }
        
        // 使用技能
        if (!usePlayerSkill(player.getId(), selectedSkill.getSkillId())) {
            return "『技能使用失败』\n\n技能使用失败！\n\n请重新选择行动";
        }
        
        // 获取战斗状态
        LifeBattleState battleState = battleStateMapper.selectByPlayerId(player.getId());
        if (battleState == null) {
            return "战斗状态异常，请重新开始！";
        }
        
        // 技能攻击逻辑
        int skillDamage = Math.max(1, (player.getAttackPower() + selectedSkill.getSkill().getPower()) - 8); // 玩家攻击力 + 技能威力 - 怪物防御
        int newMonsterHp = Math.max(0, battleState.getMonsterHp() - skillDamage);
        battleState.setMonsterHp(newMonsterHp);
        
        StringBuilder result = new StringBuilder();
        result.append(String.format("『第%d回合 - 你的回合』\n\n", battleState.getCurrentTurn()));
        result.append("你使用了技能『").append(selectedSkill.getSkill().getName()).append("』！\n");
        result.append("技能效果：").append(selectedSkill.getSkill().getDescription()).append("\n");
        result.append("造成伤害：").append(skillDamage).append("\n");
        result.append("山贼血量：").append(newMonsterHp).append("/").append(battleState.getMonsterMaxHp()).append("\n\n");
        
        if (newMonsterHp <= 0) {
            // 战斗胜利
            result.append("『胜利！』击败山贼！\n");
            result.append("经验+50 灵粹+20\n");
            
            // 给予奖励
            boolean leveledUp = playerService.gainExperience(player, 50);
            Long currentSpirit = player.getSpirit() != null ? player.getSpirit() : 0L;
            player.setSpirit(currentSpirit + 20);
            playerService.updatePlayer(player);
            
            if (leveledUp) {
                result.append("『恭喜！』升级了！\n");
            }
            
            result.append("输入任意内容返回主菜单");
            
            // 清理战斗状态
            finishBattle(player.getId());
        } else {
            // 更新技能冷却
            updateSkillCooldowns(player.getId());
            
            // 怪物回合
            result.append("怪物回合\n");
            String monsterAction = performMonsterActionWithCooldown(battleState, player);
            result.append(monsterAction).append("\n\n");
            
            // 更新战斗状态
            battleState.setPlayerHp(player.getHealth());
            battleState.setCurrentTurn(battleState.getCurrentTurn() + 1);
            battleState.setUpdateTime(new Date());
            battleStateMapper.updateByPrimaryKey(battleState);
            
            // 检查玩家是否死亡
            if (player.getHealth() <= 0) {
                result.append("『败北！』\n\n");
                result.append("你被山贼击败了！\n");
                result.append("血量归1，体力-5\n\n");
                result.append("输入任意内容返回主菜单");
                
                // 失败惩罚
                player.setHealth(1);
                player.setStamina(Math.max(0, player.getStamina() - 5));
                playerService.updatePlayer(player);
                
                // 清理战斗状态
                finishBattle(player.getId());
            } else {
                result.append(String.format("『第%d回合开始』\n\n", battleState.getCurrentTurn()));
                result.append("请选择你的下一步行动：\n\n1. 攻击\n2. 防御\n3. 技能\n4. 道具\n5. 逃跑");
            }
        }
        
        return result.toString();
    }
}
