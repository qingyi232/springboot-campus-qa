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
 * 活动实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_management")
public class Activity extends BaseEntity {
    
    /**
     * 活动ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 活动标题
     */
    private String title;
    
    /**
     * 活动描述
     */
    private String description;
    
    /**
     * 活动类型 (LECTURE-讲座, COMPETITION-竞赛, CULTURAL-文化活动, SPORTS-体育活动)
     */
    private String type;
    
    /**
     * 活动地点
     */
    private String location;
    
    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /**
     * 报名截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDeadline;
    
    /**
     * 最大报名人数
     */
    private Integer maxParticipants;
    
    /**
     * 当前报名人数
     */
    private Integer currentParticipants;
    
    /**
     * 活动要求
     */
    private String requirements;
    
    /**
     * 活动封面图
     */
    private String coverImage;
    
    /**
     * 主办方
     */
    private String organizer;
    
    /**
     * 活动状态 (0-草稿 1-已发布 2-已结束)
     */
    private Integer status;
}

