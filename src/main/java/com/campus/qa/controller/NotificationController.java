package com.campus.qa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.common.Result;
import com.campus.qa.entity.Notification;
import com.campus.qa.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/notification")
@CrossOrigin
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 分页查询通知列表
     */
    @GetMapping("/page")
    public Result<IPage<Notification>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer isRead) {
        Page<Notification> page = new Page<>(current, size);
        IPage<Notification> result = notificationService.pageNotifications(page, userId, isRead);
        return Result.success(result);
    }
    
    /**
     * 获取未读通知列表
     */
    @GetMapping("/unread/{userId}")
    public Result<List<Notification>> getUnread(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return Result.success(notifications);
    }
    
    /**
     * 发送通知
     */
    @PostMapping
    public Result<String> send(@RequestBody Notification notification) {
        boolean success = notificationService.sendNotification(notification);
        return success ? Result.success("发送成功") : Result.error("发送失败");
    }
    
    /**
     * 标记为已读
     */
    @PutMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id) {
        boolean success = notificationService.markAsRead(id);
        return success ? Result.success("标记成功") : Result.error("标记失败");
    }
    
    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = notificationService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}


