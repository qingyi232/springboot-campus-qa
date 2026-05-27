package com.campus.qa.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.qa.entity.Notification;

import java.util.List;

/**
 * 消息通知服务接口
 */
public interface NotificationService extends IService<Notification> {
    
    /**
     * 发送通知
     */
    boolean sendNotification(Notification notification);
    
    /**
     * 发送系统通知（全体用户）
     */
    boolean sendSystemNotification(String title, String content);
    
    /**
     * 获取用户未读通知列表
     */
    List<Notification> getUnreadNotifications(Long userId);
    
    /**
     * 标记通知为已读
     */
    boolean markAsRead(Long id);
    
    /**
     * 分页查询通知
     */
    IPage<Notification> pageNotifications(Page<Notification> page, Long userId, Integer isRead);
}


