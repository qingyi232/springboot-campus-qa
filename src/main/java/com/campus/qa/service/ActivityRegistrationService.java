package com.campus.qa.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.qa.entity.ActivityRegistration;

/**
 * 活动报名服务接口
 */
public interface ActivityRegistrationService extends IService<ActivityRegistration> {
    
    /**
     * 用户报名活动
     */
    boolean register(ActivityRegistration registration);
    
    /**
     * 取消报名
     */
    boolean cancelRegistration(Long id);
    
    /**
     * 审核报名
     */
    boolean auditRegistration(Long id, Integer auditStatus, String auditRemark);
    
    /**
     * 签到
     */
    boolean checkIn(Long id);
    
    /**
     * 检查用户是否已报名某活动
     */
    boolean hasRegistered(Long activityId, Long userId);
    
    /**
     * 分页查询报名记录
     */
    IPage<ActivityRegistration> pageRegistrations(Page<ActivityRegistration> page, 
                                                   Long activityId, Long userId, Integer auditStatus);
}


