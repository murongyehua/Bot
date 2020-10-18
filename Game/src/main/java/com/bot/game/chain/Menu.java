package com.bot.game.chain;

import cn.hutool.core.util.StrUtil;
import com.bot.commom.constant.BaseConsts;
import com.bot.commom.exception.BotException;
import com.bot.game.service.impl.CommonPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * 菜单执行者共用基类
 * @author murongyehua
 * @version 1.0 2020/10/14
 */
public class Menu implements MenuPrinter{

    protected Map<String, Menu> menuChildrenMap = new HashMap<>();

    protected Map<String, CommonPlayer> playServiceMap = new HashMap<>();

    protected String menuName;

    protected String describe = StrUtil.EMPTY;

    protected static Map<String, Object> mapperMap;

    public String print(String token) {
        return this.printMenuMap(token);
    }

    private String printMenuMap(String token) {
        StringBuilder stringBuilder = new StringBuilder();
        this.getDescribe(token);
        stringBuilder.append(StrUtil.BRACKET_START).append(menuName).append(StrUtil.BRACKET_END).append(StrUtil.CRLF).append(StrUtil.CRLF);
        stringBuilder.append(describe).append(StrUtil.CRLF);
        menuChildrenMap.keySet().forEach(sort ->
                stringBuilder.append(sort).append(StrUtil.DOT).append(StrUtil.SPACE)
                        .append(menuChildrenMap.get(sort).menuName).append(StrUtil.CRLF));
        playServiceMap.keySet().forEach(sort ->
                stringBuilder.append(sort).append(StrUtil.DOT).append(StrUtil.SPACE)
                        .append(playServiceMap.get(sort).title).append(StrUtil.CRLF));
        this.appendTurnBack(stringBuilder);
        // 选项与结尾之间换一下行
        stringBuilder.append(StrUtil.CRLF);
        return stringBuilder.toString();
    }

    protected void appendTurnBack(StringBuilder stringBuilder) {
        // 添加返回选项
        stringBuilder.append(BaseConsts.Menu.ZERO).append(StrUtil.DOT).append(StrUtil.SPACE).append(BaseConsts.Menu.TURN_BACK);
    }

    @Override
    public void initMenu() {
        throw new BotException("子类实现");
    }

    @Override
    public void getDescribe(String token) {
        throw new BotException("子类实现");
    }
}