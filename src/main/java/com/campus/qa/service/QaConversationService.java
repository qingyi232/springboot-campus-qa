package com.campus.qa.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.qa.entity.QaConversation;

/**
 * 问答对话记录服务接口
 */
public interface QaConversationService extends IService<QaConversation> {
    
    /**
     * 分页查询对话记录
     */
    IPage<QaConversation> pageConversations(Page<QaConversation> page, Long userId, String sessionId);
    
    /**
     * 用户反馈
     */
    boolean feedback(Long id, Integer satisfaction, String feedback);
}


