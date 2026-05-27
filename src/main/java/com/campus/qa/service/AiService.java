package com.campus.qa.service;

import java.util.List;

/**
 * AI大模型服务接口
 */
public interface AiService {
    
    /**
     * 发送问题到AI大模型
     * @param question 用户问题
     * @param context 上下文信息（可选）
     * @return AI回答
     */
    String chat(String question, String context);
    
    /**
     * 带历史对话的聊天
     * @param question 用户问题
     * @param history 历史对话
     * @return AI回答
     */
    String chatWithHistory(String question, List<String> history);
    
    /**
     * 检查AI服务是否可用
     * @return 是否可用
     */
    boolean isAvailable();
}


