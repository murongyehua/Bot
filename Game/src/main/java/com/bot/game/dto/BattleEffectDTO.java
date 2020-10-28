package com.bot.game.dto;

import com.bot.game.enums.ENSkillEffect;
import lombok.Data;

/**
 * @author murongyehua
 * @version 1.0 2020/10/19
 */
@Data
public class BattleEffectDTO {

    private Integer liveRound;

    private ENSkillEffect effect;

}
