package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.qa.entity.QaConversation;
import com.campus.qa.mapper.QaConversationMapper;
import com.campus.qa.service.QaConversationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 问答对话记录服务实现类
 */
@Service
public class QaConversationServiceImpl extends ServiceImpl<QaConversationMapper, QaConversation> 
        implements QaConversationService {
    
    @Override
    public IPage<QaConversation> pageConversations(Page<QaConversation> page, Long userId, String sessionId) {
        LambdaQueryWrapper<QaConversation> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(QaConversation::getUserId, userId);
        }
        
        if (StringUtils.hasText(sessionId)) {
            wrapper.eq(QaConversation::getSessionId, sessionId);
        }
        
        wrapper.orderByDesc(QaConversation::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public boolean feedback(Long id, Integer satisfaction, String feedback) {
        QaConversation conversation = getById(id);
        if (conversation == null) {
            throw new RuntimeException("对话记录不存在");
        }
        
        conversation.setSatisfaction(satisfaction);
        conversation.setFeedback(feedback);
        
        return updateById(conversation);
    }
}


