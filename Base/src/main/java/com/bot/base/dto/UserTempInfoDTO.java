package com.bot.base.dto;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author murongyehua
 * @version 1.0 2020/9/28
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserTempInfoDTO {

    private String token;

    private Date outTime;

    private Boolean active;

    public UserTempInfoDTO(String token) {
        this.token = token;
        // 默认有效时间 1min
        this.outTime = DateUtil.offset(new Date(), DateField.MINUTE, 1);
        this.active = false;
    }

}
