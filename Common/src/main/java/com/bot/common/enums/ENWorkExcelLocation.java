package com.bot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public enum ENWorkExcelLocation {

    ONE(1, 3, new ArrayList<String>(){{add("1店");}}, 3),
    TWO(1, 10, new ArrayList<String>(){{add("2店");}}, 15),
    THREE(1, 17, new ArrayList<String>(){{add("3店");}}, 27),
    FOUR(1, 24, new ArrayList<String>(){{add("4店");}}, 39),
    FIVE(1, 31, new ArrayList<String>(){{add("5店");}}, 51),
    SIX(1, 38, new ArrayList<String>(){{add("6店");}}, 63),
    SEVEN(1, 45, new ArrayList<String>(){{add("7店");}}, 75),
    EIGHT(1, 52, new ArrayList<String>(){{add("8店");}}, 87),
    NINE(1, 59, new ArrayList<String>(){{add("9店");}}, 99),
    TEN(1, 66, new ArrayList<String>(){{add("10店");}}, 111),
    ELEVEN(1, 73, new ArrayList<String>(){{add("11店");}}, 123),
    TWELVE(1, 80, new ArrayList<String>(){{add("12店");}}, 135),
    THIRTEEN(1, 87, new ArrayList<String>(){{add("13店");}}, 147),
    FOURTEEN(1, 94, new ArrayList<String>(){{add("14店");}}, 159),
    FIFTEEN(1, 101, new ArrayList<String>(){{add("15店");}}, 171),
    SIXTEEN(1, 108, new ArrayList<String>(){{add("16店");}}, 183),
    SEVENTEEN(1, 115, new ArrayList<String>(){{add("17店");}}, 195),
    EIGHTEEN(1, 122, new ArrayList<String>(){{add("18店");}}, 207),
    NINETEEN(1, 129, new ArrayList<String>(){{add("19店");}}, 219),
    TWENTY(1, 136, new ArrayList<String>(){{add("20店");}}, 231),
    TWENTY_ONE(1, 143, new ArrayList<String>(){{add("21店");}}, 243),
    TWENTY_TWO(1, 150, new ArrayList<String>(){{add("22店");}}, 255),
    // 以下枚举只用于提取功能
    TOTAL_DAY(1, 0, new ArrayList<String>(){{add("各店日");}}, 267),
    TOTAL_MONTH(1, 0, new ArrayList<String>(){{add("各店月");}}, 274),
    TODAY_SAVE(1, 0, new ArrayList<String>(){{add("汇总项起点");}}, 281);


    private final int baseIndexX;

    private final int baseIndexY;

    private final List<String> keyWords;

    private final int totalY;

}
