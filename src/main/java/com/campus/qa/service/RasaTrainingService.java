package com.campus.qa.service;

/**
 * Rasa训练服务接口
 */
public interface RasaTrainingService {
    
    /**
     * 从数据库语料库生成Rasa训练数据
     */
    boolean generateRasaNluData();
    
    /**
     * 触发Rasa模型训练
     */
    boolean triggerRasaTraining();
    
    /**
     * 检查Rasa服务状态
     */
    boolean checkRasaStatus();
}


