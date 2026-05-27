package com.campus.qa.controller;

import com.campus.qa.entity.QaCorpus;
import com.campus.qa.service.AiService;
import com.campus.qa.service.QaCorpusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 诊断控制器 - 用于快速检测系统状态
 */
@RestController
@RequestMapping("/diagnostic")
public class DiagnosticController {
    
    @Autowired
    private QaCorpusService corpusService;
    
    @Autowired
    private AiService aiService;
    
    @Value("${rasa.api-url}")
    private String rasaApiUrl;
    
    @GetMapping("/status")
    public Map<String, Object> checkStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 检查语料库
            List<QaCorpus> corpus = corpusService.list();
            result.put("corpusCount", corpus != null ? corpus.size() : 0);
            result.put("corpusOk", corpus != null && !corpus.isEmpty());
            
            // 2. 检查DeepSeek
            boolean deepseekOk = false;
            String deepseekMessage = "";
            try {
                boolean available = aiService.isAvailable();
                deepseekOk = available;
                deepseekMessage = available ? "DeepSeek服务正常" : "DeepSeek服务不可用";
            } catch (Exception e) {
                deepseekMessage = "DeepSeek检测异常: " + e.getMessage();
            }
            result.put("deepseekOk", deepseekOk);
            result.put("deepseekMessage", deepseekMessage);
            
            // 3. 检查Rasa配置
            result.put("rasaUrl", rasaApiUrl);
            result.put("rasaMessage", "Rasa配置: " + rasaApiUrl + " (需手动启动)");
            
            // 4. 快速测试
            String testAnswer = "";
            try {
                testAnswer = aiService.chat("你是谁？", "");
                result.put("testAnswerOk", true);
                result.put("testAnswer", testAnswer);
            } catch (Exception e) {
                result.put("testAnswerOk", false);
                result.put("testError", e.getMessage());
            }
            
            result.put("success", true);
            result.put("message", "诊断完成");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}


