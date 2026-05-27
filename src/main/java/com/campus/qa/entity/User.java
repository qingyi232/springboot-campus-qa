package com.campus.qa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.qa.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 学号/工号
     */
    private String studentId;
    
    /**
     * 角色 (STUDENT-学生, ADMIN-管理员)
     */
    private String role;
    
    /**
     * 性别 (0-女 1-男)
     */
    private Integer gender;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 学院/部门
     */
    private String department;
    
    /**
     * 专业/职位
     */
    private String major;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 状态 (0-禁用 1-启用)
     */
    private Integer status;
}

