package com.bot.boot.controller;


public class BotDo {

    @lombok.Data
    public static class BotEvent {
        private BotToken d;
        private int op;

    }
    @lombok.Data
    public static class BotToken {
        private String plain_token;
        private String event_ts;
    }

}
