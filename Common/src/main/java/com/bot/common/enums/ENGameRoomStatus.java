package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 游戏房间状态枚举
 * @author Assistant
 */
@Getter
@AllArgsConstructor
public enum ENGameRoomStatus {

    /**
     * 等待中 - 可加入、可在大厅查看
     */
    WAITING("0", "等待中"),

    /**
     * 游戏中 - 不可加入、不在大厅显示
     */
    PLAYING("1", "游戏中");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static ENGameRoomStatus getByCode(String code) {
        for (ENGameRoomStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
