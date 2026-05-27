<template>
  <div class="customer-service">
    <!-- 科技感背景 -->
    <div class="tech-bg">
      <div class="circuit-pattern"></div>
    </div>
    
    <div class="service-container">
      <!-- 聊天区域 -->
      <div class="chat-section glass-panel">
        <div class="chat-header">
          <div class="service-info">
            <div class="service-avatar">
              <i class="el-icon-service"></i>
              <div v-if="serviceOnline" class="online-dot"></div>
            </div>
            <div class="info-text">
              <div class="service-name">
                {{ session && session.serviceName ? session.serviceName : '人工客服' }}
              </div>
              <div class="service-status">
                <span v-if="session && session.status === 'waiting'" class="status-waiting">
                  <i class="el-icon-loading"></i> 等待接入...
                </span>
                <span v-else-if="session && session.status === 'serving'" class="status-serving">
                  <i class="el-icon-success"></i> 服务中
                </span>
                <span v-else class="status-offline">
                  <i class="el-icon-warning"></i> 离线
                </span>
              </div>
            </div>
          </div>
          <div class="header-actions">
            <el-tooltip content="结束会话" placement="bottom">
              <button v-if="session" class="action-btn" @click="closeSession">
                <i class="el-icon-close"></i>
              </button>
            </el-tooltip>
          </div>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <!-- 欢迎界面 -->
          <div v-if="messages.length === 0" class="welcome-screen">
            <div class="service-icon">
              <i class="el-icon-service"></i>
            </div>
            <h2>欢迎使用人工客服</h2>
            <p>我们将尽快为您服务</p>
            <el-button type="primary" @click="startChat" v-if="!session">
              开始咨询
            </el-button>
          </div>

          <!-- 消息列表 -->
          <div v-for="(msg, index) in messages" :key="index" :class="['msg-wrapper', 'msg-' + msg.type]">
            <div class="msg-avatar" v-if="msg.type === 'bot'">
              <i class="el-icon-service"></i>
            </div>
            <div class="msg-bubble">
              <pre class="msg-text">{{ msg.content }}</pre>
              <div class="msg-meta">
                <span class="msg-time">{{ msg.time }}</span>
              </div>
            </div>
            <div class="msg-avatar" v-if="msg.type === 'user'">
              <i class="el-icon-user-solid"></i>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <div class="input-wrapper">
            <input
              v-model="message"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
              :disabled="!session || session.status === 'closed'"
              class="chat-input"
            />
            <button class="send-btn" @click="sendMessage" :disabled="!session || !message.trim() || session.status === 'closed'">
              <i class="el-icon-s-promotion"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-panel glass-panel">
          <div class="panel-header">
            <i class="el-icon-info"></i>
            <span>服务说明</span>
          </div>
          <div class="service-tips">
            <div class="tip-item">
              <i class="el-icon-time"></i>
              <span>服务时间：周一至周五 9:00-18:00</span>
            </div>
            <div class="tip-item">
              <i class="el-icon-chat-line-round"></i>
              <span>请清晰描述您的问题</span>
            </div>
            <div class="tip-item">
              <i class="el-icon-warning-outline"></i>
              <span>请勿发送敏感信息</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getSession, sendMessage as sendChatMessage, getSessionMessages, closeSession as closeSessionApi } from '@/api/customerService'

