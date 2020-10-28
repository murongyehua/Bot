package com.bot.game.dto;

import com.bot.game.dao.entity.BaseSkill;
import lombok.Data;

/**
 * @author murongyehua
 * @version 1.0 2020/10/18
 */
@Data
public class BattleSkillDTO extends BaseSkill {
    private static final long serialVersionUID = -7674827774696669891L;

    private Integer nowWaitRound;
}
