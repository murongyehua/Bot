package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.service.game.BaseGamePlay;
import com.bot.common.constant.GameRoomConsts;
import com.bot.common.enums.ENGameInfo;
import com.bot.common.enums.ENGameRoomStatus;
import com.bot.common.util.GameRoomDateUtil;
import com.bot.common.util.SendMsgUtil;
import com.bot.game.dao.entity.*;
import com.bot.game.dao.mapper.BotGameRoomMapper;
import com.bot.game.dao.mapper.BotGameRoomPlayerMapper;
import com.bot.game.dao.mapper.BotGameUserScoreMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 游戏房间管理服务
 * @author Assistant
 */
@Slf4j
@Service
public class GameRoomManager {

    @Autowired
    private BotGameRoomMapper roomMapper;

    @Autowired
    private BotGameRoomPlayerMapper roomPlayerMapper;

    @Autowired
    private BotGameUserScoreMapper userScoreMapper;

    /**
     * 游戏实例管理 - key: roomCode, value: 游戏实例
     */
    private static final Map<String, BaseGamePlay> GAME_INSTANCES = new ConcurrentHashMap<>();

    /**
     * 用户所在房间映射 - key: userId, value: roomCode
     */
    private static final Map<String, String> USER_IN_GAME_ROOM = new ConcurrentHashMap<>();

    /**
     * 用户查看游戏列表上下文 - key: userId, value: 查询时间戳
     */
    private static final Map<String, Long> USER_GAME_LIST_CONTEXT = new ConcurrentHashMap<>();

    /**
     * 用户参与方式 - key: userId, value: groupId(空则为私聊)
     */
    private static final Map<String, String> USER_PARTICIPATION_GROUP = new ConcurrentHashMap<>();

    /**
     * 处理游戏房间相关指令
     */
    public String handleGameCommand(String instruction, String userId, String groupId) {
        instruction = instruction.trim();

        // 1. 小林游戏大厅
        if (instruction.equals(GameRoomConsts.Command.GAME_HALL)) {
            return listPublicRooms();
        }

        // 2. 小林游戏
        if (instruction.equals(GameRoomConsts.Command.GAME_LIST)) {
            return listAllGames(userId);
        }

        // 3. 查看游戏详情（纯数字）
        if (instruction.matches("\\d+")) {
            return getGameDetail(userId, instruction);
        }

        // 4. 创建房间
        if (instruction.startsWith(GameRoomConsts.Command.CREATE_ROOM)) {
            return createRoom(userId, instruction, groupId);
        }

        // 5. 加入房间
        if (instruction.startsWith(GameRoomConsts.Command.JOIN_ROOM)) {
            return joinRoom(userId, instruction, groupId);
        }

        // 6. 离开房间
        if (instruction.startsWith(GameRoomConsts.Command.LEAVE_ROOM)) {
            return leaveRoom(userId, instruction);
        }

        // 7. 开始游戏
        if (instruction.startsWith(GameRoomConsts.Command.START_GAME)) {
            return startGameInRoom(userId, instruction);
        }

        // 8. 切换游戏
        if (instruction.startsWith(GameRoomConsts.Command.SWITCH_GAME)) {
            return switchGame(userId, instruction);
        }

        return null; // 不是游戏房间指令
    }

    /**
     * 处理游戏中的玩家指令
     */
    public String handleGameInstruction(String userId, String instruction) {
        String roomCode = USER_IN_GAME_ROOM.get(userId);
        if (roomCode == null) {
            return null;
        }

        BaseGamePlay gamePlay = GAME_INSTANCES.get(roomCode);
        if (gamePlay == null) {
            USER_IN_GAME_ROOM.remove(userId);
            return null;
        }

        String result = gamePlay.handleInstruction(userId, instruction);
        
        // 检查是否是退出游戏指令
        if (result != null && result.startsWith("QUIT_GAME:")) {
            // 处理退出游戏
            handlePlayerQuitGame(roomCode);
            return null; // 消息已在游戏内部发送
        }
        
        return result;
    }

    /**
     * 检查用户是否在游戏中
     */
    public boolean isUserInGame(String userId) {
        return USER_IN_GAME_ROOM.containsKey(userId);
    }

