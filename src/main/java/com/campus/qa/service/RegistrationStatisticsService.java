package com.campus.qa.service;

import java.util.List;
import java.util.Map;

/**
 * 报名统计分析服务接口
 */
public interface RegistrationStatisticsService {
    
    /**
     * 获取总体统计数据
     */
    Map<String, Object> getOverallStatistics();
    
    /**
     * 获取活动报名趋势数据
     */
    List<Map<String, Object>> getRegistrationTrend(Integer days);
    
    /**
     * 获取活动分类统计
     */
    List<Map<String, Object>> getCategoryStatistics();
    
    /**
     * 获取热门活动排行
     */
    List<Map<String, Object>> getPopularActivities(Integer limit);
    
    /**
     * 获取活动报名详情
     */
    Map<String, Object> getActivityRegistrationDetail(Long activityId);
    
    /**
     * 获取用户活跃度统计
     */
    List<Map<String, Object>> getUserActivityStatistics(Integer limit);
    
    /**
     * 导出报名数据
     */
    List<Map<String, Object>> exportRegistrationData(Long activityId);
}


