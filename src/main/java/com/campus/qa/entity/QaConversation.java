package com.campus.qa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.qa.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问答对话记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qa_conversation")
public class QaConversation extends BaseEntity {
    
    /**
     * 对话ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户提问
     */
    private String userQuestion;
    
    /**
     * 机器人回答
     */
    private String botAnswer;
    
    /**
     * 识别意图
     */
    private String intent;
    
    /**
     * 置信度
     */
    private Double confidence;
    
    /**
     * 匹配的语料ID
     */
    private Long corpusId;
    
    /**
     * 用户满意度 (1-5星)
     */
    private Integer satisfaction;
    
    /**
     * 用户反馈
     */
    private String feedback;
    
    /**
     * IP地址
     */
    private String ip;
}


