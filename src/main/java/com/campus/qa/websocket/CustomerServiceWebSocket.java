package com.campus.qa.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客服WebSocket处理器
 */
@Slf4j
@Component
@ServerEndpoint("/ws/customer-service/{userId}/{role}")
public class CustomerServiceWebSocket {
    
    /**
     * 存储所有在线用户的WebSocket会话
     * key: userId_role, value: Session
     */
    private static Map<String, Session> onlineUsers = new ConcurrentHashMap<>();
    
    /**
     * 存储用户会话映射
     * key: sessionId, value: Set<userId_role>
     */
    private static Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(@PathParam("userId") String userId, 
                       @PathParam("role") String role, 
                       Session session) {
        String userKey = userId + "_" + role;
        onlineUsers.put(userKey, session);
        log.info("客服WebSocket连接建立: userId={}, role={}, 当前在线人数={}", 
                userId, role, onlineUsers.size());
        
        // 发送连接成功消息
        JSONObject message = new JSONObject();
        message.put("type", "connect");
        message.put("message", "连接成功");
        sendMessage(session, message.toJSONString());
    }
    
    @OnClose
    public void onClose(@PathParam("userId") String userId, 
                        @PathParam("role") String role) {
        String userKey = userId + "_" + role;
        onlineUsers.remove(userKey);
        log.info("客服WebSocket连接关闭: userId={}, role={}, 当前在线人数={}", 
                userId, role, onlineUsers.size());
    }
    
    @OnMessage
    public void onMessage(String message, 
                         @PathParam("userId") String userId, 
                         @PathParam("role") String role) {
        log.info("收到消息: userId={}, role={}, message={}", userId, role, message);
        
        try {
            JSONObject messageObj = JSON.parseObject(message);
            String type = messageObj.getString("type");
            
            if ("chat".equals(type)) {
                // 聊天消息，转发给对方
                String receiverId = messageObj.getString("receiverId");
                String receiverRole = messageObj.getString("receiverRole");
                String receiverKey = receiverId + "_" + receiverRole;
                
                Session receiverSession = onlineUsers.get(receiverKey);
                if (receiverSession != null && receiverSession.isOpen()) {
                    sendMessage(receiverSession, message);
                    log.info("消息已转发: {} -> {}", userId + "_" + role, receiverKey);
                } else {
                    log.warn("接收者不在线: {}", receiverKey);
                }
            } else if ("typing".equals(type)) {
                // 正在输入状态，转发给对方
                String receiverId = messageObj.getString("receiverId");
                String receiverRole = messageObj.getString("receiverRole");
                String receiverKey = receiverId + "_" + receiverRole;
                
                Session receiverSession = onlineUsers.get(receiverKey);
                if (receiverSession != null && receiverSession.isOpen()) {
                    sendMessage(receiverSession, message);
                }
            }
        } catch (Exception e) {
            log.error("处理消息失败: " + e.getMessage(), e);
        }
    }
    
    @OnError
    public void onError(@PathParam("userId") String userId, 
                       @PathParam("role") String role, 
                       Throwable error) {
        log.error("WebSocket错误: userId={}, role={}, error={}", 
                userId, role, error.getMessage());
    }
    
    /**
     * 发送消息给指定用户
     */
    public static void sendToUser(String userId, String role, String message) {
        String userKey = userId + "_" + role;
        Session session = onlineUsers.get(userKey);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }
    
    /**
     * 广播消息给所有在线用户
     */
    public static void broadcast(String message) {
        onlineUsers.values().forEach(session -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }
    
    /**
     * 发送消息
     */
    private static void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("发送消息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取在线用户数
     */
    public static int getOnlineCount() {
        return onlineUsers.size();
    }
    
    /**
     * 检查用户是否在线
     */
    public static boolean isOnline(String userId, String role) {
        String userKey = userId + "_" + role;
        Session session = onlineUsers.get(userKey);
        return session != null && session.isOpen();
    }
}


