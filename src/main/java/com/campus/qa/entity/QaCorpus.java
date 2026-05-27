package com.campus.qa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.qa.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问答语料库实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qa_corpus")
public class QaCorpus extends BaseEntity {
    
    /**
     * 语料ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 问题
     */
    private String question;
    
    /**
     * 答案
     */
    private String answer;
    
    /**
     * 问题分类 (FRESHMAN_GUIDE-新生指引, LEARNING-学习相关, 
     *           LIFE-生活相关, ACTIVITY-活动相关, NEWS-资讯相关,
     *           RULES-规章制度, OTHER-其他)
     */
    private String category;
    
    /**
     * 来源类型 (CONTENT-内容, ACTIVITY-活动, NEWS-资讯, MANUAL-手动)
     */
    @TableField("source_type")
    private String sourceType;
    
    /**
     * 来源ID
     */
    @TableField("source_id")
    private Long sourceId;
    
    /**
     * 关键词（多个关键词用逗号分隔）
     */
    private String keywords;
    
    /**
     * 意图标签（用于Rasa训练）
     */
    private String intent;
    
    /**
     * 置信度阈值
     */
    private Double confidenceThreshold;
    
    /**
     * 使用次数
     */
    private Integer usageCount;
    
    /**
     * 状态 (0-禁用 1-启用)
     */
    private Integer status;
}

