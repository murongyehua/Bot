package com.bot.game.dto;

import com.bot.game.dao.entity.PlayerPhantom;
import lombok.Data;

/**
 * @author liul
 * @version 1.0 2020/10/18
 */
@Data
public class BattlePhantomDTO extends PlayerPhantom {

    private static final long serialVersionUID = -2213251202460244234L;

    private Integer finalAttack;

    private Integer finalSpeed;

    private Integer finalDefense;
}
