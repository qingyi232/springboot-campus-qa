package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.entity.ChatMessage;
import com.campus.qa.entity.CustomerServiceSession;
import com.campus.qa.mapper.ChatMessageMapper;
import com.campus.qa.mapper.CustomerServiceSessionMapper;
import com.campus.qa.service.CustomerServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客服服务实现类
 */
@Service
public class CustomerServiceServiceImpl implements CustomerServiceService {
    
    @Autowired
    private CustomerServiceSessionMapper sessionMapper;
    
    @Autowired
    private ChatMessageMapper messageMapper;
    
    @Override
    @Transactional
    public CustomerServiceSession createSession(Long studentId, String studentName) {
        CustomerServiceSession session = new CustomerServiceSession();
        session.setStudentId(studentId);
        session.setStudentName(studentName);
        session.setStatus("waiting");
        session.setUnreadCount(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        
        sessionMapper.insert(session);
        return session;
    }
    
    @Override
    public CustomerServiceSession getOrCreateSession(Long studentId, String studentName) {
        // 先查找是否有未关闭的会话
        LambdaQueryWrapper<CustomerServiceSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceSession::getStudentId, studentId)
               .in(CustomerServiceSession::getStatus, "waiting", "serving")
               .orderByDesc(CustomerServiceSession::getCreateTime)
               .last("LIMIT 1");
        
        CustomerServiceSession session = sessionMapper.selectOne(wrapper);
        
        if (session == null) {
            session = createSession(studentId, studentName);
        }
        
        return session;
    }
    
    @Override
    @Transactional
    public boolean acceptSession(Long sessionId, Long serviceId, String serviceName) {
        CustomerServiceSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            return false;
        }
        
        session.setServiceId(serviceId);
        session.setServiceName(serviceName);
        session.setStatus("serving");
        session.setUpdateTime(LocalDateTime.now());
        
        return sessionMapper.updateById(session) > 0;
    }
    
    @Override
    @Transactional
    public boolean closeSession(Long sessionId) {
        CustomerServiceSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            return false;
        }
        
        session.setStatus("closed");
        session.setUpdateTime(LocalDateTime.now());
        
        return sessionMapper.updateById(session) > 0;
    }
    
    @Override
    @Transactional
    public ChatMessage sendMessage(ChatMessage message) {
        message.setCreateTime(LocalDateTime.now());
        message.setIsRead(0);
        messageMapper.insert(message);
        
        // 更新会话的最后消息
        CustomerServiceSession session = sessionMapper.selectById(message.getSessionId());
        if (session != null) {
            session.setLastMessage(message.getContent());
            session.setLastMessageTime(LocalDateTime.now());
            
            // 如果是学生发送的消息，增加未读数
            if ("student".equals(message.getSenderRole())) {
                session.setUnreadCount(session.getUnreadCount() + 1);
            }
            
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
        
        return message;
    }
    
    @Override
    public List<ChatMessage> getSessionMessages(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .orderByAsc(ChatMessage::getCreateTime);
        
        return messageMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional
    public boolean markMessagesAsRead(Long sessionId, Long userId) {
        // 标记对方发送的消息为已读
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .eq(ChatMessage::getReceiverId, userId)
               .eq(ChatMessage::getIsRead, 0);
        
        ChatMessage update = new ChatMessage();
        update.setIsRead(1);
        
        messageMapper.update(update, wrapper);
        
        // 清空会话未读数
        CustomerServiceSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setUnreadCount(0);
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
        
        return true;
    }
    
    @Override
    public CustomerServiceSession getUserSession(Long userId) {
        LambdaQueryWrapper<CustomerServiceSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceSession::getStudentId, userId)
               .in(CustomerServiceSession::getStatus, "waiting", "serving")
               .orderByDesc(CustomerServiceSession::getCreateTime)
               .last("LIMIT 1");
        
        return sessionMapper.selectOne(wrapper);
    }
    
    @Override
    public Page<CustomerServiceSession> getWaitingSessions(int current, int size) {
        Page<CustomerServiceSession> page = new Page<>(current, size);
        LambdaQueryWrapper<CustomerServiceSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceSession::getStatus, "waiting")
               .orderByDesc(CustomerServiceSession::getCreateTime);
        
        return sessionMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Page<CustomerServiceSession> getServingSessions(int current, int size) {
        Page<CustomerServiceSession> page = new Page<>(current, size);
        LambdaQueryWrapper<CustomerServiceSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceSession::getStatus, "serving")
               .orderByDesc(CustomerServiceSession::getLastMessageTime);
        
        return sessionMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Page<CustomerServiceSession> getAdminSessions(Long serviceId, int current, int size) {
        Page<CustomerServiceSession> page = new Page<>(current, size);
        LambdaQueryWrapper<CustomerServiceSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceSession::getServiceId, serviceId)
               .in(CustomerServiceSession::getStatus, "serving", "closed")
               .orderByDesc(CustomerServiceSession::getLastMessageTime);
        
        return sessionMapper.selectPage(page, wrapper);
    }
    
    @Override
    public CustomerServiceSession getSessionById(Long sessionId) {
        return sessionMapper.selectById(sessionId);
    }
    
    @Override
    public int getUnreadCount(Long sessionId, Long userId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .eq(ChatMessage::getReceiverId, userId)
               .eq(ChatMessage::getIsRead, 0);
        
        return messageMapper.selectCount(wrapper).intValue();
    }
}