export default {
  name: 'CustomerService',
  data() {
    return {
      session: null,
      messages: [],
      message: '',
      ws: null,
      serviceOnline: false
    }
  },
  mounted() {
    this.loadSession()
  },
  beforeDestroy() {
    this.disconnectWebSocket()
  },
  methods: {
    async loadSession() {
      try {
        const res = await getSession()
        if (res.code === 200 && res.data) {
          this.session = res.data
          this.loadMessages()
          this.connectWebSocket()
        }
      } catch (error) {
        console.error('加载会话失败:', error)
      }
    },
    async loadMessages() {
      if (!this.session) return
      
      try {
        const res = await getSessionMessages(this.session.id)
        if (res.code === 200 && res.data) {
          this.messages = res.data.map(msg => ({
            type: msg.senderRole === 'student' ? 'user' : 'bot',
            content: msg.content,
            time: this.formatTime(msg.createTime)
          }))
          this.$nextTick(() => {
            this.scrollToBottom()
          })
        }
      } catch (error) {
        console.error('加载消息失败:', error)
      }
    },
    async startChat() {
      await this.loadSession()
    },
    async sendMessage() {
      if (!this.message.trim() || !this.session) return
      
      const content = this.message.trim()
      this.message = ''
      
      // 添加到消息列表
      this.messages.push({
        type: 'user',
        content: content,
        time: this.formatTime(new Date())
      })
      
      this.$nextTick(() => {
        this.scrollToBottom()
      })
      
      try {
        // 发送消息到后端
        await sendChatMessage({
          sessionId: this.session.id,
          receiverId: this.session.serviceId,
          receiverRole: 'admin',
          content: content,
          messageType: 'text'
        })
        
        // 通过WebSocket发送
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
          this.ws.send(JSON.stringify({
            type: 'chat',
            sessionId: this.session.id,
            receiverId: this.session.serviceId,
            receiverRole: 'admin',
            content: content
          }))
        }
      } catch (error) {
        this.$message.error('发送消息失败')
        console.error('发送消息失败:', error)
      }
    },
    async closeSession() {
      if (!this.session) return
      
      try {
        await this.$confirm('确定要结束会话吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await closeSessionApi(this.session.id)
        this.$message.success('会话已结束')
        this.session.status = 'closed'
        this.disconnectWebSocket()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('结束会话失败')
        }
      }
    },
    connectWebSocket() {
      if (!this.session) return
      
      const userId = localStorage.getItem('userId')
      const wsUrl = `ws://localhost:8080/ws/customer-service/${userId}/student`
      
      this.ws = new WebSocket(wsUrl)
      
      this.ws.onopen = () => {
        console.log('WebSocket连接成功')
      }
      
      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          
          if (data.type === 'chat') {
            this.messages.push({
              type: 'bot',
              content: data.content,
              time: this.formatTime(new Date())
            })
            
            this.$nextTick(() => {
              this.scrollToBottom()
            })
          } else if (data.type === 'connect') {
            this.serviceOnline = true
          }
        } catch (error) {
          console.error('处理消息失败:', error)
        }
      }
      
      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
        this.serviceOnline = false
      }
      
      this.ws.onclose = () => {
        console.log('WebSocket连接关闭')
        this.serviceOnline = false
      }
    },
    disconnectWebSocket() {
      if (this.ws) {
        this.ws.close()
        this.ws = null
      }
    },
    scrollToBottom() {
      const container = this.$refs.messagesContainer
      if (container) {
        container.scrollTop = container.scrollHeight
      }
    },
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.customer-service {
  position: relative;
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.tech-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.1;
  pointer-events: none;
}

.circuit-pattern {
  width: 100%;
  height: 100%;
  background-image: 
    repeating-linear-gradient(90deg, transparent, transparent 50px, rgba(255,255,255,0.1) 50px, rgba(255,255,255,0.1) 51px),
    repeating-linear-gradient(0deg, transparent, transparent 50px, rgba(255,255,255,0.1) 50px, rgba(255,255,255,0.1) 51px);
}

.service-container {
  position: relative;
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px;
  display: flex;
  gap: 30px;
  min-height: calc(100vh - 60px);
}

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
}

.glass-panel {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.service-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.service-avatar {
  position: relative;
  width: 45px;
  height: 45px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background: #67C23A;
  border: 2px solid white;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.service-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.service-status {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.status-waiting {
  color: #E6A23C;
}

.status-serving {
  color: #67C23A;
}

.status-offline {
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  color: #606266;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  gap: 20px;
}

.service-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 50px;
}

.welcome-screen h2 {
  font-size: 24px;
  color: #2c3e50;
  margin: 0;
}

.welcome-screen p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.msg-wrapper {
  display: flex;
  gap: 12px;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.msg-wrapper.msg-user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 20px;
  color: white;
}

.msg-user .msg-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.msg-bot .msg-avatar {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.msg-bubble {
  max-width: 60%;
  padding: 12px 16px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.msg-user .msg-bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.msg-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
}

.msg-meta {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.msg-time {
  font-size: 12px;
  opacity: 0.6;
}

.chat-input-area {
  padding: 20px 30px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chat-input {
  flex: 1;
  padding: 12px 20px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 25px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.chat-input:focus {
  border-color: #667eea;
}

.send-btn {
  width: 45px;
  height: 45px;
  border: none;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: all 0.3s;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.sidebar {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-panel {
  padding: 25px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid rgba(0, 0, 0, 0.05);
}

.panel-header i {
  font-size: 20px;
  color: #667eea;
}

.service-tips {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.tip-item {
  display: flex;
  align-items: start;
  gap: 10px;
  padding: 12px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.tip-item i {
  color: #667eea;
  margin-top: 2px;
  font-size: 16px;
  flex-shrink: 0;
}
</style>


