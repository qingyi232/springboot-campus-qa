package com.campus.qa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.common.Result;
import com.campus.qa.entity.QaConversation;
import com.campus.qa.entity.QaCorpus;
import com.campus.qa.service.QaConversationService;
import com.campus.qa.service.QaCorpusService;
import com.campus.qa.service.QaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 智能问答控制器
 */
@RestController
@RequestMapping("/qa")
@CrossOrigin
public class QaController {
    
    @Autowired
    private QaService qaService;
    
    @Autowired
    private QaCorpusService corpusService;
    
    @Autowired
    private QaConversationService conversationService;
    
    @Autowired
    private com.campus.qa.service.RasaTrainingService rasaTrainingService;
    
    /**
     * 发送问题
     */
    @PostMapping("/ask")
    public Result<Map<String, Object>> ask(@RequestBody Map<String, Object> data) {
        String question = (String) data.get("question");
        String sessionId = (String) data.get("sessionId");
        Long userId = data.get("userId") != null ? Long.valueOf(data.get("userId").toString()) : null;
        
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        Map<String, Object> result = qaService.sendQuestion(question, sessionId, userId);
        return Result.success(result);
    }
    
    /**
     * 分页查询对话记录
     */
    @GetMapping("/conversation/page")
    public Result<IPage<QaConversation>> pageConversations(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId) {
        Page<QaConversation> page = new Page<>(current, size);
        IPage<QaConversation> result = conversationService.pageConversations(page, userId, sessionId);
        return Result.success(result);
    }
    
    /**
     * 用户反馈
     */
    @PutMapping("/conversation/{id}/feedback")
    public Result<String> feedback(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Integer satisfaction = (Integer) data.get("satisfaction");
        String feedback = (String) data.get("feedback");
        boolean success = conversationService.feedback(id, satisfaction, feedback);
        return success ? Result.success("反馈成功") : Result.error("反馈失败");
    }
    
    /**
     * 分页查询语料库
     */
    @GetMapping("/corpus/page")
    public Result<IPage<QaCorpus>> pageCorpus(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        Page<QaCorpus> page = new Page<>(current, size);
        IPage<QaCorpus> result = corpusService.pageCorpus(page, keyword, category);
        return Result.success(result);
    }
    
    /**
     * 添加语料
     */
    @PostMapping("/corpus")
    public Result<String> addCorpus(@RequestBody QaCorpus corpus) {
        boolean success = corpusService.addCorpus(corpus);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }
    
    /**
     * 更新语料
     */
    @PutMapping("/corpus/{id}")
    public Result<String> updateCorpus(@PathVariable Long id, @RequestBody QaCorpus corpus) {
        corpus.setId(id);
        boolean success = corpusService.updateById(corpus);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    /**
     * 删除语料
     */
    @DeleteMapping("/corpus/{id}")
    public Result<String> deleteCorpus(@PathVariable Long id) {
        boolean success = corpusService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 从内容生成语料
     */
    @PostMapping("/corpus/generate")
    public Result<String> generateCorpus(@RequestBody Map<String, Object> data) {
        Long contentId = Long.valueOf(data.get("contentId").toString());
        String type = (String) data.get("type");
        boolean success = corpusService.generateCorpusFromContent(contentId, type);
        return success ? Result.success("生成成功") : Result.error("生成失败");
    }
    
    /**
     * 训练模型
     */
    @PostMapping("/train")
    public Result<String> trainModel() {
        boolean success = qaService.trainModel();
        return success ? Result.success("训练成功") : Result.error("训练失败");
    }
    
    /**
     * 同步语料库到Rasa（生成训练数据）
     */
    @PostMapping("/sync")
    public Result<String> syncToRasa() {
        try {
            boolean success = rasaTrainingService.generateRasaNluData();
            return success ? Result.success("训练数据生成成功，请执行模型训练") : Result.error("生成失败");
        } catch (Exception e) {
            return Result.error("同步失败: " + e.getMessage());
        }
    }
    
    /**
     * 触发Rasa模型训练
     */
    @PostMapping("/train-model")
    public Result<String> trainRasaModel() {
        try {
            // 先生成训练数据
            boolean generateSuccess = rasaTrainingService.generateRasaNluData();
            if (!generateSuccess) {
                return Result.error("训练数据生成失败");
            }
            
            // 触发训练
            boolean trainSuccess = rasaTrainingService.triggerRasaTraining();
            return trainSuccess ? Result.success("模型训练已启动，请稍后查看训练结果") : Result.error("训练启动失败，请检查Rasa服务是否运行");
        } catch (Exception e) {
            return Result.error("训练失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查Rasa服务状态
     */
    @GetMapping("/rasa-status")
    public Result<Map<String, Object>> checkRasaStatus() {
        Map<String, Object> status = new HashMap<>();
        boolean isRunning = rasaTrainingService.checkRasaStatus();
        status.put("isRunning", isRunning);
        status.put("message", isRunning ? "Rasa服务运行正常" : "Rasa服务未运行，请先启动Rasa");
        return Result.success(status);
    }
}

