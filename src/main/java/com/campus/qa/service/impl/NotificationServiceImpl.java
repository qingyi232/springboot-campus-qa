package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.qa.entity.Notification;
import com.campus.qa.mapper.NotificationMapper;
import com.campus.qa.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知服务实现类
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> 
        implements NotificationService {
    
    @Override
    public boolean sendNotification(Notification notification) {
        if (notification.getIsRead() == null) {
            notification.setIsRead(0); // 默认未读
        }
        
        if (notification.getPriority() == null) {
            notification.setPriority(0); // 默认普通优先级
        }
        
        return save(notification);
    }
    
    @Override
    public boolean sendSystemNotification(String title, String content) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("SYSTEM");
        notification.setIsRead(0);
        notification.setPriority(1); // 重要
        
        return save(notification);
    }
    
    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getIsRead, 0);
        wrapper.and(w -> w.eq(Notification::getReceiverId, userId)
                .or().isNull(Notification::getReceiverId)); // 包含全体通知
        wrapper.orderByDesc(Notification::getPriority)
                .orderByDesc(Notification::getCreateTime);
        
        return list(wrapper);
    }
    
    @Override
    public boolean markAsRead(Long id) {
        Notification notification = getById(id);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }
        
        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        
        return updateById(notification);
    }
    
    @Override
    public IPage<Notification> pageNotifications(Page<Notification> page, Long userId, Integer isRead) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.and(w -> w.eq(Notification::getReceiverId, userId)
                    .or().isNull(Notification::getReceiverId));
        }
        
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        
        wrapper.orderByDesc(Notification::getPriority)
                .orderByDesc(Notification::getCreateTime);
        
        return page(page, wrapper);
    }
}


