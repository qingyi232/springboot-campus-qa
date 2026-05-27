package com.campus.qa.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.qa.entity.News;

import java.util.List;

/**
 * 校园资讯服务接口
 */
public interface NewsService extends IService<News> {
    
    /**
     * 发布资讯
     */
    boolean publishNews(News news);
    
    /**
     * 分页查询资讯
     */
    IPage<News> pageNews(Page<News> page, String keyword, String category, Integer status);
    
    /**
     * 获取置顶资讯
     */
    List<News> getTopNews();
    
    /**
     * 设置置顶
     */
    boolean setTop(Long id, Integer isTop);
    
    /**
     * 更新资讯状态
     */
    boolean updateNewsStatus(Long id, Integer status);
    
    /**
     * 增加浏览次数
     */
    boolean increaseViewCount(Long id);
    
    /**
     * 点赞
     */
    boolean like(Long id);
}


