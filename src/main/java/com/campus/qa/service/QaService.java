package com.campus.qa.service;

import com.campus.qa.entity.QaConversation;

import java.util.Map;

/**
 * 智能问答服务接口
 */
public interface QaService {
    
    /**
     * 发送问题到Rasa并获取回答
     */
    Map<String, Object> sendQuestion(String question, String sessionId, Long userId);
    
    /**
     * 记录对话
     */
    void recordConversation(QaConversation conversation);
    
    /**
     * 训练Rasa模型
     */
    boolean trainModel();
    
    /**
     * 同步语料库到Rasa训练数据
     */
    boolean syncCorpusToRasa();
}


