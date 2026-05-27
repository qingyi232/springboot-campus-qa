package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.qa.entity.Activity;
import com.campus.qa.entity.Content;
import com.campus.qa.entity.News;
import com.campus.qa.entity.QaCorpus;
import com.campus.qa.mapper.QaCorpusMapper;
import com.campus.qa.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 问答语料库服务实现类
 */
@Service
public class QaCorpusServiceImpl extends ServiceImpl<QaCorpusMapper, QaCorpus> 
        implements QaCorpusService {
    
    @Autowired
    private ContentService contentService;
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private NewsService newsService;
    
    @Override
    public boolean addCorpus(QaCorpus corpus) {
        if (corpus.getStatus() == null) {
            corpus.setStatus(1); // 启用
        }
        
        if (corpus.getUsageCount() == null) {
            corpus.setUsageCount(0);
        }
        
        if (corpus.getConfidenceThreshold() == null) {
            corpus.setConfidenceThreshold(0.7);
        }
        
        return save(corpus);
    }
    
    @Override
    public IPage<QaCorpus> pageCorpus(Page<QaCorpus> page, String keyword, String category) {
        LambdaQueryWrapper<QaCorpus> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(QaCorpus::getQuestion, keyword)
                    .or().like(QaCorpus::getAnswer, keyword)
                    .or().like(QaCorpus::getKeywords, keyword);
        }
        
        if (StringUtils.hasText(category)) {
            wrapper.eq(QaCorpus::getCategory, category);
        }
        
        wrapper.orderByDesc(QaCorpus::getUsageCount)
                .orderByDesc(QaCorpus::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public QaCorpus findBestMatch(String question) {
        // 简单的关键词匹配算法
        // 实际项目中可以使用更复杂的NLP算法或向量相似度计算
        
        LambdaQueryWrapper<QaCorpus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QaCorpus::getStatus, 1); // 只查询启用的语料
        
        List<QaCorpus> allCorpus = list(wrapper);
        
        QaCorpus bestMatch = null;
        double bestScore = 0.0;
        
        for (QaCorpus corpus : allCorpus) {
            double score = calculateSimilarity(question, corpus.getQuestion(), corpus.getKeywords());
            
            if (score > bestScore && score >= (corpus.getConfidenceThreshold() != null ? corpus.getConfidenceThreshold() : 0.7)) {
                bestScore = score;
                bestMatch = corpus;
            }
        }
        
        return bestMatch;
    }
    
    /**
     * 计算相似度
     */
    private double calculateSimilarity(String question, String corpusQuestion, String keywords) {
        question = question.toLowerCase();
        corpusQuestion = corpusQuestion.toLowerCase();
        
        // 完全匹配
        if (question.equals(corpusQuestion)) {
            return 1.0;
        }
        
        // 包含匹配
        if (question.contains(corpusQuestion) || corpusQuestion.contains(question)) {
            return 0.8;
        }
        
        // 关键词匹配
        if (StringUtils.hasText(keywords)) {
            String[] keywordArray = keywords.split(",");
            int matchCount = 0;
            for (String keyword : keywordArray) {
                if (question.contains(keyword.trim().toLowerCase())) {
                    matchCount++;
                }
            }
            if (matchCount > 0) {
                return 0.6 + (0.2 * matchCount / keywordArray.length);
            }
        }
        
        return 0.0;
    }
    
    @Override
    public boolean increaseUsageCount(Long id) {
        QaCorpus corpus = getById(id);
        if (corpus == null) {
            return false;
        }
        
        corpus.setUsageCount(corpus.getUsageCount() + 1);
        return updateById(corpus);
    }
    
    @Override
    public List<QaCorpus> findRelated(String question, int limit) {
        // 查找相关语料，用于为AI提供上下文
        LambdaQueryWrapper<QaCorpus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QaCorpus::getStatus, 1); // 只查询启用的语料
        wrapper.orderByDesc(QaCorpus::getUsageCount); // 按使用次数排序
        
        List<QaCorpus> allCorpus = list(wrapper);
        
        // 计算相似度并排序
        List<QaCorpus> relatedList = new java.util.ArrayList<>();
        for (QaCorpus corpus : allCorpus) {
            double score = calculateSimilarity(question, corpus.getQuestion(), corpus.getKeywords());
            if (score > 0.3) { // 设置较低的阈值
                relatedList.add(corpus);
            }
            
            if (relatedList.size() >= limit) {
                break;
            }
        }
        
        return relatedList;
    }
    
    @Override
    public boolean generateCorpusFromContent(Long contentId, String type) {
        QaCorpus corpus = new QaCorpus();
        corpus.setSourceId(contentId);
        corpus.setSourceType(type);
        
        switch (type) {
            case "CONTENT":
                Content content = contentService.getById(contentId);
                if (content != null) {
                    corpus.setQuestion("请问" + content.getTitle() + "?");
                    corpus.setAnswer(content.getSummary());
                    corpus.setCategory(mapContentCategory(content.getCategory()));
                    corpus.setKeywords(content.getTags());
                }
                break;
                
            case "ACTIVITY":
                Activity activity = activityService.getById(contentId);
                if (activity != null) {
                    corpus.setQuestion("有什么" + activity.getTitle() + "吗?");
                    corpus.setAnswer(activity.getDescription());
                    corpus.setCategory("ACTIVITY");
                    corpus.setKeywords(activity.getType());
                }
                break;
                
            case "NEWS":
                News news = newsService.getById(contentId);
                if (news != null) {
                    corpus.setQuestion(news.getTitle() + "是什么?");
                    corpus.setAnswer(news.getSummary());
                    corpus.setCategory("NEWS");
                    // tags字段不存在，移除此逻辑
                    corpus.setKeywords(news.getCategory());
                }
                break;
                
            default:
                return false;
        }
        
        return addCorpus(corpus);
    }
    
    /**
     * 映射内容分类到语料分类
     */
    private String mapContentCategory(String contentCategory) {
        switch (contentCategory) {
            case "FRESHMAN_GUIDE":
                return "FRESHMAN_GUIDE";
            case "LEARNING_RESOURCE":
                return "LEARNING";
            case "CAMPUS_LIFE":
                return "LIFE";
            case "SCHOOL_RULES":
                return "RULES";
            default:
                return "OTHER";
        }
    }
}

