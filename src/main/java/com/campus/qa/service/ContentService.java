package com.campus.qa.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.qa.entity.Content;

import java.util.List;

/**
 * 内容管理服务接口
 */
public interface ContentService extends IService<Content> {
    
    /**
     * 发布内容
     */
    boolean publishContent(Content content);
    
    /**
     * 分页查询内容
     */
    IPage<Content> pageContents(Page<Content> page, String keyword, String category, Integer status);
    
    /**
     * 根据分类获取内容列表
     */
    List<Content> getContentsByCategory(String category);
    
    /**
     * 更新内容状态
     */
    boolean updateContentStatus(Long id, Integer status);
    
    /**
     * 增加浏览次数
     */
    boolean increaseViewCount(Long id);
}


