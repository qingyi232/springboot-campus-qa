package com.campus.qa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.common.Result;
import com.campus.qa.entity.ChatMessage;
import com.campus.qa.entity.CustomerServiceSession;
import com.campus.qa.entity.User;
import com.campus.qa.service.CustomerServiceService;
import com.campus.qa.service.UserService;
import com.campus.qa.util.JwtUtil;
import com.campus.qa.websocket.CustomerServiceWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客服服务Controller
 */
@RestController
@RequestMapping("/api/customer-service")
public class CustomerServiceController {
    
    @Autowired
    private CustomerServiceService customerServiceService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    /**
     * 创建或获取客服会话
     */
    @GetMapping("/session")
    public Result getSession(HttpServletRequest request) {
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
        
        CustomerServiceSession session = customerServiceService.getOrCreateSession(user.getId(), username);
        return Result.success(session);
    }
    
    /**
     * 获取会话详情
     */
    @GetMapping("/session/{sessionId}")
    public Result getSessionDetail(@PathVariable Long sessionId) {
        CustomerServiceSession session = customerServiceService.getSessionById(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }
        return Result.success(session);
    }
    
    /**
     * 管理员接入会话
     */
    @PostMapping("/session/{sessionId}/accept")
    @PreAuthorize("hasAuthority('admin')")
    public Result acceptSession(@PathVariable Long sessionId, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String serviceName = jwtUtil.getUsernameFromToken(token);
        User user = userService.findByUsername(serviceName);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        boolean success = customerServiceService.acceptSession(sessionId, user.getId(), serviceName);
        return success ? Result.success() : Result.error("接入会话失败");
    }
    
    /**
     * 关闭会话
     */
    @PostMapping("/session/{sessionId}/close")
    public Result closeSession(@PathVariable Long sessionId) {
        boolean success = customerServiceService.closeSession(sessionId);
        return success ? Result.success() : Result.error("关闭会话失败");
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/message")
    public Result sendMessage(@RequestBody ChatMessage message, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        message.setSenderId(user.getId());
        message.setSenderName(username);
        message.setSenderRole(role.equals("ADMIN") ? "admin" : "student");
        
        ChatMessage savedMessage = customerServiceService.sendMessage(message);
        return Result.success(savedMessage);
    }
    
    /**
     * 获取会话消息历史
     */
    @GetMapping("/session/{sessionId}/messages")
    public Result getSessionMessages(@PathVariable Long sessionId) {
        List<ChatMessage> messages = customerServiceService.getSessionMessages(sessionId);
        return Result.success(messages);
    }
    
    /**
     * 标记消息已读
     */
    @PostMapping("/session/{sessionId}/read")
    public Result markAsRead(@PathVariable Long sessionId, HttpServletRequest request) {
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
        
        boolean success = customerServiceService.markMessagesAsRead(sessionId, user.getId());
        return success ? Result.success() : Result.error("标记失败");
    }
    
    /**
     * 获取等待中的会话列表（管理员）
     */
    @GetMapping("/waiting-sessions")
    @PreAuthorize("hasAuthority('admin')")
    public Result getWaitingSessions(@RequestParam(defaultValue = "1") int current,
                                    @RequestParam(defaultValue = "10") int size) {
        Page<CustomerServiceSession> page = customerServiceService.getWaitingSessions(current, size);
        return Result.success(page);
    }
    
    /**
     * 获取服务中的会话列表（管理员）
     */
    @GetMapping("/serving-sessions")
    @PreAuthorize("hasAuthority('admin')")
    public Result getServingSessions(@RequestParam(defaultValue = "1") int current,
                                    @RequestParam(defaultValue = "10") int size) {
        Page<CustomerServiceSession> page = customerServiceService.getServingSessions(current, size);
        return Result.success(page);
    }
    
    /**
     * 获取管理员的会话列表
     */
    @GetMapping("/my-sessions")
    @PreAuthorize("hasAuthority('admin')")
    public Result getMySessions(HttpServletRequest request,
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
        
        Page<CustomerServiceSession> page = customerServiceService.getAdminSessions(user.getId(), current, size);
        return Result.success(page);
    }
    
    /**
     * 获取在线状态
     */
    @GetMapping("/online-status")
    public Result getOnlineStatus(@RequestParam String userId, @RequestParam String role) {
        boolean online = CustomerServiceWebSocket.isOnline(userId, role);
        Map<String, Object> result = new HashMap<>();
        result.put("online", online);
        result.put("userId", userId);
        result.put("role", role);
        return Result.success(result);
    }
    
    /**
     * 获取在线人数
     */
    @GetMapping("/online-count")
    public Result getOnlineCount() {
        int count = CustomerServiceWebSocket.getOnlineCount();
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }
}

