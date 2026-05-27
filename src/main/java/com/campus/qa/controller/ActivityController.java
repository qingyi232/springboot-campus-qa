package com.campus.qa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.common.Result;
import com.campus.qa.entity.Activity;
import com.campus.qa.entity.ActivityRegistration;
import com.campus.qa.entity.User;
import com.campus.qa.service.ActivityService;
import com.campus.qa.service.ActivityRegistrationService;
import com.campus.qa.service.UserService;
import com.campus.qa.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 活动管理控制器
 */
@RestController
@RequestMapping("/activity")
@CrossOrigin
public class ActivityController {
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private ActivityRegistrationService registrationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 分页查询活动列表
     */
    @GetMapping("/page")
    public Result<IPage<Activity>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        Page<Activity> page = new Page<>(current, size);
        IPage<Activity> result = activityService.pageActivities(page, keyword, type, status);
        return Result.success(result);
    }
    
    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public Result<Activity> getById(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        if (activity != null) {
            activityService.increaseViewCount(id);
            return Result.success(activity);
        }
        return Result.error("活动不存在");
    }
    
    /**
     * 发布活动
     */
    @PostMapping
    public Result<String> publish(@RequestBody Activity activity) {
        boolean success = activityService.publishActivity(activity);
        return success ? Result.success("发布成功") : Result.error("发布失败");
    }
    
    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        boolean success = activityService.updateById(activity);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = activityService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 更新活动状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> data) {
        boolean success = activityService.updateActivityStatus(id, data.get("status"));
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }
    
    /**
     * 活动报名
     */
    @PostMapping("/{id}/register")
    public Result<String> register(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 1. 获取当前登录用户
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("请先登录");
            }
            token = token.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 2. 验证活动状态
            Activity activity = activityService.getById(id);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            if (activity.getStatus() != 1) {
                return Result.error("活动未开始或已结束");
            }
            if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
                return Result.error("活动名额已满");
            }
            
            // 3. 检查是否已经报名过
            if (registrationService.hasRegistered(id, user.getId())) {
                return Result.error("您已经报名过该活动");
            }
            
            // 4. 创建报名记录
            ActivityRegistration registration = new ActivityRegistration();
            registration.setActivityId(id);
            registration.setUserId(user.getId());
            registration.setUserName(user.getRealName());
            registration.setStudentId(user.getStudentId());
            registration.setPhone(user.getPhone());
            registration.setStatus(0); // 待审核
            
            System.out.println("========== 开始保存报名记录 ==========");
            System.out.println("活动ID: " + id);
            System.out.println("用户ID: " + user.getId());
            System.out.println("用户姓名: " + user.getRealName());
            
            boolean success = registrationService.save(registration);
            
            System.out.println("保存结果: " + success);
            if (success && registration.getId() != null) {
                System.out.println("生成的报名记录ID: " + registration.getId());
            } else {
                System.out.println("保存失败或未生成ID");
            }
            System.out.println("========================================");
            
            if (!success) {
                return Result.error("报名失败");
            }
            
            // 5. 增加参与人数
            activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
            activityService.updateById(activity);
            
            return Result.success("报名成功，等待审核");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("========== 报名异常 ==========");
            System.out.println("异常信息: " + e.getMessage());
            System.out.println("============================");
            return Result.error("报名失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取活动统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = activityService.getActivityStatistics();
        return Result.success(statistics);
    }
}

