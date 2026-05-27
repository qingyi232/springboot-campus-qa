package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.qa.entity.User;
import com.campus.qa.mapper.UserMapper;
import com.campus.qa.service.UserService;
import com.campus.qa.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public String login(String username, String password) {
        log.info("登录请求 - 用户名: {}", username);
        
        User user = findByUsername(username);
        if (user == null) {
            log.error("用户不存在: {}", username);
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getStatus() == 0) {
            log.error("用户已被禁用: {}", username);
            throw new RuntimeException("用户已被禁用");
        }
        
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches) {
            log.error("密码错误 - 用户名: {}", username);
            throw new RuntimeException("密码错误");
        }
        
        String token = jwtUtil.generateToken(username, user.getRole());
        log.info("登录成功 - 用户: {}, 角色: {}", username, user.getRole());
        return token;
    }
    
    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 默认角色为学生
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole("STUDENT");
        }
        
        // 默认状态为启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        return save(user);
    }
    
    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }
    
    @Override
    public IPage<User> pageUsers(Page<User> page, String keyword, String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getStudentId, keyword));
        }
        
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public boolean updateStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(status);
        return updateById(user);
    }
    
    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }
}

