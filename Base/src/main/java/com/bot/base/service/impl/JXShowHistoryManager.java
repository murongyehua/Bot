package com.bot.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.base.dto.jx.ShowHistoryResp;
import com.bot.game.dao.entity.JXShowHistoryQuery;
import com.bot.game.dao.entity.JXShowHistoryQueryExample;
import com.bot.game.dao.mapper.JXShowHistoryQueryMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JXShowHistoryManager {

    @Resource
    private JXShowHistoryQueryMapper showHistoryQueryMapper;

    public ShowHistoryResp getShows(String queryId) {
        if (StrUtil.isEmpty(queryId)) {
            return new ShowHistoryResp();
        }
        JXShowHistoryQueryExample example = new JXShowHistoryQueryExample();
        example.createCriteria().andQueryIdEqualTo(queryId);
        List<JXShowHistoryQuery> showHistoryQueryList = showHistoryQueryMapper.selectByExample(example);
        if (CollectionUtil.isEmpty(showHistoryQueryList)) {
            return new ShowHistoryResp();
        }
        List<String> images = showHistoryQueryList.stream().map(JXShowHistoryQuery::getShowUrl).collect(Collectors.toList());
        ShowHistoryResp showHistoryResp = new ShowHistoryResp();
        showHistoryResp.setImages(images);
        showHistoryResp.setTitle(showHistoryQueryList.get(0).getTitle());
        return showHistoryResp;
    }


}
