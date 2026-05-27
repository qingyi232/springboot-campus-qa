package com.campus.qa.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.qa.entity.QaCorpus;

import java.util.List;

/**
 * 问答语料库服务接口
 */
public interface QaCorpusService extends IService<QaCorpus> {
    
    /**
     * 添加语料
     */
    boolean addCorpus(QaCorpus corpus);
    
    /**
     * 分页查询语料
     */
    IPage<QaCorpus> pageCorpus(Page<QaCorpus> page, String keyword, String category);
    
    /**
     * 查找最佳匹配
     */
    QaCorpus findBestMatch(String question);
    
    /**
     * 增加使用次数
     */
    boolean increaseUsageCount(Long id);
    
    /**
     * 查找相关语料（用于AI上下文构建）
     */
    List<QaCorpus> findRelated(String question, int limit);
    
    /**
     * 从内容/活动/资讯自动生成语料
     */
    boolean generateCorpusFromContent(Long contentId, String type);
}

