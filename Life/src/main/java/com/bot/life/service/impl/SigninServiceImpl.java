package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifePlayerSignin;
import com.bot.life.dao.mapper.LifePlayerMapper;
import com.bot.life.dao.mapper.LifePlayerSigninMapper;
import com.bot.life.service.SigninService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * 签到服务实现
 * @author Assistant
 */
@Service
public class SigninServiceImpl implements SigninService {
    
    @Autowired
    private LifePlayerSigninMapper signinMapper;
    
    @Autowired
    private LifePlayerMapper playerMapper;
    
    private final Random random = new Random();
    
    @Override
    public String signin(Long playerId) {
        LifePlayer player = playerMapper.selectByPrimaryKey(playerId);
        if (player == null) {
            return "角色不存在！";
        }
        
        // 检查今日是否已签到
        if (hasSignedToday(playerId)) {
            return "『签到失败』\n\n今日已经签到过了，请明天再来！";
        }
        
        // 随机生成1-100的灵粹奖励
        int spiritReward = random.nextInt(100) + 1;
        
        try {
            // 创建签到记录
            LifePlayerSignin signin = new LifePlayerSignin();
            signin.setPlayerId(playerId);
            signin.setSigninDate(getTodayDate());
            signin.setSpiritReward(spiritReward);
            signin.setCreateTime(new Date());
            
            signinMapper.insert(signin);
            
            // 给玩家增加灵粹
            Long currentSpirit = player.getSpirit();
            if (currentSpirit == null) {
                currentSpirit = 0L;
            }
            player.setSpirit(currentSpirit + spiritReward);
            player.setUpdateTime(new Date());
            playerMapper.updateByPrimaryKey(player);
            
            return String.format("『签到成功！』\n\n恭喜获得 %d 灵粹！\n\n当前灵粹：%d\n\n明天记得继续签到哦～\n\n输入任意内容返回主菜单", 
                               spiritReward, player.getSpirit());
                               
        } catch (Exception e) {
            e.printStackTrace();
            return "『签到失败』\n\n签到过程中出现错误，请稍后再试！";
        }
    }
    
    @Override
    public boolean hasSignedToday(Long playerId) {
        Date today = getTodayDate();
        LifePlayerSignin todaySignin = signinMapper.selectTodaySignin(playerId, today);
        return todaySignin != null;
    }
    
    /**
     * 获取今天的日期（只保留年月日）
     */
    private Date getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
