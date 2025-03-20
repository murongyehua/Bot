package com.bot.base.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class ChatIdDTO {

    private String id;

    private Date lastDate;

}
