package com.campus.qa.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.qa.entity.Activity;

import java.util.Map;

/**
 * 活动服务接口
 */
public interface ActivityService extends IService<Activity> {
    
    /**
     * 发布活动
     */
    boolean publishActivity(Activity activity);
    
    /**
     * 分页查询活动
     */
    IPage<Activity> pageActivities(Page<Activity> page, String keyword, String type, Integer status);
    
    /**
     * 更新活动状态
     */
    boolean updateActivityStatus(Long id, Integer status);
    
    /**
     * 增加浏览次数
     */
    boolean increaseViewCount(Long id);
    
    /**
     * 获取活动统计数据
     */
    Map<String, Object> getActivityStatistics();
}