    /**
     * 1. 查看游戏大厅
     */
    private String listPublicRooms() {
        List<BotGameRoom> rooms = roomMapper.selectPublicWaitingRooms();
        
        if (CollectionUtil.isEmpty(rooms)) {
            return GameRoomConsts.Tips.HALL_EMPTY;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("─────────────\n");
        sb.append("🏟️ 小林游戏大厅\n");
        sb.append("─────────────\n\n");
        for (BotGameRoom room : rooms) {
            int currentCount = roomPlayerMapper.countByRoomId(room.getId());
            sb.append(String.format("• 房间[%s] - %s  人数:%d/%d\n",
                    room.getRoomCode(),
                    room.getGameName(),
                    currentCount,
                    room.getMaxPeople()));
        }
        sb.append("\n加入方式：发送【加入房间 房间号】\n");
        sb.append("创建方式：发送【创建房间 游戏名 [口令]】");
        
        return sb.toString();
    }

    /**
     * 2. 查看游戏列表
     */
    private String listAllGames(String userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("─────────────\n");
        sb.append("🎮 游戏列表\n");
        sb.append("─────────────\n\n");
        
        ENGameInfo[] games = ENGameInfo.values();
        for (int i = 0; i < games.length; i++) {
            ENGameInfo game = games[i];
            sb.append(String.format("%d. %s  人数:%d~%d\n",
                    game.getNo(),
                    game.getName(),
                    game.getMinPeople(),
                    game.getMaxPeople()));
        }
        
        sb.append("\n查看详情：在本界面后发送序号，例如【1】\n");
        sb.append("创建房间：发送【创建房间 游戏名 [口令]】\n");
        
        // 记录用户查询游戏列表的时间
        USER_GAME_LIST_CONTEXT.put(userId, System.currentTimeMillis());
        
        return sb.toString();
    }

    /**
     * 3. 查看游戏详情
     */
    private String getGameDetail(String userId, String gameNoStr) {
        // 检查上下文是否存在且未过期
        Long queryTime = USER_GAME_LIST_CONTEXT.get(userId);
        if (queryTime == null) {
            return null; // 不是查看游戏详情的指令
        }
        
        if (System.currentTimeMillis() - queryTime > GameRoomConsts.GAME_LIST_CONTEXT_TIMEOUT) {
            USER_GAME_LIST_CONTEXT.remove(userId);
            return GameRoomConsts.Tips.GAME_LIST_EXPIRED;
        }

        try {
            int gameNo = Integer.parseInt(gameNoStr);
            ENGameInfo gameInfo = ENGameInfo.getByNo(gameNo);
            
            if (gameInfo == null) {
                return GameRoomConsts.Tips.INVALID_GAME_NO;
            }
            
            // 清除上下文
            USER_GAME_LIST_CONTEXT.remove(userId);
            
            return String.format("=== %s ===\n\n%s",
                    gameInfo.getName(),
                    gameInfo.getDesc());
                    
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 4. 创建房间
     * 指令格式：创建房间 游戏名 [口令]
     */
    @Transactional(rollbackFor = Exception.class)
    public String createRoom(String userId, String instruction, String groupId) {
        // 检查是否为群聊
        if (groupId == null || groupId.trim().isEmpty()) {
            return "小林游戏房间仅支持群聊玩法，私聊暂不支持哦~";
        }
        
        String[] parts = instruction.split("\\s+");
        if (parts.length < 2) {
            return "指令格式错误，正确格式：创建房间 游戏名 [口令]";
        }

        String gameName = parts[1];
        String password = parts.length > 2 ? parts[2] : null;

        // 校验密码长度
        if (password != null && password.length() > GameRoomConsts.PASSWORD_MAX_LENGTH) {
            return GameRoomConsts.Tips.PASSWORD_TOO_LONG;
        }

        // 查找游戏信息
        ENGameInfo gameInfo = ENGameInfo.getByName(gameName);
        if (gameInfo == null) {
            return GameRoomConsts.Tips.GAME_NOT_FOUND;
        }

        // 检查用户是否已在其他房间
        BotGameRoomPlayer existingRoom = roomPlayerMapper.selectByUserId(userId);
        if (existingRoom != null) {
            BotGameRoom room = roomMapper.selectByPrimaryKey(existingRoom.getRoomId());
            return String.format(GameRoomConsts.Tips.ALREADY_IN_ROOM, room.getRoomCode());
        }

        // 生成房间号
        String roomCode = generateRoomCode();

        // 创建房间
        BotGameRoom room = new BotGameRoom();
        room.setRoomCode(roomCode);
        room.setRoomPassword(password);
        room.setGameCode(gameInfo.getCode());
        room.setGameName(gameInfo.getName());
        room.setMaxPeople(gameInfo.getMaxPeople());
        room.setCreateTime(GameRoomDateUtil.now());
        room.setLastTime(GameRoomDateUtil.now());
        room.setStatus(ENGameRoomStatus.WAITING.getCode());
        room.setMasterId(userId);
        roomMapper.insertSelective(room);

        // 房主自动加入房间
        BotGameRoom createdRoom = roomMapper.selectByRoomCode(roomCode);
        BotGameRoomPlayer player = new BotGameRoomPlayer();
        player.setRoomId(createdRoom.getId());
        player.setUserId(userId);
        player.setCreateDate(GameRoomDateUtil.nowTimestamp());
        roomPlayerMapper.insertSelective(player);

        // 记录参与方式(ConcurrentHashMap不允许null值,私聊时使用空字符串)
        USER_PARTICIPATION_GROUP.put(userId, groupId == null ? "" : groupId);

        String roomType = StrUtil.isBlank(password) ? "公开房间" : "私密房间";
        return String.format(GameRoomConsts.Tips.CREATE_ROOM_SUCCESS,
                roomCode,
                gameInfo.getName(),
                gameInfo.getMinPeople(),
                gameInfo.getMaxPeople(),
                roomType);
    }

    /**
     * 5. 加入房间
     * 指令格式：加入房间 房间号 [密码]
     */
    @Transactional(rollbackFor = Exception.class)
    public String joinRoom(String userId, String instruction, String groupId) {
        // 检查是否为群聊
        if (groupId == null || groupId.trim().isEmpty()) {
            return "小林游戏房间仅支持群聊玩法，私聊暂不支持哦~";
        }
        
        String[] parts = instruction.split("\\s+");
        if (parts.length < 2) {
            return "指令格式错误，正确格式：加入房间 房间号 [密码]";
        }

        String roomCode = parts[1];
        String password = parts.length > 2 ? parts[2] : null;

        // 1. 检查用户是否已在其他房间
        BotGameRoomPlayer existingRoom = roomPlayerMapper.selectByUserId(userId);
        if (existingRoom != null) {
            BotGameRoom room = roomMapper.selectByPrimaryKey(existingRoom.getRoomId());
            return String.format(GameRoomConsts.Tips.ALREADY_IN_ROOM, room.getRoomCode());
        }

        // 2. 校验目标房间存在性
        BotGameRoom targetRoom = roomMapper.selectByRoomCode(roomCode);
        if (targetRoom == null) {
            return GameRoomConsts.Tips.ROOM_NOT_FOUND;
        }

        // 3. 校验房间状态
        if (ENGameRoomStatus.PLAYING.getCode().equals(targetRoom.getStatus())) {
            return GameRoomConsts.Tips.GAME_STARTED;
        }

        // 4. 校验密码（私密房间）
        if (StrUtil.isNotBlank(targetRoom.getRoomPassword())) {
            if (!targetRoom.getRoomPassword().equals(password)) {
                return GameRoomConsts.Tips.WRONG_PASSWORD;
            }
        }

        // 5. 校验人数上限
        int currentCount = roomPlayerMapper.countByRoomId(targetRoom.getId());
        if (currentCount >= targetRoom.getMaxPeople()) {
            return String.format(GameRoomConsts.Tips.ROOM_FULL,
                    currentCount, targetRoom.getMaxPeople());
        }

        // 6. 执行加入逻辑
        BotGameRoomPlayer player = new BotGameRoomPlayer();
        player.setRoomId(targetRoom.getId());
        player.setUserId(userId);
        player.setCreateDate(GameRoomDateUtil.nowTimestamp());
        roomPlayerMapper.insertSelective(player);

        // 记录参与方式(ConcurrentHashMap不允许null值,私聊时使用空字符串)
        USER_PARTICIPATION_GROUP.put(userId, groupId == null ? "" : groupId);

        // 7. 更新房间最后活跃时间
        targetRoom.setLastTime(GameRoomDateUtil.now());
        roomMapper.updateByPrimaryKeySelective(targetRoom);

        return String.format(GameRoomConsts.Tips.JOIN_SUCCESS,
                roomCode, currentCount + 1, targetRoom.getMaxPeople());
    }

    /**
     * 6. 离开房间
     * 指令格式：离开房间 房间号
     */
    @Transactional(rollbackFor = Exception.class)
    public String leaveRoom(String userId, String instruction) {
        String[] parts = instruction.split("\\s+");
        if (parts.length < 2) {
            return "指令格式错误，正确格式：离开房间 房间号";
        }

        String roomCode = parts[1];

        // 查找房间
        BotGameRoom room = roomMapper.selectByRoomCode(roomCode);
        if (room == null) {
            return GameRoomConsts.Tips.ROOM_NOT_FOUND;
        }

        // 检查用户是否在该房间
        BotGameRoomPlayer playerInRoom = roomPlayerMapper.selectByUserId(userId);
        if (playerInRoom == null || !playerInRoom.getRoomId().equals(room.getId())) {
            return GameRoomConsts.Tips.NOT_IN_ROOM;
        }

        // 移除玩家
        roomPlayerMapper.deleteByRoomIdAndUserId(room.getId(), userId);

        // 检查房间是否还有人
        int remainingCount = roomPlayerMapper.countByRoomId(room.getId());
        if (remainingCount == 0) {
            // 房间无人，删除房间
            roomMapper.deleteByPrimaryKey(room.getId());
            return String.format(GameRoomConsts.Tips.LEAVE_SUCCESS + "，房间已解散", roomCode);
        } else {
            // 如果离开的是房主，转移房主
            if (userId.equals(room.getMasterId())) {
                List<BotGameRoomPlayer> players = roomPlayerMapper.selectByRoomId(room.getId());
                if (CollectionUtil.isNotEmpty(players)) {
                    room.setMasterId(players.get(0).getUserId());
                    roomMapper.updateByPrimaryKeySelective(room);
                }
            }
            return String.format(GameRoomConsts.Tips.LEAVE_SUCCESS, roomCode);
        }
    }

    /**
     * 7. 开始游戏
     * 指令格式：开始游戏 房间号
     */
    @Transactional(rollbackFor = Exception.class)
    public String startGameInRoom(String userId, String instruction) {
        String[] parts = instruction.split("\\s+");
        if (parts.length < 2) {
            return "指令格式错误，正确格式：开始游戏 房间号";
        }

        String roomCode = parts[1];

        // 查找房间
        BotGameRoom room = roomMapper.selectByRoomCode(roomCode);
        if (room == null) {
            return GameRoomConsts.Tips.ROOM_NOT_FOUND;
        }

        // 检查是否为房主
        if (!userId.equals(room.getMasterId())) {
            return GameRoomConsts.Tips.NOT_ROOM_MASTER;
        }

        // 检查房间状态
        if (!ENGameRoomStatus.WAITING.getCode().equals(room.getStatus())) {
            return GameRoomConsts.Tips.ROOM_NOT_WAITING;
        }

        // 获取游戏信息
        ENGameInfo gameInfo = ENGameInfo.getByCode(room.getGameCode());
        if (gameInfo == null) {
            return GameRoomConsts.Tips.GAME_NOT_FOUND;
        }

        // 检查人数
        int currentCount = roomPlayerMapper.countByRoomId(room.getId());
        if (currentCount < gameInfo.getMinPeople()) {
            return String.format(GameRoomConsts.Tips.PLAYER_NOT_ENOUGH, gameInfo.getMinPeople());
        }

        // 更新房间状态为游戏中
        room.setStatus(ENGameRoomStatus.PLAYING.getCode());
        room.setLastTime(GameRoomDateUtil.now());
        roomMapper.updateByPrimaryKeySelective(room);

        // 获取所有玩家
        List<BotGameRoomPlayer> players = roomPlayerMapper.selectByRoomId(room.getId());
        List<String> playerIds = players.stream()
                .map(BotGameRoomPlayer::getUserId)
                .collect(Collectors.toList());

        // 创建游戏实例
        try {
            BaseGamePlay gamePlay = createGameInstance(gameInfo, roomCode, playerIds);
            
            // 设置玩家参与方式
            Map<String, String> participationMap = new HashMap<>();
            for (String playerId : playerIds) {
                String groupId = USER_PARTICIPATION_GROUP.get(playerId);
                participationMap.put(playerId, groupId);
            }
            gamePlay.setParticipationMap(participationMap);
            
            GAME_INSTANCES.put(roomCode, gamePlay);
            
            // 记录玩家所在房间
            for (String playerId : playerIds) {
                USER_IN_GAME_ROOM.put(playerId, roomCode);
            }

            // 启动游戏（游戏开始消息已在游戏内部广播给所有群）
            gamePlay.startGame();
            return GameRoomConsts.Tips.START_GAME_SUCCESS;

        } catch (Exception e) {
            log.error("创建游戏实例失败", e);
            // 回滚房间状态
            room.setStatus(ENGameRoomStatus.WAITING.getCode());
            roomMapper.updateByPrimaryKeySelective(room);
            return "游戏启动失败，请稍后重试~";
        }
    }

    /**
     * 8. 切换游戏
     * 指令格式：切换游戏 游戏名
     */
    @Transactional(rollbackFor = Exception.class)
    public String switchGame(String userId, String instruction) {
        String[] parts = instruction.split("\\s+");
        if (parts.length < 2) {
            return "指令格式错误，正确格式：切换游戏 游戏名";
        }

        String gameName = parts[1];

        // 查找用户所在房间
        BotGameRoomPlayer playerInRoom = roomPlayerMapper.selectByUserId(userId);
        if (playerInRoom == null) {
            return GameRoomConsts.Tips.NOT_IN_ROOM;
        }

        BotGameRoom room = roomMapper.selectByPrimaryKey(playerInRoom.getRoomId());
        if (room == null) {
            return GameRoomConsts.Tips.ROOM_NOT_FOUND;
        }

        // 检查是否为房主
        if (!userId.equals(room.getMasterId())) {
            return GameRoomConsts.Tips.NOT_ROOM_MASTER;
        }

        // 检查房间状态
        if (!ENGameRoomStatus.WAITING.getCode().equals(room.getStatus())) {
            return GameRoomConsts.Tips.ROOM_NOT_WAITING;
        }

        // 查找目标游戏
        ENGameInfo targetGame = ENGameInfo.getByName(gameName);
        if (targetGame == null) {
            return GameRoomConsts.Tips.GAME_NOT_FOUND;
        }

        // 检查当前人数是否符合
        int currentCount = roomPlayerMapper.countByRoomId(room.getId());
        if (currentCount < targetGame.getMinPeople() || currentCount > targetGame.getMaxPeople()) {
            return String.format(GameRoomConsts.Tips.PLAYER_NOT_FIT,
                    currentCount, targetGame.getMinPeople(), targetGame.getMaxPeople());
        }

        // 更新房间游戏信息
        room.setGameCode(targetGame.getCode());
        room.setGameName(targetGame.getName());
        room.setMaxPeople(targetGame.getMaxPeople());
        room.setLastTime(GameRoomDateUtil.now());
        roomMapper.updateByPrimaryKeySelective(room);

        return String.format(GameRoomConsts.Tips.SWITCH_GAME_SUCCESS, targetGame.getName());
    }

    /**
     * 结算游戏积分
     */
    @Transactional(rollbackFor = Exception.class)
    public void settleGameScore(String roomCode) {
        BaseGamePlay gamePlay = GAME_INSTANCES.get(roomCode);
        if (gamePlay == null) {
            return;
        }

        try {
            // 计算积分
            Map<String, Integer> scores = gamePlay.calculateScores();
            if (scores == null || scores.isEmpty()) {
                return;
            }

            // 更新每个玩家的积分
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                String userId = entry.getKey();
                Integer score = entry.getValue();

                BotGameUserScoreExample example = new BotGameUserScoreExample();
                example.createCriteria().andUserIdEqualTo(userId);
                List<BotGameUserScore> existingScores = userScoreMapper.selectByExample(example);

                if (CollectionUtil.isEmpty(existingScores)) {
                    // 不存在，插入新记录
                    BotGameUserScore userScore = new BotGameUserScore();
                    userScore.setUserId(userId);
                    userScore.setScore(score);
                    userScoreMapper.insertSelective(userScore);
                } else {
                    // 存在，更新积分
                    BotGameUserScore userScore = existingScores.get(0);
                    userScore.setScore(userScore.getScore() + score);
                    userScoreMapper.updateByPrimaryKeySelective(userScore);
                }
            }

            log.info("房间[{}]游戏积分结算完成", roomCode);

        } catch (Exception e) {
            log.error("房间[{}]积分结算失败", roomCode, e);
        }
    }

    /**
     * 游戏结束，清理资源
     */
    @Transactional(rollbackFor = Exception.class)
    public void finishGame(String roomCode) {
        try {
            // 1. 结算积分
            settleGameScore(roomCode);

            // 2. 结束游戏实例
            BaseGamePlay gamePlay = GAME_INSTANCES.remove(roomCode);
            if (gamePlay != null) {
                gamePlay.endGame();
                
                // 3. 清除玩家映射
                for (String playerId : gamePlay.getPlayerIds()) {
                    USER_IN_GAME_ROOM.remove(playerId);
                }
            }

            // 4. 更新房间状态为等待中
            BotGameRoom room = roomMapper.selectByRoomCode(roomCode);
            if (room != null) {
                room.setStatus(ENGameRoomStatus.WAITING.getCode());
                room.setLastTime(GameRoomDateUtil.now());
                roomMapper.updateByPrimaryKeySelective(room);
            }

            log.info("房间[{}]游戏结束清理完成", roomCode);

        } catch (Exception e) {
            log.error("房间[{}]游戏结束清理失败", roomCode, e);
        }
    }

    /**
     * 生成房间编号
     */
    private String generateRoomCode() {
        String code;
        int attempts = 0;
        do {
            code = String.valueOf(RandomUtil.randomInt(
                    GameRoomConsts.ROOM_CODE_MIN,
                    GameRoomConsts.ROOM_CODE_MAX));
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("生成房间编号失败，请稍后重试");
            }
        } while (roomMapper.existsByRoomCode(code));
        return code;
    }

    /**
     * 创建游戏实例
     */
    private BaseGamePlay createGameInstance(ENGameInfo gameInfo, String roomCode, List<String> playerIds) {
        try {
            // 直接使用playServiceName作为类名（例如：SevenPickGamePlay）
            String className = "com.bot.base.service.game.impl." + gameInfo.getPlayServiceName();
            
            // 反射创建游戏实例
            Class<?> clazz = Class.forName(className);
            return (BaseGamePlay) clazz.getDeclaredConstructor(
                    String.class, String.class, String.class, List.class)
                    .newInstance(roomCode, gameInfo.getCode(), gameInfo.getName(), playerIds);
                    
        } catch (Exception e) {
            log.error("创建游戏实例失败: {}, playServiceName: {}", 
                    gameInfo.getName(), gameInfo.getPlayServiceName(), e);
            throw new RuntimeException("游戏实例创建失败: " + e.getMessage());
        }
    }

    /**
     * 检查并清理超时的游戏(超过10分钟无操作)
     */
    public void cleanTimeoutGames() {
        try {
            long currentTime = System.currentTimeMillis();
            long timeoutMillis = 10 * 60 * 1000; // 10分钟
            
            List<String> timeoutRoomCodes = new ArrayList<>();
            List<String> endedRoomCodes = new ArrayList<>();
            
            // 检查所有游戏实例
            for (Map.Entry<String, BaseGamePlay> entry : GAME_INSTANCES.entrySet()) {
                String roomCode = entry.getKey();
                BaseGamePlay gamePlay = entry.getValue();
                
                // 检查游戏是否自然结束
                if (gamePlay.isGameEnded()) {
                    endedRoomCodes.add(roomCode);
                    log.info("检测到房间[{}]游戏已结束,准备结算和解散", roomCode);
                } else if (currentTime - gamePlay.getLastActivityTime() > timeoutMillis) {
                    timeoutRoomCodes.add(roomCode);
                    log.info("检测到房间[{}]游戏超时10分钟无操作,准备解散", roomCode);
                }
            }
            
            // 处理自然结束的游戏(结算积分后解散房间)
            for (String roomCode : endedRoomCodes) {
                finishGame(roomCode);
            }
            
            // 解散超时的游戏(不结算积分)
            for (String roomCode : timeoutRoomCodes) {
                BaseGamePlay gamePlay = GAME_INSTANCES.get(roomCode);
                if (gamePlay != null) {
                    // 发送解散通知
                    String message = String.format("房间[%s] 游戏[%s]超过10分钟无操作,自动解散~", 
                            roomCode, gamePlay.getGameName());
                    sendGameBroadcastMessage(gamePlay, message);
                    
                    // 直接解散房间(不结算积分)
                    handlePlayerQuitGame(roomCode);
                }
            }
            
            if (!endedRoomCodes.isEmpty()) {
                log.info("结算并解散了{}个自然结束的游戏房间", endedRoomCodes.size());
            }
            if (!timeoutRoomCodes.isEmpty()) {
                log.info("清理了{}个超时游戏房间", timeoutRoomCodes.size());
            }
            
        } catch (Exception e) {
            log.error("清理超时游戏失败", e);
        }
    }

    /**
     * 向游戏房间所有玩家发送广播消息
     */
    private void sendGameBroadcastMessage(BaseGamePlay gamePlay, String message) {
        Map<String, String> participationMap = gamePlay.getParticipationMap();
        List<String> playerIds = gamePlay.getPlayerIds();
        
        if (participationMap == null || playerIds == null) {
            return;
        }
        
        Map<String, List<String>> groupPlayers = new HashMap<>();
        List<String> privatePlayers = new ArrayList<>();
        
        // 分组
        for (String playerId : playerIds) {
            String groupId = participationMap.get(playerId);
            if (groupId != null && !groupId.trim().isEmpty()) {
                groupPlayers.computeIfAbsent(groupId, k -> new ArrayList<>()).add(playerId);
            } else {
                privatePlayers.add(playerId);
            }
        }
        
        // 群聊发送(每个群只发一次)
        for (Map.Entry<String, List<String>> entry : groupPlayers.entrySet()) {
            SendMsgUtil.sendGroupMsg(entry.getKey(), message, "");
        }
        
        // 私聊发送
        for (String playerId : privatePlayers) {
            SendMsgUtil.sendMsg(playerId, message);
        }
    }

    /**
     * 处理玩家退出游戏 - 解散房间
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePlayerQuitGame(String roomCode) {
        try {
            // 1. 结束游戏实例(不结算积分)
            BaseGamePlay gamePlay = GAME_INSTANCES.remove(roomCode);
            if (gamePlay != null) {
                gamePlay.endGame();
                
                // 2. 清除玩家映射
                for (String playerId : gamePlay.getPlayerIds()) {
                    USER_IN_GAME_ROOM.remove(playerId);
                    USER_PARTICIPATION_GROUP.remove(playerId);
                }
            }

            // 3. 删除房间玩家关联
            BotGameRoom room = roomMapper.selectByRoomCode(roomCode);
            if (room != null) {
                BotGameRoomPlayerExample playerExample = new BotGameRoomPlayerExample();
                playerExample.createCriteria().andRoomIdEqualTo(room.getId());
                roomPlayerMapper.deleteByExample(playerExample);

                // 4. 删除房间
                roomMapper.deleteByPrimaryKey(room.getId());
            }

            log.info("房间[{}]因玩家退出而解散", roomCode);

        } catch (Exception e) {
            log.error("处理玩家退出游戏失败, 房间:{}", roomCode, e);
        }
    }
}
