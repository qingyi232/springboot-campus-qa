package com.campus.qa.controller;

import com.campus.qa.common.Result;
import com.campus.qa.entity.User;
import com.campus.qa.service.UserService;
import com.campus.qa.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            
            String token = userService.login(username, password);
            User user = userService.findByUsername(username);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        try {
            boolean success = userService.register(user);
            return success ? Result.success("注册成功") : Result.error("注册失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // 从token中获取用户名
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String username = jwtUtil.getUsernameFromToken(token);
                User user = userService.findByUsername(username);
                if (user != null) {
                    // 清除密码字段
                    user.setPassword(null);
                    return Result.success(user);
                }
            }
            return Result.error("用户未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

