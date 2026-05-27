package com.campus.qa.service.impl;

import com.campus.qa.entity.QaCorpus;
import com.campus.qa.service.QaCorpusService;
import com.campus.qa.service.RasaTrainingService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rasa训练服务实现类
 */
@Service
public class RasaTrainingServiceImpl implements RasaTrainingService {
    
    @Value("${rasa.api-url}")
    private String rasaApiUrl;
    
    @Autowired
    private QaCorpusService corpusService;
    
    private static final String RASA_DATA_PATH = "rasa/data/generated_nlu.yml";
    
    @Override
    public boolean generateRasaNluData() {
        try {
            // 获取所有启用的语料
            List<QaCorpus> corpusList = corpusService.list();
            
            // 按意图分组
            Map<String, StringBuilder> intentExamples = new HashMap<>();
            
            for (QaCorpus corpus : corpusList) {
                if (corpus.getStatus() == 1) { // 只处理启用的语料
                    String intent = corpus.getIntent();
                    if (intent == null || intent.isEmpty()) {
                        intent = "general_question";
                    }
                    
                    intentExamples.putIfAbsent(intent, new StringBuilder());
                    
                    // 添加问题作为训练样本
                    intentExamples.get(intent)
                        .append("      - ").append(corpus.getQuestion()).append("\n");
                    
                    // 如果有关键词，也添加为训练样本
                    if (corpus.getKeywords() != null && !corpus.getKeywords().isEmpty()) {
                        String[] keywords = corpus.getKeywords().split(",");
                        for (String keyword : keywords) {
                            intentExamples.get(intent)
                                .append("      - ").append(keyword.trim()).append("\n");
                        }
                    }
                }
            }
            
            // 生成NLU训练数据文件
            try (FileWriter writer = new FileWriter(RASA_DATA_PATH)) {
                writer.write("version: \"3.1\"\n\n");
                writer.write("nlu:\n");
                
                for (Map.Entry<String, StringBuilder> entry : intentExamples.entrySet()) {
                    writer.write("  - intent: " + entry.getKey() + "\n");
                    writer.write("    examples: |\n");
                    writer.write(entry.getValue().toString());
                    writer.write("\n");
                }
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean triggerRasaTraining() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 调用Rasa训练API
            HttpPost httpPost = new HttpPost(rasaApiUrl + "/model/train");
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                return statusCode == 200 || statusCode == 204;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean checkRasaStatus() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(rasaApiUrl + "/status");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return response.getStatusLine().getStatusCode() == 200;
            }
        } catch (Exception e) {
            return false;
        }
    }
}


