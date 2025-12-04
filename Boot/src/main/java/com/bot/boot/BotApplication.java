package com.bot.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 * @author murongyehua
 */
@SpringBootApplication(scanBasePackages = {"com.bot.boot","com.bot.base","com.bot.game","com.bot.life", "com.bot.common.loader"})
@MapperScan({"com.bot.game.dao.mapper", "com.bot.life.dao.mapper"})
@EnableScheduling
public class BotApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }

}
