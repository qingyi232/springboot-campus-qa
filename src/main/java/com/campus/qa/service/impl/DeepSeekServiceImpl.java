package com.campus.qa.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.campus.qa.service.AiService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek AI服务实现类
 */
@Service
public class DeepSeekServiceImpl implements AiService {
    
    private static final Logger log = LoggerFactory.getLogger(DeepSeekServiceImpl.class);
    
    @Value("${deepseek.api-key}")
    private String apiKey;
    
    @Value("${deepseek.api-url}")
    private String apiUrl;
    
    @Value("${deepseek.model}")
    private String model;
    
    @Value("${deepseek.max-tokens}")
    private Integer maxTokens;
    
    @Value("${deepseek.temperature}")
    private Double temperature;
    
    @Value("${deepseek.timeout}")
    private Long timeout;
    
    private OkHttpClient httpClient;
    
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    
    @PostConstruct
    public void init() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
        
        log.info("DeepSeek AI Service initialized with model: {}", model);
    }
    
    @Override
    public String chat(String question, String context) {
        try {
            // 构建系统提示词
            String systemPrompt = buildSystemPrompt(context);
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            
            // 构建消息列表
            JSONArray messages = new JSONArray();
            
            // 系统消息
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
            
            // 用户消息
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            
            // 发送请求
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE))
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("DeepSeek API call failed: {}", response.code());
                    return "抱歉，AI服务暂时不可用，请稍后再试。";
                }
                
                String responseBody = response.body().string();
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                
                // 解析回答
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    String answer = message.getString("content");
                    
                    log.info("DeepSeek response received, length: {}", answer.length());
                    return answer;
                } else {
                    return "抱歉，未能获取到有效回答。";
                }
            }
            
        } catch (IOException e) {
            log.error("DeepSeek API call error", e);
            return "抱歉，网络连接失败，请稍后再试。";
        } catch (Exception e) {
            log.error("DeepSeek service error", e);
            return "抱歉，系统出现错误，请稍后再试。";
        }
    }
    
    @Override
    public String chatWithHistory(String question, List<String> history) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            
            // 构建消息列表（包含历史）
            JSONArray messages = new JSONArray();
            
            // 系统消息
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", buildSystemPrompt(null));
            messages.add(systemMessage);
            
            // 添加历史对话
            if (history != null && !history.isEmpty()) {
                for (int i = 0; i < history.size(); i++) {
                    JSONObject msg = new JSONObject();
                    msg.put("role", i % 2 == 0 ? "user" : "assistant");
                    msg.put("content", history.get(i));
                    messages.add(msg);
                }
            }
            
            // 当前问题
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            
            // 发送请求
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE))
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "抱歉，AI服务暂时不可用。";
                }
                
                String responseBody = response.body().string();
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                JSONArray choices = jsonResponse.getJSONArray("choices");
                
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    return message.getString("content");
                }
                
                return "抱歉，未能获取到有效回答。";
            }
            
        } catch (Exception e) {
            log.error("DeepSeek chat with history error", e);
            return "抱歉，系统出现错误。";
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // 发送简单测试请求
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 10);
            
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", "hi");
            messages.add(message);
            requestBody.put("messages", messages);
            
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE))
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.warn("DeepSeek service availability check failed", e);
            return false;
        }
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的校园智能问答助手，负责回答学生和老师关于校园的各类问题。\n");
        prompt.append("你的回答应该：\n");
        prompt.append("1. 准确、专业、友好\n");
        prompt.append("2. 简洁明了，重点突出\n");
        prompt.append("3. 如果不确定答案，诚实告知并建议联系相关部门\n");
        prompt.append("4. 使用中文回答\n");
        
        if (context != null && !context.isEmpty()) {
            prompt.append("\n相关校园信息参考：\n");
            prompt.append(context);
            prompt.append("\n请基于以上信息回答用户问题。");
        }
        
        return prompt.toString();
    }
}

