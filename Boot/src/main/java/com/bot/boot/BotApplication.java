package com.bot.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * @author murongyehua
 */
@SpringBootApplication(scanBasePackages = {"com.bot.boot","com.bot.base","com.bot.game"})
@MapperScan("com.bot.game.dao.mapper")
public class BotApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }

}
