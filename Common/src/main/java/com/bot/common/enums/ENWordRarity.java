package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENWordRarity {

    COMMON("1", "普通", "0.35"),
    RARE("2", "稀有", "0.30"),
    EPIC("3", "史诗", "0.25"),
    LEGENDARY("4", "传说", "0.10");

    private String value;

    private String label;

    private String probability;

    public static String getLabelByValue(String value) {
        for (ENWordRarity item : ENWordRarity.values()) {
            if (item.value.equals(value)) {
                return item.label;
            }
        }
        return null;
    }

}
