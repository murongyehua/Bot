package com.bot.base.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePicReq {

    private String model;

    private String prompt;

    private String image_size;

    private Integer num_inference_steps;

}
