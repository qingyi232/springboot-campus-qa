package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.entity.Feedback;
import com.campus.qa.mapper.FeedbackMapper;
import com.campus.qa.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 问题反馈服务实现类
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {
    
    @Autowired
    private FeedbackMapper feedbackMapper;
    
    @Override
    @Transactional
    public boolean submitFeedback(Feedback feedback) {
        feedback.setStatus("pending");
        feedback.setCreateTime(LocalDateTime.now());
        feedback.setUpdateTime(LocalDateTime.now());
        
        return feedbackMapper.insert(feedback) > 0;
    }
    
    @Override
    public Page<Feedback> getFeedbackPage(int current, int size, String status, String feedbackType) {
        Page<Feedback> page = new Page<>(current, size);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Feedback::getStatus, status);
        }
        
        if (StringUtils.hasText(feedbackType)) {
            wrapper.eq(Feedback::getFeedbackType, feedbackType);
        }
        
        wrapper.orderByDesc(Feedback::getCreateTime);
        
        return feedbackMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Page<Feedback> getUserFeedbackPage(Long userId, int current, int size) {
        Page<Feedback> page = new Page<>(current, size);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId)
               .orderByDesc(Feedback::getCreateTime);
        
        return feedbackMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Feedback getFeedbackById(Long id) {
        return feedbackMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public boolean handleFeedback(Long id, Long handlerId, String handlerName, String status, String reply) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            return false;
        }
        
        feedback.setHandlerId(handlerId);
        feedback.setHandlerName(handlerName);
        feedback.setStatus(status);
        feedback.setReply(reply);
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setUpdateTime(LocalDateTime.now());
        
        return feedbackMapper.updateById(feedback) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteFeedback(Long id) {
        return feedbackMapper.deleteById(id) > 0;
    }
    
    @Override
    public long getPendingCount() {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getStatus, "pending");
        
        return feedbackMapper.selectCount(wrapper);
    }
}


