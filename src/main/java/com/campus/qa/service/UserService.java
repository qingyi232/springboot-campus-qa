package com.campus.qa.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.qa.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     */
    String login(String username, String password);
    
    /**
     * 用户注册
     */
    boolean register(User user);
    
    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);
    
    /**
     * 分页查询用户
     */
    IPage<User> pageUsers(Page<User> page, String keyword, String role);
    
    /**
     * 更新用户状态
     */
    boolean updateStatus(Long id, Integer status);
    
    /**
     * 重置密码
     */
    boolean resetPassword(Long id, String newPassword);
}


