package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.qa.entity.Activity;
import com.campus.qa.entity.ActivityRegistration;
import com.campus.qa.mapper.ActivityMapper;
import com.campus.qa.mapper.ActivityRegistrationMapper;
import com.campus.qa.service.RegistrationStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报名统计分析服务实现类
 */
@Service
public class RegistrationStatisticsServiceImpl implements RegistrationStatisticsService {
    
    @Autowired
    private ActivityRegistrationMapper registrationMapper;
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Override
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 总活动数
        Long totalActivities = activityMapper.selectCount(null);
        result.put("totalActivities", totalActivities);
        
        // 总报名数
        Long totalRegistrations = registrationMapper.selectCount(null);
        result.put("totalRegistrations", totalRegistrations);
        
        // 已通过报名数
        LambdaQueryWrapper<ActivityRegistration> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(ActivityRegistration::getStatus, 1);
        Long approvedRegistrations = registrationMapper.selectCount(approvedWrapper);
        result.put("approvedRegistrations", approvedRegistrations);
        
        // 待审核报名数
        LambdaQueryWrapper<ActivityRegistration> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(ActivityRegistration::getStatus, 0);
        Long pendingRegistrations = registrationMapper.selectCount(pendingWrapper);
        result.put("pendingRegistrations", pendingRegistrations);
        
        // 本月新增报名数
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LambdaQueryWrapper<ActivityRegistration> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.ge(ActivityRegistration::getCreateTime, monthStart);
        Long monthRegistrations = registrationMapper.selectCount(monthWrapper);
        result.put("monthRegistrations", monthRegistrations);
        
        // 今日新增报名数
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<ActivityRegistration> dayWrapper = new LambdaQueryWrapper<>();
        dayWrapper.ge(ActivityRegistration::getCreateTime, dayStart);
        Long dayRegistrations = registrationMapper.selectCount(dayWrapper);
        result.put("dayRegistrations", dayRegistrations);
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getRegistrationTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        // 获取时间范围内的所有报名
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(ActivityRegistration::getCreateTime, startDate.atStartOfDay())
               .le(ActivityRegistration::getCreateTime, endDate.plusDays(1).atStartOfDay());
        List<ActivityRegistration> registrations = registrationMapper.selectList(wrapper);
        
