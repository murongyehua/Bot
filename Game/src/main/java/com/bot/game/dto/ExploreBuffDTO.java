package com.bot.game.dto;

import com.bot.game.enums.ENGoodEffect;
import lombok.Data;

import java.util.Date;

/**
 * @author murongyehua
 * @version 1.0 2020/10/19
 */
@Data
public class ExploreBuffDTO {

    private ENGoodEffect enGoodEffect;

    private Date outTime;

}
