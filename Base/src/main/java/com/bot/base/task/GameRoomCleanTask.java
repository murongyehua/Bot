package com.bot.base.task;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.base.service.impl.GameRoomManager;
import com.bot.common.constant.GameRoomConsts;
import com.bot.game.dao.entity.BotGameRoom;
import com.bot.game.dao.entity.BotGameRoomPlayerExample;
import com.bot.game.dao.mapper.BotGameRoomMapper;
import com.bot.game.dao.mapper.BotGameRoomPlayerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 游戏房间清理定时任务
 * 每分钟执行一次，清理超时10分钟的等待中房间和游戏中房间
 * @author Assistant
 */
@Slf4j
@Component
public class GameRoomCleanTask {

    @Autowired
    private BotGameRoomMapper roomMapper;

    @Autowired
    private BotGameRoomPlayerMapper roomPlayerMapper;

    @Autowired
    private GameRoomManager gameRoomManager;

    @PostConstruct
    public void init() {
        log.info("GameRoomCleanTask 定时任务已初始化，将每分钟执行一次清理任务");
    }

    /**
     * 每分钟执行一次，清理超时房间
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void cleanTimeoutRooms() {
        log.info("GameRoomCleanTask 开始执行 - 清理超时房间定时任务");
        try {
            // 1. 清理超时的等待中房间
            cleanWaitingRooms();
            
            // 2. 清理超时的游戏中房间
            gameRoomManager.cleanTimeoutGames();
            
            log.info("GameRoomCleanTask 执行完成");
        } catch (Exception e) {
            log.error("GameRoomCleanTask 执行异常", e);
        }
    }

    /**
     * 清理超时的等待中房间
     */
    @Transactional(rollbackFor = Exception.class)
    private void cleanWaitingRooms() {
        try {
            // 查询超时的等待中房间
            List<BotGameRoom> timeoutRooms = roomMapper.selectTimeoutWaitingRooms();
            
            if (CollectionUtil.isEmpty(timeoutRooms)) {
                return;
            }

            log.info("开始清理超时房间，共{}个", timeoutRooms.size());

            for (BotGameRoom room : timeoutRooms) {
                try {
                    // 删除房间玩家关联
                    BotGameRoomPlayerExample playerExample = new BotGameRoomPlayerExample();
                    playerExample.createCriteria().andRoomIdEqualTo(room.getId());
                    int playerCount = roomPlayerMapper.deleteByExample(playerExample);

                    // 删除房间
                    roomMapper.deleteByPrimaryKey(room.getId());

                    log.info("清理超时房间成功 - 房间号:{}, 游戏:{}, 创建时间:{}, 最后活跃:{}, 清理玩家数:{}",
                            room.getRoomCode(),
                            room.getGameName(),
                            room.getCreateTime(),
                            room.getLastTime(),
                            playerCount);

                } catch (Exception e) {
                    log.error("清理房间[{}]失败", room.getRoomCode(), e);
                }
            }

            log.info("超时房间清理完成");

        } catch (Exception e) {
            log.error("清理超时房间任务执行失败", e);
        }
    }
}
