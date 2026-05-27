<template>
  <div class="customer-service-admin">
    <div class="page-header">
      <h2>
        <i class="el-icon-service"></i>
        人工客服工作台
      </h2>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-label">等待中</span>
          <span class="stat-value waiting">{{ waitingCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">服务中</span>
          <span class="stat-value serving">{{ servingCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">在线人数</span>
          <span class="stat-value online">{{ onlineCount }}</span>
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- 会话列表 -->
      <div class="session-list-panel">
        <el-tabs v-model="activeTab" @tab-click="handleTabChange">
          <el-tab-pane label="等待接入" name="waiting">
            <div class="session-list">
              <div
                v-for="session in waitingSessions"
                :key="session.id"
                class="session-item"
                :class="{ active: currentSession && currentSession.id === session.id }"
                @click="selectSession(session)"
              >
                <div class="session-avatar">
                  <i class="el-icon-user-solid"></i>
                </div>
                <div class="session-info">
                  <div class="session-name">{{ session.studentName }}</div>
                  <div class="session-time">{{ session.createTime }}</div>
                </div>
                <el-button
                  type="primary"
                  size="mini"
                  @click.stop="acceptSession(session)"
                >
                  接入
                </el-button>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="服务中" name="serving">
            <div class="session-list">
              <div
                v-for="session in servingSessions"
                :key="session.id"
                class="session-item"
                :class="{ active: currentSession && currentSession.id === session.id }"
                @click="selectSession(session)"
              >
                <div class="session-avatar">
                  <i class="el-icon-user-solid"></i>
                  <span v-if="session.unreadCount > 0" class="unread-badge">{{ session.unreadCount }}</span>
                </div>
                <div class="session-info">
                  <div class="session-name">{{ session.studentName }}</div>
                  <div class="session-last-msg">{{ session.lastMessage }}</div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 聊天区域 -->
      <div class="chat-panel" v-if="currentSession">
        <div class="chat-header">
          <div class="user-info">
            <div class="user-avatar">
              <i class="el-icon-user-solid"></i>
            </div>
            <div>
              <div class="user-name">{{ currentSession.studentName }}</div>
              <div class="user-status">{{ getSessionStatusText(currentSession.status) }}</div>
            </div>
          </div>
          <div class="header-actions">
            <el-button size="small" @click="closeCurrentSession">
              <i class="el-icon-close"></i> 结束会话
            </el-button>
          </div>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <div v-for="(msg, index) in messages" :key="index" :class="['msg-wrapper', 'msg-' + msg.type]">
            <div class="msg-avatar" v-if="msg.type === 'user'">
              <i class="el-icon-user-solid"></i>
            </div>
            <div class="msg-bubble">
              <pre class="msg-text">{{ msg.content }}</pre>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
            <div class="msg-avatar" v-if="msg.type === 'bot'">
              <i class="el-icon-service"></i>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <el-input
            type="textarea"
            v-model="message"
            placeholder="输入消息..."
            :rows="3"
            @keyup.ctrl.enter="sendMessage"
          ></el-input>
          <div class="input-actions">
            <span class="input-hint">Ctrl + Enter 发送</span>
            <el-button type="primary" @click="sendMessage" :disabled="!message.trim()">
              <i class="el-icon-s-promotion"></i> 发送
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-panel" v-else>
        <el-empty description="请从左侧选择一个会话"></el-empty>
      </div>
    </div>
  </div>
</template>

<script>
import {
  getWaitingSessions,
  getServingSessions,
  acceptSession as acceptSessionApi,
  sendMessage as sendChatMessage,
  getSessionMessages,
  closeSession as closeSessionApi,
  getOnlineCount
} from '@/api/customerService'

export default {
  name: 'CustomerServiceAdmin',
  data() {
    return {
      activeTab: 'waiting',
      waitingSessions: [],
      servingSessions: [],
      currentSession: null,
      messages: [],
      message: '',
      ws: null,
      waitingCount: 0,
      servingCount: 0,
      onlineCount: 0,
      refreshTimer: null
    }
  },
  mounted() {
    this.loadSessions()
    this.loadOnlineCount()
    this.connectWebSocket()
    
    // 每30秒刷新会话列表
    this.refreshTimer = setInterval(() => {
      this.loadSessions()
      this.loadOnlineCount()
    }, 30000)
  },
  beforeDestroy() {
    this.disconnectWebSocket()
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
  },
  methods: {
    async loadSessions() {
      try {
        // 加载等待中的会话
        const waitingRes = await getWaitingSessions({ current: 1, size: 50 })
        if (waitingRes.code === 200 && waitingRes.data) {
          this.waitingSessions = waitingRes.data.records || []
          this.waitingCount = waitingRes.data.total || 0
        }
        
        // 加载服务中的会话
        const servingRes = await getServingSessions({ current: 1, size: 50 })
        if (servingRes.code === 200 && servingRes.data) {
          this.servingSessions = servingRes.data.records || []
          this.servingCount = servingRes.data.total || 0
        }
      } catch (error) {
        console.error('加载会话列表失败:', error)
      }
    },
    async loadOnlineCount() {
      try {
        const res = await getOnlineCount()
        if (res.code === 200 && res.data) {
          this.onlineCount = res.data.count || 0
        }
      } catch (error) {
        console.error('加载在线人数失败:', error)
      }
    },
    async acceptSession(session) {
      try {
        await acceptSessionApi(session.id)
        this.$message.success('已接入会话')
        this.loadSessions()
        this.selectSession(session)
        this.activeTab = 'serving'
      } catch (error) {
        this.$message.error('接入失败')
        console.error('接入会话失败:', error)
      }
    },
    async selectSession(session) {
      this.currentSession = session
      await this.loadMessages()
      this.connectWebSocket()
    },
    async loadMessages() {
      if (!this.currentSession) return
      
      try {
        const res = await getSessionMessages(this.currentSession.id)
        if (res.code === 200 && res.data) {
          this.messages = res.data.map(msg => ({
            type: msg.senderRole === 'admin' ? 'bot' : 'user',
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
    async sendMessage() {
      if (!this.message.trim() || !this.currentSession) return
      
      const content = this.message.trim()
      this.message = ''
      
      // 添加到消息列表
      this.messages.push({
        type: 'bot',
        content: content,
        time: this.formatTime(new Date())
      })
      
      this.$nextTick(() => {
        this.scrollToBottom()
      })
      
      try {
        // 发送消息到后端
        await sendChatMessage({
          sessionId: this.currentSession.id,
          receiverId: this.currentSession.studentId,
          receiverRole: 'student',
          content: content,
          messageType: 'text'
        })
        
        // 通过WebSocket发送
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
          this.ws.send(JSON.stringify({
            type: 'chat',
            sessionId: this.currentSession.id,
            receiverId: this.currentSession.studentId,
            receiverRole: 'student',
            content: content
          }))
        }
      } catch (error) {
        this.$message.error('发送消息失败')
        console.error('发送消息失败:', error)
      }
    },
    async closeCurrentSession() {
      if (!this.currentSession) return
      
      try {
        await this.$confirm('确定要结束此会话吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await closeSessionApi(this.currentSession.id)
        this.$message.success('会话已结束')
        this.currentSession = null
        this.messages = []
        this.loadSessions()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('结束会话失败')
        }
      }
    },
    handleTabChange() {
      this.currentSession = null
      this.messages = []
    },
    connectWebSocket() {
      const userId = localStorage.getItem('userId')
      const wsUrl = `ws://localhost:8080/ws/customer-service/${userId}/admin`
      
      if (this.ws) {
        this.ws.close()
      }
      
      this.ws = new WebSocket(wsUrl)
      
      this.ws.onopen = () => {
        console.log('WebSocket连接成功')
      }
      
      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          
          if (data.type === 'chat') {
            // 只有当前会话的消息才显示
            if (this.currentSession && data.sessionId === this.currentSession.id) {
              this.messages.push({
                type: 'user',
                content: data.content,
                time: this.formatTime(new Date())
              })
              
              this.$nextTick(() => {
                this.scrollToBottom()
              })
            }
            
            // 刷新会话列表
            this.loadSessions()
          }
        } catch (error) {
          console.error('处理消息失败:', error)
        }
      }
      
      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
      }
      
      this.ws.onclose = () => {
        console.log('WebSocket连接关闭')
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
    },
    getSessionStatusText(status) {
      const texts = {
        waiting: '等待中',
        serving: '服务中',
        closed: '已关闭'
      }
      return texts[status] || status
    }
  }
}
</script>

<style scoped>
.customer-service-admin {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  background: white;
  padding: 20px 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-header h2 i {
  color: #409EFF;
  font-size: 24px;
}

.header-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
}

.stat-value.waiting {
  color: #E6A23C;
}

.stat-value.serving {
  color: #409EFF;
}

.stat-value.online {
  color: #67C23A;
}

.main-content {
  display: flex;
  gap: 20px;
  height: calc(100vh - 180px);
}

.session-list-panel {
  width: 350px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.session-list {
  max-height: calc(100vh - 280px);
  overflow-y: auto;
}

.session-item {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #ecf5ff;
}

.session-avatar {
  position: relative;
  width: 45px;
  height: 45px;
  background: #409EFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #F56C6C;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 600;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.session-time,
.session-last-msg {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-panel,
.empty-panel {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
}

.empty-panel {
  justify-content: center;
  align-items: center;
}

.chat-header {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: #409EFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
}

.user-status {
  font-size: 12px;
  color: #909399;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.msg-wrapper {
  display: flex;
  gap: 12px;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.msg-wrapper.msg-bot {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
  color: white;
}

.msg-user .msg-avatar {
  background: #409EFF;
}

.msg-bot .msg-avatar {
  background: #67C23A;
}

.msg-bubble {
  max-width: 60%;
  padding: 10px 15px;
  border-radius: 10px;
}

.msg-user .msg-bubble {
  background: #f0f0f0;
}

.msg-bot .msg-bubble {
  background: #409EFF;
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

.msg-time {
  margin-top: 5px;
  font-size: 11px;
  opacity: 0.6;
  text-align: right;
}

.chat-input-area {
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.input-hint {
  font-size: 12px;
  color: #909399;
}
</style>


