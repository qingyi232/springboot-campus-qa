package com.campus.qa.controller;

import com.campus.qa.common.Result;
import com.campus.qa.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private QaConversationService conversationService;

    /**
     * 获取首页统计数据
     */
    @GetMapping("/home")
    public Result<Map<String, Object>> getHomeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计注册用户数
        long userCount = userService.count();
        stats.put("userCount", userCount);
        
        // 统计精彩活动数
        long activityCount = activityService.count();
        stats.put("activityCount", activityCount);
        
        // 统计校园资讯数
        long newsCount = newsService.count();
        stats.put("newsCount", newsCount);
        
        // 统计问答次数
        long qaCount = conversationService.count();
        stats.put("qaCount", qaCount);
        
        return Result.success(stats);
    }
}


