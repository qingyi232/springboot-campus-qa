package com.campus.qa.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.campus.qa.entity.QaConversation;
import com.campus.qa.entity.QaCorpus;
import com.campus.qa.service.AiService;
import com.campus.qa.service.QaConversationService;
import com.campus.qa.service.QaCorpusService;
import com.campus.qa.service.QaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能问答服务实现类
 */
@Service
public class QaServiceImpl implements QaService {
    
    private static final Logger log = LoggerFactory.getLogger(QaServiceImpl.class);
    
    @Value("${rasa.api-url}")
    private String rasaApiUrl;
    
    @Value("${rasa.webhook-url}")
    private String webhookUrl;
    
    @Autowired
    private QaConversationService conversationService;
    
    @Autowired
    private QaCorpusService corpusService;
    
    @Autowired
    private AiService aiService;
    
    @Override
    public Map<String, Object> sendQuestion(String question, String sessionId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        String answer = null;
        String intent = "";
        Double confidence = 0.0;
        Long corpusId = null;
        String source = ""; // 标记答案来源
        
        try {
            log.info("📝 收到问题: {}", question);
            
            // ========================================
            // 三层混合AI策略
            // ========================================
            
            // 第一层：本地语料库精确匹配（最快、最准确）
            try {
                log.debug("🔍 第一层：检查本地语料库...");
                QaCorpus corpus = corpusService.findBestMatch(question);
                
                if (corpus != null) {
                    answer = corpus.getAnswer();
                    intent = corpus.getIntent();
                    confidence = 1.0;
                    corpusId = corpus.getId();
                    source = "LOCAL_CORPUS";
                    
                    // 增加使用次数
                    corpusService.increaseUsageCount(corpus.getId());
                    
                    log.info("✅ 答案来源: 本地语料库 [问题: {}]", question);
                } else {
                    log.debug("ℹ️ 本地语料库未找到匹配");
                }
            } catch (Exception e) {
                log.warn("⚠️ 本地语料库查询异常: {}", e.getMessage());
            }
            
            // 第二层：Rasa AI（训练过的校园场景）
            if (answer == null || answer.isEmpty()) {
                try {
                    log.debug("🔍 第二层：调用Rasa AI...");
                    Map<String, Object> rasaResult = callRasaApi(question, sessionId);
                    String rasaAnswer = (String) rasaResult.get("answer");
                    String rasaIntent = (String) rasaResult.get("intent");
                    Double rasaConfidence = (Double) rasaResult.get("confidence");
                    
                    // 判断Rasa是否给出了有效答案
                    if (rasaAnswer != null && !rasaAnswer.isEmpty() 
                        && !rasaAnswer.contains("没有理解") 
                        && !rasaAnswer.contains("没有找到")
                        && !rasaAnswer.contains("不可用")
                        && rasaConfidence > 0.5) {
                        
                        answer = rasaAnswer;
                        intent = rasaIntent;
                        confidence = rasaConfidence;
                        source = "RASA";
                        log.info("✅ 答案来源: Rasa AI [意图: {}, 置信度: {}]", intent, confidence);
                    } else {
                        log.debug("ℹ️ Rasa未能提供有效答案");
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Rasa调用异常: {}", e.getMessage());
                }
            }
            
            // 第三层：DeepSeek大模型兜底（最智能、最灵活）
            if (answer == null || answer.isEmpty()) {
                try {
                    log.info("🔍 第三层：启用DeepSeek大模型兜底...");
                    
                    // 构建上下文：从语料库中查找相关信息
                    String context = buildContextForAi(question);
                    log.debug("📚 为AI构建的上下文长度: {}", context.length());
                    
                    // 调用DeepSeek
                    answer = aiService.chat(question, context);
                    
                    if (answer != null && !answer.isEmpty()) {
                        intent = "ai_fallback";
                        confidence = 0.8;
                        source = "DEEPSEEK";
                        log.info("✅ 答案来源: DeepSeek AI [问题: {}]", question);
                    } else {
                        log.error("❌ DeepSeek返回了空答案");
                        answer = "抱歉，我暂时无法回答您的问题。这可能是因为AI服务暂时不可用，请稍后再试。";
                        source = "ERROR";
                    }
                } catch (Exception e) {
                    log.error("❌ DeepSeek调用失败: {}", e.getMessage(), e);
                    answer = "抱歉，AI服务出现异常。详细信息: " + e.getMessage();
                    source = "ERROR";
                }
            }
            
            // 如果三层都失败了
            if (answer == null || answer.isEmpty()) {
                log.error("❌ 三层AI策略全部失败");
                answer = "抱歉,我暂时无法回答您的问题,请稍后再试。";
                source = "ERROR";
            }
            
            // 记录对话
            try {
                log.debug("📝 准备保存对话记录...");
                QaConversation conversation = new QaConversation();
                conversation.setSessionId(sessionId);
                conversation.setUserId(userId);
                conversation.setUserQuestion(question);
                conversation.setBotAnswer(answer);
                conversation.setIntent(intent);
                conversation.setConfidence(confidence);
                conversation.setCorpusId(corpusId);
                
                log.debug("对话记录详情: sessionId={}, userId={}, question={}, answer长度={}", 
                    sessionId, userId, question, answer != null ? answer.length() : 0);
                
                recordConversation(conversation);
                log.info("✅ 对话记录保存成功");
            } catch (Exception e) {
                log.error("❌ 记录对话失败，完整异常信息: ", e);
                // 不影响返回结果
            }
            
            result.put("success", true);
            result.put("answer", answer);
            result.put("intent", intent);
            result.put("confidence", confidence);
            result.put("source", source);  // 告诉前端答案来源
            
        } catch (Exception e) {
            log.error("❌ 问答服务顶层异常", e);
            result.put("success", false);
            result.put("answer", "系统错误: " + e.getMessage());
            result.put("error", e.getMessage());
            result.put("source", "ERROR");
        }
        
        return result;
    }
    
    /**
     * 为AI大模型构建上下文信息
     */
    private String buildContextForAi(String question) {
        try {
            // 从语料库中查找相关信息
            List<QaCorpus> relatedCorpus = corpusService.findRelated(question, 3);
            
            if (relatedCorpus == null || relatedCorpus.isEmpty()) {
                return "";
            }
            
            StringBuilder context = new StringBuilder();
            context.append("以下是校园相关的参考信息：\n\n");
            
            for (int i = 0; i < relatedCorpus.size(); i++) {
                QaCorpus corpus = relatedCorpus.get(i);
                context.append(i + 1).append(". ");
                context.append("问：").append(corpus.getQuestion()).append("\n");
                context.append("   答：").append(corpus.getAnswer()).append("\n\n");
            }
            
            return context.toString();
        } catch (Exception e) {
            log.error("构建AI上下文失败", e);
            return "";
        }
    }
    
    /**
     * 调用Rasa API
     */
    private Map<String, Object> callRasaApi(String message, String sender) {
        Map<String, Object> result = new HashMap<>();
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = rasaApiUrl + webhookUrl;
            HttpPost httpPost = new HttpPost(url);
            
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("sender", sender);
            requestBody.put("message", message);
            
            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            
            // 发送请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.debug("🔍 Rasa 原始响应: {}", responseString);
                JSONArray responseArray = JSON.parseArray(responseString);
                
                if (responseArray != null && !responseArray.isEmpty()) {
                    // 拼接所有消息
                    StringBuilder fullAnswer = new StringBuilder();
                    for (int i = 0; i < responseArray.size(); i++) {
                        JSONObject messageObj = responseArray.getJSONObject(i);
                        String text = messageObj.getString("text");
                        if (text != null && !text.isEmpty()) {
                            fullAnswer.append(text);
                            // 如果不是最后一条消息，添加换行
                            if (i < responseArray.size() - 1) {
                                fullAnswer.append("\n");
                            }
                        }
                    }
                    
                    String answer = fullAnswer.toString();
                    log.debug("🔍 拼接后的完整 answer 长度: {}", answer.length());
                    log.debug("🔍 拼接后的完整 answer 内容: {}", answer);
                    
                    result.put("answer", !answer.isEmpty() ? answer : "抱歉,我没有理解您的问题。");
                    result.put("intent", "unknown");
                    result.put("confidence", 0.8);  // 提高置信度，确保Rasa的回答被使用
                } else {
                    result.put("answer", "抱歉,我没有找到相关答案。");
                    result.put("intent", "unknown");
                    result.put("confidence", 0.0);
                }
            }
            
        } catch (Exception e) {
            result.put("answer", "Rasa服务暂时不可用,使用本地知识库进行回答。");
            result.put("intent", "error");
            result.put("confidence", 0.0);
        }
        
        return result;
    }
    
    @Override
    public void recordConversation(QaConversation conversation) {
        conversationService.save(conversation);
    }
    
    @Override
    public boolean trainModel() {
        // 这里可以调用Rasa的训练API
        // 实际实现需要根据Rasa的具体版本和配置来完成
        return true;
    }
    
    @Override
    public boolean syncCorpusToRasa() {
        // 将语料库数据同步到Rasa训练数据文件
        // 实际实现需要生成Rasa所需的训练数据格式
        corpusService.list();
        
        // TODO: 将语料库转换为Rasa训练数据格式并写入文件
        
        return true;
    }
}

