package com.bot.life.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 游戏状态实体
 * @author Assistant
 */
@Data
public class LifeGameStatus {
    private Long id;
    private String userId;
    private Integer gameMode; // 游戏模式：0未进入1预备状态2正式游戏
    private String currentMenu; // 当前菜单状态
    private String contextData; // JSON格式的上下文数据
    private Date createTime;
    private Date updateTime;
}
