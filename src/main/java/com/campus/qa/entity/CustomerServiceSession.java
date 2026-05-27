package com.campus.qa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客服会话实体类
 */
@Data
@TableName("customer_service_session")
public class CustomerServiceSession {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学生用户名
     */
    private String studentName;
    
    /**
     * 客服ID(管理员ID)
     */
    private Long serviceId;
    
    /**
     * 客服用户名
     */
    private String serviceName;
    
    /**
     * 会话状态(waiting-等待中 serving-服务中 closed-已关闭)
     */
    private String status;
    
    /**
     * 未读消息数
     */
    private Integer unreadCount;
    
    /**
     * 最后一条消息内容
     */
    private String lastMessage;
    
    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;
    
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