        // 按日期分组统计
        Map<LocalDate, Long> dateCountMap = registrations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getCreateTime().toLocalDate(),
                Collectors.counting()
            ));
        
        // 填充每一天的数据
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("count", dateCountMap.getOrDefault(date, 0L));
            result.add(dayData);
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        // 获取所有活动
        List<Activity> activities = activityMapper.selectList(null);
        
        // 按类型分组
        Map<String, List<Activity>> categoryMap = activities.stream()
            .collect(Collectors.groupingBy(Activity::getType));
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map.Entry<String, List<Activity>> entry : categoryMap.entrySet()) {
            String category = entry.getKey();
            List<Activity> categoryActivities = entry.getValue();

            long totalRegistrations = 0;
            List<String> activityNames = new ArrayList<>();
            for (Activity activity : categoryActivities) {
                LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ActivityRegistration::getActivityId, activity.getId());
                totalRegistrations += registrationMapper.selectCount(wrapper);
                activityNames.add(activity.getTitle());
            }

            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("category", category);
            categoryData.put("activityCount", categoryActivities.size());
            categoryData.put("registrationCount", totalRegistrations);
            categoryData.put("activityNames", activityNames);
            result.add(categoryData);
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getPopularActivities(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认前10
        }
        
        // 获取所有活动
        List<Activity> activities = activityMapper.selectList(null);
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Activity activity : activities) {
            // 统计每个活动的报名数
            LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ActivityRegistration::getActivityId, activity.getId());
            Long registrationCount = registrationMapper.selectCount(wrapper);
            
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("activityId", activity.getId());
            activityData.put("activityName", activity.getTitle());
            activityData.put("category", activity.getType());
            activityData.put("registrationCount", registrationCount);
            activityData.put("maxParticipants", activity.getMaxParticipants());
            activityData.put("startTime", activity.getStartTime());
            result.add(activityData);
        }
        
        // 按报名数降序排序，取前limit个
        result.sort((a, b) -> Long.compare(
            (Long) b.get("registrationCount"), 
            (Long) a.get("registrationCount")
        ));
        
        return result.stream().limit(limit).collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getActivityRegistrationDetail(Long activityId) {
        Map<String, Object> result = new HashMap<>();
        
        // 活动信息
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return result;
        }
        
        result.put("activity", activity);
        
        // 总报名数
        LambdaQueryWrapper<ActivityRegistration> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(ActivityRegistration::getActivityId, activityId);
        Long totalRegistrations = registrationMapper.selectCount(totalWrapper);
        result.put("totalRegistrations", totalRegistrations);
        
        // 已通过
        LambdaQueryWrapper<ActivityRegistration> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(ActivityRegistration::getActivityId, activityId)
                      .eq(ActivityRegistration::getStatus, 1);
        Long approvedCount = registrationMapper.selectCount(approvedWrapper);
        result.put("approvedCount", approvedCount);
        
        // 待审核
        LambdaQueryWrapper<ActivityRegistration> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(ActivityRegistration::getActivityId, activityId)
                     .eq(ActivityRegistration::getStatus, 0);
        Long pendingCount = registrationMapper.selectCount(pendingWrapper);
        result.put("pendingCount", pendingCount);
        
        // 已拒绝
        LambdaQueryWrapper<ActivityRegistration> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(ActivityRegistration::getActivityId, activityId)
                      .eq(ActivityRegistration::getStatus, 2);
        Long rejectedCount = registrationMapper.selectCount(rejectedWrapper);
        result.put("rejectedCount", rejectedCount);
        
        // 报名率
        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
            double registrationRate = (double) totalRegistrations / activity.getMaxParticipants() * 100;
            result.put("registrationRate", Math.round(registrationRate * 100.0) / 100.0);
        } else {
            result.put("registrationRate", 0.0);
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getUserActivityStatistics(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20; // 默认前20
        }
        
        // 获取所有报名记录
        List<ActivityRegistration> registrations = registrationMapper.selectList(null);
        
        // 按用户分组统计
        Map<Long, List<ActivityRegistration>> userMap = registrations.stream()
            .collect(Collectors.groupingBy(ActivityRegistration::getUserId));
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<ActivityRegistration>> entry : userMap.entrySet()) {
            Long userId = entry.getKey();
            List<ActivityRegistration> userRegistrations = entry.getValue();
            
            if (!userRegistrations.isEmpty()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", userId);
                userData.put("userName", userRegistrations.get(0).getUserName());
                userData.put("totalRegistrations", userRegistrations.size());
                
                // 已通过的报名数
                long approvedCount = userRegistrations.stream()
                    .filter(r -> r.getStatus() == 1)
                    .count();
                userData.put("approvedCount", approvedCount);
                
                result.add(userData);
            }
        }
        
        // 按总报名数降序排序
        result.sort((a, b) -> Integer.compare(
            (Integer) b.get("totalRegistrations"), 
            (Integer) a.get("totalRegistrations")
        ));
        
        return result.stream().limit(limit).collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> exportRegistrationData(Long activityId) {
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        
        if (activityId != null) {
            wrapper.eq(ActivityRegistration::getActivityId, activityId);
        }
        
        wrapper.orderByDesc(ActivityRegistration::getCreateTime);
        List<ActivityRegistration> registrations = registrationMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ActivityRegistration registration : registrations) {
            // 获取活动信息
            Activity activity = activityMapper.selectById(registration.getActivityId());
            
            Map<String, Object> data = new HashMap<>();
            data.put("registrationId", registration.getId());
            data.put("activityName", activity != null ? activity.getTitle() : "");
            data.put("userName", registration.getUserName());
            data.put("studentId", registration.getStudentId());
            data.put("phone", registration.getPhone());
            
            String statusText = "";
            if (registration.getStatus() == 0) {
                statusText = "待审核";
            } else if (registration.getStatus() == 1) {
                statusText = "已通过";
            } else if (registration.getStatus() == 2) {
                statusText = "已拒绝";
            }
            data.put("status", statusText);
            data.put("createTime", registration.getCreateTime());
            
            result.add(data);
        }
        
        return result;
    }
}

