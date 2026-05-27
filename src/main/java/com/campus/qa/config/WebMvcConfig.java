package com.campus.qa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir:uploads/avatars}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，使上传的文件可以通过URL访问
        File uploadDirectory = new File(uploadDir);
        String absolutePath = uploadDirectory.getAbsolutePath() + File.separator;
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath.replace("\\avatars\\", "\\"));
    }
}
