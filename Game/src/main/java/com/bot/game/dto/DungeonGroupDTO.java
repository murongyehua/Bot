package com.bot.game.dto;

import com.bot.game.enums.ENDungeonResult;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author liul
 * @version 1.0 2020/11/5
 */
@Data
public class DungeonGroupDTO {

    private String name;

    private ENDungeonResult result;

    private List<DungeonSinglePlayerDTO> players;

    private Map<String, String> resultMap;

}
