package com.campus.qa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.common.Result;
import com.campus.qa.entity.User;
import com.campus.qa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public Result<IPage<User>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        Page<User> page = new Page<>(current, size);
        IPage<User> result = userService.pageUsers(page, keyword, role);
        return Result.success(result);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return user != null ? Result.success(user) : Result.error("用户不存在");
    }
    
    /**
     * 新增用户
     */
    @PostMapping
    public Result<String> add(@RequestBody User user) {
        try {
            boolean success = userService.register(user);
            return success ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        boolean success = userService.updateById(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> data) {
        boolean success = userService.updateStatus(id, data.get("status"));
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }
    
    /**
     * 重置密码
     */
    @PutMapping("/{id}/password")
    public Result<String> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> data) {
        boolean success = userService.resetPassword(id, data.get("newPassword"));
        return success ? Result.success("密码重置成功") : Result.error("密码重置失败");
    }
}

