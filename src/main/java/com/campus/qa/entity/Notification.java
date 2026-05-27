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
 * 消息通知实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notification")
public class Notification extends BaseEntity {
    
    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 通知类型 (SYSTEM-系统通知, ACTIVITY-活动通知, NEWS-资讯通知)
     */
    private String type;
    
    /**
     * 接收用户ID (为空则为全体用户)
     */
    private Long receiverId;
    
    /**
     * 发送用户ID
     */
    private Long senderId;
    
    /**
     * 是否已读 (0-未读 1-已读)
     */
    private Integer isRead;
    
    /**
     * 优先级 (0-普通 1-重要 2-紧急)
     */
    private Integer priority;
    
    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
}


