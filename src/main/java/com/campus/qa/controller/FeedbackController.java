package com.campus.qa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.common.Result;
import com.campus.qa.entity.Feedback;
import com.campus.qa.entity.User;
import com.campus.qa.service.FeedbackService;
import com.campus.qa.service.UserService;
import com.campus.qa.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 问题反馈Controller
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    
    @Autowired
    private FeedbackService feedbackService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    /**
     * 提交反馈
     */
    @PostMapping("/submit")
    public Result submitFeedback(@RequestBody Feedback feedback, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        feedback.setUserId(user.getId());
        feedback.setUsername(username);
        
        boolean success = feedbackService.submitFeedback(feedback);
        return success ? Result.success("提交成功，我们会尽快处理") : Result.error("提交失败");
    }
    
    /**
     * 分页查询反馈列表（管理员）
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('admin')")
    public Result getFeedbackPage(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String feedbackType) {
        Page<Feedback> page = feedbackService.getFeedbackPage(current, size, status, feedbackType);
        return Result.success(page);
    }
    
    /**
     * 获取用户的反馈列表
     */
    @GetMapping("/my")
    public Result getMyFeedback(HttpServletRequest request,
                               @RequestParam(defaultValue = "1") int current,
                               @RequestParam(defaultValue = "10") int size) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        Page<Feedback> page = feedbackService.getUserFeedbackPage(user.getId(), current, size);
        return Result.success(page);
    }
    
    /**
     * 获取反馈详情
     */
    @GetMapping("/{id}")
    public Result getFeedbackDetail(@PathVariable Long id) {
        Feedback feedback = feedbackService.getFeedbackById(id);
        if (feedback == null) {
            return Result.error("反馈不存在");
        }
        return Result.success(feedback);
    }
    
    /**
     * 处理反馈（管理员）
     */
    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAuthority('admin')")
    public Result handleFeedback(@PathVariable Long id,
                                @RequestBody Map<String, String> params,
                                HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String handlerName = jwtUtil.getUsernameFromToken(token);
        User user = userService.findByUsername(handlerName);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        String status = params.get("status");
        String reply = params.get("reply");
        
        boolean success = feedbackService.handleFeedback(id, user.getId(), handlerName, status, reply);
        return success ? Result.success("处理成功") : Result.error("处理失败");
    }
    
    /**
     * 删除反馈（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public Result deleteFeedback(@PathVariable Long id) {
        boolean success = feedbackService.deleteFeedback(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 获取待处理反馈数量
     */
    @GetMapping("/pending-count")
    @PreAuthorize("hasAuthority('admin')")
    public Result getPendingCount() {
        long count = feedbackService.getPendingCount();
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }
}

