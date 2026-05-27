package com.campus.qa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.common.Result;
import com.campus.qa.entity.ActivityRegistration;
import com.campus.qa.service.ActivityRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 活动报名控制器
 */
@RestController
@RequestMapping("/activity-registration")
@CrossOrigin
public class ActivityRegistrationController {
    
    @Autowired
    private ActivityRegistrationService registrationService;
    
    /**
     * 分页查询报名记录
     */
    @GetMapping("/page")
    public Result<IPage<ActivityRegistration>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer auditStatus) {
        Page<ActivityRegistration> page = new Page<>(current, size);
        IPage<ActivityRegistration> result = registrationService.pageRegistrations(page, activityId, userId, auditStatus);
        return Result.success(result);
    }
    
    /**
     * 报名活动
     */
    @PostMapping
    public Result<String> register(@RequestBody ActivityRegistration registration) {
        try {
            boolean success = registrationService.register(registration);
            return success ? Result.success("报名成功") : Result.error("报名失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消报名
     */
    @DeleteMapping("/{id}")
    public Result<String> cancel(@PathVariable Long id) {
        try {
            boolean success = registrationService.cancelRegistration(id);
            return success ? Result.success("取消成功") : Result.error("取消失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 审核报名
     */
    @PutMapping("/{id}/audit")
    public Result<String> audit(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Integer auditStatus = (Integer) data.get("auditStatus");
        String auditRemark = (String) data.get("auditRemark");
        boolean success = registrationService.auditRegistration(id, auditStatus, auditRemark);
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }
    
    /**
     * 签到
     */
    @PutMapping("/{id}/checkin")
    public Result<String> checkIn(@PathVariable Long id) {
        try {
            boolean success = registrationService.checkIn(id);
            return success ? Result.success("签到成功") : Result.error("签到失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}


