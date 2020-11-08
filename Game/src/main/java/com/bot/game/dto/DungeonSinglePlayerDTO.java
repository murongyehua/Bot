package com.bot.game.dto;

import com.bot.game.dao.entity.PlayerPhantom;
import lombok.Data;

import java.util.List;

/**
 * @author murongyehua
 * @version 1.0 2020/11/5
 */
@Data
public class DungeonSinglePlayerDTO {

    private String playerId;

    private List<PlayerPhantom> phantoms;

}
