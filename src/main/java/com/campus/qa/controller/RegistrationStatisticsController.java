package com.campus.qa.controller;

import com.campus.qa.common.Result;
import com.campus.qa.service.RegistrationStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报名统计分析Controller
 */
@RestController
@RequestMapping("/api/registration-statistics")
@PreAuthorize("hasAuthority('admin')")
public class RegistrationStatisticsController {
    
    @Autowired
    private RegistrationStatisticsService statisticsService;
    
    /**
     * 获取总体统计数据
     */
    @GetMapping("/overall")
    public Result getOverallStatistics() {
        Map<String, Object> statistics = statisticsService.getOverallStatistics();
        return Result.success(statistics);
    }
    
    /**
     * 获取报名趋势数据
     */
    @GetMapping("/trend")
    public Result getRegistrationTrend(@RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> trend = statisticsService.getRegistrationTrend(days);
        return Result.success(trend);
    }
    
    /**
     * 获取活动分类统计
     */
    @GetMapping("/category")
    public Result getCategoryStatistics() {
        List<Map<String, Object>> statistics = statisticsService.getCategoryStatistics();
        return Result.success(statistics);
    }
    
    /**
     * 获取热门活动排行
     */
    @GetMapping("/popular")
    public Result getPopularActivities(@RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> activities = statisticsService.getPopularActivities(limit);
        return Result.success(activities);
    }
    
    /**
     * 获取活动报名详情
     */
    @GetMapping("/activity/{activityId}")
    public Result getActivityDetail(@PathVariable Long activityId) {
        Map<String, Object> detail = statisticsService.getActivityRegistrationDetail(activityId);
        return Result.success(detail);
    }
    
    /**
     * 获取用户活跃度统计
     */
    @GetMapping("/user-activity")
    public Result getUserActivityStatistics(@RequestParam(defaultValue = "20") Integer limit) {
        List<Map<String, Object>> statistics = statisticsService.getUserActivityStatistics(limit);
        return Result.success(statistics);
    }
    
    /**
     * 导出报名数据
     */
    @GetMapping("/export")
    public Result exportData(@RequestParam(required = false) Long activityId) {
        List<Map<String, Object>> data = statisticsService.exportRegistrationData(activityId);
        return Result.success(data);
    }
}


