package com.campus.qa.controller;

import com.campus.qa.common.Result;
import com.campus.qa.entity.QaCorpus;
import com.campus.qa.service.QaCorpusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 语料库控制器
 * 用于 Rasa Action Server 直接查询语料库（避免死循环）
 */
@RestController
@RequestMapping("/api/qa-corpus")
public class QaCorpusController {

    @Autowired
    private QaCorpusService corpusService;

    /**
     * 搜索语料库（供 Rasa Action Server 调用）
     * 注意：此API不会再调用Rasa，避免死循环
     */
    @GetMapping("/search")
    public Result<QaCorpus> search(@RequestParam String question) {
        try {
            // 查找最佳匹配
            QaCorpus corpus = corpusService.findBestMatch(question);
            
            if (corpus != null) {
                // 增加浏览次数
                corpusService.increaseUsageCount(corpus.getId());
                return Result.success(corpus);
            } else {
                return Result.error("未找到匹配的答案");
            }
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}


