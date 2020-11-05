package com.bot.game.dto;

import com.bot.game.dao.entity.BaseMonster;
import com.bot.game.dao.entity.PlayerWeapon;
import lombok.Data;

/**
 * @author murongyehua
 * @version 1.0 2020/10/18
 */
@Data
public class BattleMonsterDTO extends BaseMonster {
    private static final long serialVersionUID = 3811328146874283809L;

    private Integer finalAttack;

    private Integer finalSpeed;

    private Integer finalDefense;

    private Integer finalHp;

    private Integer hp;

    private BattleWeaponDTO battleWeaponDTO;

}
