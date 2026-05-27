package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.qa.entity.Content;
import com.campus.qa.mapper.ContentMapper;
import com.campus.qa.service.ContentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 内容管理服务实现类
 */
@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> 
        implements ContentService {
    
    @Override
    public boolean publishContent(Content content) {
        if (content.getStatus() == null) {
            content.setStatus(1); // 已发布
        }
        
        if (content.getViewCount() == null) {
            content.setViewCount(0);
        }
        
        // 移除sort字段相关逻辑
        
        return save(content);
    }
    
    @Override
    public IPage<Content> pageContents(Page<Content> page, String keyword, String category, Integer status) {
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Content::getTitle, keyword)
                    .or().like(Content::getSummary, keyword)
                    .or().like(Content::getContent, keyword);  // 新增：搜索正文内容
        }
        
        if (StringUtils.hasText(category)) {
            wrapper.eq(Content::getCategory, category);
        }
        
        if (status != null) {
            wrapper.eq(Content::getStatus, status);
        }
        
        wrapper.orderByDesc(Content::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public List<Content> getContentsByCategory(String category) {
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Content::getCategory, category);
        wrapper.eq(Content::getStatus, 1); // 只查询已发布的
        wrapper.orderByDesc(Content::getCreateTime);
        
        return list(wrapper);
    }
    
    @Override
    public boolean updateContentStatus(Long id, Integer status) {
        Content content = getById(id);
        if (content == null) {
            throw new RuntimeException("内容不存在");
        }
        
        content.setStatus(status);
        return updateById(content);
    }
    
    @Override
    public boolean increaseViewCount(Long id) {
        Content content = getById(id);
        if (content == null) {
            return false;
        }
        
        content.setViewCount(content.getViewCount() + 1);
        return updateById(content);
    }
}

