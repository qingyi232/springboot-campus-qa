package com.campus.qa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.entity.ChatMessage;
import com.campus.qa.entity.CustomerServiceSession;

import java.util.List;

/**
 * 客服服务接口
 */
public interface CustomerServiceService {
    
    /**
     * 创建客服会话
     */
    CustomerServiceSession createSession(Long studentId, String studentName);
    
    /**
     * 获取或创建会话
     */
    CustomerServiceSession getOrCreateSession(Long studentId, String studentName);
    
    /**
     * 管理员接入会话
     */
    boolean acceptSession(Long sessionId, Long serviceId, String serviceName);
    
    /**
     * 关闭会话
     */
    boolean closeSession(Long sessionId);
    
    /**
     * 发送消息
     */
    ChatMessage sendMessage(ChatMessage message);
    
    /**
     * 获取会话消息历史
     */
    List<ChatMessage> getSessionMessages(Long sessionId);
    
    /**
     * 标记消息已读
     */
    boolean markMessagesAsRead(Long sessionId, Long userId);
    
    /**
     * 获取用户的会话
     */
    CustomerServiceSession getUserSession(Long userId);
    
    /**
     * 获取等待中的会话列表
     */
    Page<CustomerServiceSession> getWaitingSessions(int current, int size);
    
    /**
     * 获取服务中的会话列表
     */
    Page<CustomerServiceSession> getServingSessions(int current, int size);
    
    /**
     * 获取管理员的会话列表
     */
    Page<CustomerServiceSession> getAdminSessions(Long serviceId, int current, int size);
    
    /**
     * 获取会话详情
     */
    CustomerServiceSession getSessionById(Long sessionId);
    
    /**
     * 获取未读消息数
     */
    int getUnreadCount(Long sessionId, Long userId);
}


