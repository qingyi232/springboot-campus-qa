package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.qa.entity.Activity;
import com.campus.qa.entity.ActivityRegistration;
import com.campus.qa.mapper.ActivityRegistrationMapper;
import com.campus.qa.service.ActivityRegistrationService;
import com.campus.qa.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 活动报名服务实现类
 */
@Service
public class ActivityRegistrationServiceImpl extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration> 
        implements ActivityRegistrationService {
    
    @Autowired
    private ActivityService activityService;
    
    @Override
    @Transactional
    public boolean register(ActivityRegistration registration) {
        // 检查是否已报名
        if (hasRegistered(registration.getActivityId(), registration.getUserId())) {
            throw new RuntimeException("您已报名该活动");
        }
        
        // 检查活动是否存在
        Activity activity = activityService.getById(registration.getActivityId());
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        // 检查是否在报名截止时间内
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(activity.getRegistrationDeadline())) {
            throw new RuntimeException("报名已截止");
        }
        
        // 检查报名人数是否已满
        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new RuntimeException("报名人数已满");
        }
        
        // 设置默认审核状态为待审核
        if (registration.getStatus() == null) {
            registration.setStatus(0);
        }
        
        boolean result = save(registration);
        
        // 更新活动报名人数
        if (result) {
            activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
            activityService.updateById(activity);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean cancelRegistration(Long id) {
        ActivityRegistration registration = getById(id);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }
        
        boolean result = removeById(id);
        
        // 更新活动报名人数
        if (result) {
            Activity activity = activityService.getById(registration.getActivityId());
            if (activity != null && activity.getCurrentParticipants() > 0) {
                activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
                activityService.updateById(activity);
            }
        }
        
        return result;
    }
    
    @Override
    public boolean auditRegistration(Long id, Integer auditStatus, String auditRemark) {
        ActivityRegistration registration = getById(id);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }

        registration.setStatus(auditStatus);
        // registration.setAuditRemark(auditRemark); // 如果有审核备注字段

        return updateById(registration);
    }
    
    @Override
    public boolean checkIn(Long id) {
        ActivityRegistration registration = getById(id);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }
        
        if (registration.getStatus() != 1) {
            throw new RuntimeException("报名审核未通过，无法签到");
        }
        
        // 注意：checkInStatus在数据库中不存在，移除此逻辑
        
        return true;
    }
    
    @Override
    public boolean hasRegistered(Long activityId, Long userId) {
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityRegistration::getActivityId, activityId);
        wrapper.eq(ActivityRegistration::getUserId, userId);
        
        return count(wrapper) > 0;
    }
    
    @Override
    public IPage<ActivityRegistration> pageRegistrations(Page<ActivityRegistration> page,
                                                        Long activityId, Long userId, Integer auditStatus) {
        QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();

        if (activityId != null) {
            wrapper.eq("ar.activity_id", activityId);
        }

        if (userId != null) {
            wrapper.eq("ar.user_id", userId);
        }

        if (auditStatus != null) {
            wrapper.eq("ar.status", auditStatus);
        }

        // The ordering is now handled in the custom SQL of findPage
        // wrapper.orderByDesc(ActivityRegistration::getCreateTime);

        return baseMapper.findPage(page, wrapper);
    }
}

