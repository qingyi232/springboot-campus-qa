package com.campus.qa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 基于SpringBoot的校园智能问答系统 - 主启动类
 * 
 * @author Campus QA System
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.campus.qa.mapper")
public class CampusQaSystemApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CampusQaSystemApplication.class, args);
        System.out.println("\n==========================================");
        System.out.println("校园智能问答系统启动成功！");
        System.out.println("访问地址: http://localhost:8080/api");
        System.out.println("==========================================\n");
    }
}


