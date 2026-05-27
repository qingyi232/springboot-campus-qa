package com.campus.qa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问题反馈实体类
 */
@Data
@TableName("feedback")
public class Feedback {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 反馈用户ID
     */
    private Long userId;
    
    /**
     * 反馈用户名
     */
    private String username;
    
    /**
     * 反馈类型(功能问题/内容建议/其他)
     */
    private String feedbackType;
    
    /**
     * 反馈标题
     */
    private String title;
    
    /**
     * 反馈内容
     */
    private String content;
    
    /**
     * 联系方式(手机号或邮箱)
     */
    private String contact;
    
    /**
     * 处理状态(pending-待处理 processing-处理中 resolved-已解决 rejected-已驳回)
     */
    private String status;
    
    /**
     * 处理人ID
     */
    private Long handlerId;
    
    /**
     * 处理人用户名
     */
    private String handlerName;
    
    /**
     * 回复内容
     */
    private String reply;
    
    /**
     * 回复时间
     */
    private LocalDateTime replyTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}


