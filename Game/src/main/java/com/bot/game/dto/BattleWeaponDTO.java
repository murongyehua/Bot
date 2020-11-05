package com.bot.game.dto;

import com.bot.game.enums.ENWeaponEffect;
import lombok.Data;

/**
 * @author liul
 * @version 1.0 2020/11/5
 */
@Data
public class BattleWeaponDTO {

    private ENWeaponEffect enWeaponEffect;

    private Integer level;

}
