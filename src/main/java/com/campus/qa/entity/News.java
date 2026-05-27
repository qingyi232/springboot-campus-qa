package com.campus.qa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.qa.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 校园资讯实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("news_management")
public class News extends BaseEntity {
    
    /**
     * 资讯ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 资讯标题
     */
    private String title;
    
    /**
     * 资讯分类 (SCHOOL_NEWS-学校新闻, NOTICE-通知公告, 
     *           ACADEMIC-学术动态, HOT_TOPIC-热点话题)
     */
    private String category;
    
    /**
     * 资讯摘要
     */
    private String summary;
    
    /**
     * 资讯内容
     */
    private String content;
    
    /**
     * 封面图片
     */
    private String coverImage;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
    
    /**
     * 是否置顶 (0-否 1-是)
     */
    private Integer isTop;
    
    /**
     * 状态 (0-草稿 1-已发布)
     */
    private Integer status;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 点赞次数
     */
    private Integer likeCount;
}

