package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public enum ENChatEngine {

    CHATGPT("CHATGPT", "chat-gpt", new ArrayList<String>(){{add("CHATGPT");add("chatgpt");}}),
    TENCENT("TENCENT", "混元", new ArrayList<String>(){{add("混元");}}),
    DEFAULT("DEFAULT", "默认", new ArrayList<String>(){{add("默认");}});

    private final String value;

    private final String label;

    private final List<String> keyWords;

    public static String getValueByKeyWord(String keyWord) {
        for (ENChatEngine enChatEngine : ENChatEngine.values()) {
            if (enChatEngine.keyWords.contains(keyWord)) {
                return enChatEngine.value;
            }
        }
        return null;
    }

    public static ENChatEngine getByValue(String value) {
        for (ENChatEngine enChatEngine : ENChatEngine.values()) {
            if (enChatEngine.value.equals(value)) {
                return enChatEngine;
            }
        }
        return DEFAULT;
    }



}
