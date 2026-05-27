package com.campus.qa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.qa.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容管理实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_management")
public class Content extends BaseEntity {
    
    /**
     * 内容ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 内容标题
     */
    private String title;
    
    /**
     * 内容分类 (FRESHMAN_GUIDE-新生报到指引, LEARNING_RESOURCE-在线学习资源, 
     *           CAMPUS_LIFE-校园生活百科, SCHOOL_RULES-校规校纪)
     */
    private String category;
    
    /**
     * 内容摘要
     */
    private String summary;
    
    /**
     * 内容详情
     */
    private String content;
    
    /**
     * 封面图片
     */
    private String coverImage;
    
    /**
     * 标签（多个标签用逗号分隔）
     */
    private String tags;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 状态 (0-草稿 1-已发布)
     */
    private Integer status;
}

