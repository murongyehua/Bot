package com.bot.base.service.impl;

import cn.hutool.core.util.StrUtil;
import com.bot.base.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author liul
 * @version 1.0 2020/10/29
 */
@Service("voteServiceImpl")
public class VoteServiceImpl implements BaseService {

    public static Map<String, Integer> votes = new HashMap<>();

    public static List<String> voted = new LinkedList<>();


    @Override
    public String doQueryReturn(String reqContent, String token) {
        if (voted.contains(token)) {
            return "你已经投过票了";
        }
        String voteContent = reqContent.substring(2);
        String[] contents = voteContent.trim().split(StrUtil.DASHED);
        List<String> tempList = Arrays.asList(contents);
        List<String> list = new ArrayList<>(tempList);
        if (list.size() != 3) {
            return "必须投3个人，不能弃票，不能多投";
        }

        for (String content : contents) {
            list.remove(content);
            if (list.contains(content)) {
                return "不能给同一人投票";
            }
            votes.merge(content, 1, (a, b) -> a + b);
        }
        voted.add(token);
        return "投票成功!";
    }
}
