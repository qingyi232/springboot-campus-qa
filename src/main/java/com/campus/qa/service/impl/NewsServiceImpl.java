package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.qa.entity.News;
import com.campus.qa.mapper.NewsMapper;
import com.campus.qa.service.NewsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 校园资讯服务实现类
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> 
        implements NewsService {
    
    @Override
    public boolean publishNews(News news) {
        if (news.getStatus() == null) {
            news.setStatus(1); // 已发布
        }
        
        if (news.getPublishTime() == null) {
            news.setPublishTime(LocalDateTime.now());
        }
        
        if (news.getIsTop() == null) {
            news.setIsTop(0);
        }
        
        if (news.getViewCount() == null) {
            news.setViewCount(0);
        }
        
        if (news.getLikeCount() == null) {
            news.setLikeCount(0);
        }
        
        return save(news);
    }
    
    @Override
    public IPage<News> pageNews(Page<News> page, String keyword, String category, Integer status) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(News::getTitle, keyword)
                    .or().like(News::getSummary, keyword)
                    .or().like(News::getContent, keyword);  // 新增：搜索正文内容
        }
        
        if (StringUtils.hasText(category)) {
            wrapper.eq(News::getCategory, category);
        }
        
        if (status != null) {
            wrapper.eq(News::getStatus, status);
        }
        
        wrapper.orderByDesc(News::getIsTop)
                .orderByDesc(News::getPublishTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public List<News> getTopNews() {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getIsTop, 1);
        wrapper.eq(News::getStatus, 1);
        wrapper.orderByDesc(News::getPublishTime);
        wrapper.last("LIMIT 5"); // 最多返回5条置顶资讯
        
        return list(wrapper);
    }
    
    @Override
    public boolean setTop(Long id, Integer isTop) {
        News news = getById(id);
        if (news == null) {
            throw new RuntimeException("资讯不存在");
        }
        
        news.setIsTop(isTop);
        return updateById(news);
    }
    
    @Override
    public boolean updateNewsStatus(Long id, Integer status) {
        News news = getById(id);
        if (news == null) {
            throw new RuntimeException("资讯不存在");
        }
        
        news.setStatus(status);
        return updateById(news);
    }
    
    @Override
    public boolean increaseViewCount(Long id) {
        News news = getById(id);
        if (news == null) {
            return false;
        }
        
        news.setViewCount(news.getViewCount() + 1);
        return updateById(news);
    }
    
    @Override
    public boolean like(Long id) {
        News news = getById(id);
        if (news == null) {
            return false;
        }
        
        news.setLikeCount(news.getLikeCount() + 1);
        return updateById(news);
    }
}

