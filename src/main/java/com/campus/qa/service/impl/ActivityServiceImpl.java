package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.qa.entity.Activity;
import com.campus.qa.mapper.ActivityMapper;
import com.campus.qa.service.ActivityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 活动服务实现类
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> 
        implements ActivityService {
    
    @Override
    public boolean publishActivity(Activity activity) {
        if (activity.getStatus() == null) {
            activity.setStatus(1); // 已发布
        }
        
        if (activity.getCurrentParticipants() == null) {
            activity.setCurrentParticipants(0);
        }
        
        // 移除viewCount字段相关逻辑
        
        return save(activity);
    }
    
    @Override
    public IPage<Activity> pageActivities(Page<Activity> page, String keyword, String type, Integer status) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Activity::getTitle, keyword)
                    .or().like(Activity::getDescription, keyword)
                    .or().like(Activity::getRequirements, keyword)
                    .or().like(Activity::getOrganizer, keyword);  // 新增：搜索参加要求和主办方
        }
        
        if (StringUtils.hasText(type)) {
            wrapper.eq(Activity::getType, type);
        }
        
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }
        
        wrapper.orderByDesc(Activity::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public boolean updateActivityStatus(Long id, Integer status) {
        Activity activity = getById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        activity.setStatus(status);
        return updateById(activity);
    }
    
    @Override
    public boolean increaseViewCount(Long id) {
        Activity activity = getById(id);
        if (activity == null) {
            return false;
        }
        
        // 移除浏览次数逻辑
        return updateById(activity);
    }
    
    @Override
    public Map<String, Object> getActivityStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总活动数
        long totalCount = count();
        statistics.put("totalCount", totalCount);
        
        // 进行中的活动数
        LambdaQueryWrapper<Activity> ongoingWrapper = new LambdaQueryWrapper<>();
        ongoingWrapper.eq(Activity::getStatus, 2);
        long ongoingCount = count(ongoingWrapper);
        statistics.put("ongoingCount", ongoingCount);
        
        // 已发布的活动数
        LambdaQueryWrapper<Activity> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(Activity::getStatus, 1);
        long publishedCount = count(publishedWrapper);
        statistics.put("publishedCount", publishedCount);
        
        return statistics;
    }
}

