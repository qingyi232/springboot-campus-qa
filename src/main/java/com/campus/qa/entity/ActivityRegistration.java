package com.campus.qa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.qa.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动报名实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_registration")
public class ActivityRegistration extends BaseEntity {
    
    /**
     * 报名ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 活动ID
     */
    private Long activityId;
    
    /**
     * 活动名称
     */
    @TableField(exist = false)
    private String activityName;

    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 学号/工号
     */
    private String studentId;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 状态 (0-待审核 1-已通过 2-已拒绝)
     */
    private Integer status;
}

