package com.campus.qa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.entity.Feedback;

/**
 * 问题反馈服务接口
 */
public interface FeedbackService {
    
    /**
     * 提交反馈
     */
    boolean submitFeedback(Feedback feedback);
    
    /**
     * 分页查询反馈列表
     */
    Page<Feedback> getFeedbackPage(int current, int size, String status, String feedbackType);
    
    /**
     * 获取用户的反馈列表
     */
    Page<Feedback> getUserFeedbackPage(Long userId, int current, int size);
    
    /**
     * 获取反馈详情
     */
    Feedback getFeedbackById(Long id);
    
    /**
     * 处理反馈
     */
    boolean handleFeedback(Long id, Long handlerId, String handlerName, String status, String reply);
    
    /**
     * 删除反馈
     */
    boolean deleteFeedback(Long id);
    
    /**
     * 获取待处理反馈数量
     */
    long getPendingCount();
}


