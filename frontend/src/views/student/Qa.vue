<template>
  <div class="student-qa">
    <!-- 科技感背景 -->
    <div class="tech-bg">
      <div class="circuit-pattern"></div>
    </div>
    
    <div class="qa-container">
      <!-- 对话区域 -->
      <div class="chat-section glass-panel">
        <div class="chat-header">
          <div class="ai-indicator">
            <div class="pulse-dot"></div>
            <span>本地库在线</span>
          </div>
          <button class="action-btn" @click="clearChat">
            <i class="el-icon-delete"></i>
          </button>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <!-- 欢迎界面 -->
          <div v-if="messages.length === 0" class="welcome-screen">
            <div class="ai-avatar">
              <div class="avatar-glow"></div>
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <h2>您好！我是智能问答助手</h2>
            <p>为您提供精准的校园问答服务</p>
            <div class="quick-chips">
              <div
                class="chip"
                v-for="(q, i) in commonQuestions.slice(0, 4)"
                :key="i"
                @click="sendQuestion(q)"
              >
                <i class="el-icon-search"></i>
                <span>{{ q }}</span>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div v-for="(msg, index) in messages" :key="index" :class="['msg-wrapper', 'msg-' + msg.type]">
            <div class="msg-avatar" v-if="msg.type === 'bot'">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <div class="msg-bubble">
              <div v-if="msg.loading" class="loading-dots">
                <span></span><span></span><span></span>
              </div>
              <div v-else>
                <pre class="msg-text">{{ msg.content }}</pre>
                <div v-if="msg.source" class="msg-meta">
                  <span class="source-badge">{{ getSourceText(msg.source) }}</span>
                  <span class="msg-time">{{ msg.time }}</span>
                </div>
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
              v-model="question"
              placeholder="输入您的问题..."
              @keyup.enter="handleSend"
              :disabled="loading"
              class="chat-input"
            />
            <button class="send-btn" @click="handleSend" :disabled="loading || !question.trim()">
              <i class="el-icon-s-promotion"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-panel glass-panel">
          <div class="panel-header">
            <i class="el-icon-question"></i>
            <span>常见问题</span>
          </div>
          <div class="question-list">
            <div class="question-item" @click="sendQuestion(item)" v-for="(item, index) in commonQuestions" :key="index">
              <i class="el-icon-right"></i>
              <span>{{ item }}</span>
            </div>
          </div>
        </div>

        <div class="sidebar-panel glass-panel stats-panel">
          <div class="panel-header">
            <i class="el-icon-data-analysis"></i>
            <span>今日数据</span>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ todayStats.total }}</div>
              <div class="stat-label">总提问</div>
              <div class="stat-bar"></div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ todayStats.answered }}</div>
              <div class="stat-label">已解答</div>
              <div class="stat-bar stat-bar-success"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'StudentQa',
  data() {
    return {
      question: '',
      loading: false,
      messages: [],
      sessionId: '',
      commonQuestions: [
        '图书馆开放时间',
        '食堂在哪里',
        '如何选课',
        '校园卡充值方式',
        '期末考试安排',
        '如何查成绩',
        '新生报到流程'
      ],
      todayStats: {
        total: 0,
        answered: 0
      }
    }
  },
  computed: {
    ...mapGetters(['currentUser'])
  },
  mounted() {
    // 等待用户信息加载完成后再初始化
    this.$nextTick(() => {
      if (this.currentUser && this.currentUser.id) {
        // 为当前用户生成或恢复sessionId
        const sessionKey = `qa_session_id_${this.currentUser.id}`
        const savedSessionId = localStorage.getItem(sessionKey)
        if (savedSessionId) {
          this.sessionId = savedSessionId
        } else {
          this.sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
          localStorage.setItem(sessionKey, this.sessionId)
        }
        
        // 加载该用户的历史聊天记录
        this.loadChatHistory()
        
        // 加载今日统计数据
        this.loadTodayStats()
      }
    })
  },
  beforeDestroy() {
    // 保存聊天记录到localStorage
    this.saveChatHistory()
  },
  methods: {
    async handleSend() {
      if (!this.question.trim() || this.loading) return
      
      const userQuestion = this.question.trim()
      this.question = ''
      
      await this.sendQuestion(userQuestion)
    },
    async sendQuestion(question) {
      this.messages.push({
        type: 'user',
        content: question,
        time: this.getCurrentTime()
      })
      
      this.messages.push({
        type: 'bot',
        loading: true,
        content: '',
        time: this.getCurrentTime()
      })
      
      this.scrollToBottom()
      this.loading = true
      
      try {
        const res = await this.$axios.post('/qa/ask', {
          question: question,
          sessionId: this.sessionId,
          userId: this.currentUser.id
        })
        
        const lastMsg = this.messages[this.messages.length - 1]
        lastMsg.loading = false
        lastMsg.content = res.data.answer || '抱歉，我暂时无法回答这个问题。'
        lastMsg.source = res.data.source
        
        this.todayStats.total++
        this.todayStats.answered++
        
        // 保存聊天记录
        this.saveChatHistory()
        
      } catch (error) {
        const lastMsg = this.messages[this.messages.length - 1]
        lastMsg.loading = false
        lastMsg.content = '抱歉，系统出现错误，请稍后再试。'
      } finally {
        this.loading = false
        this.scrollToBottom()
      }
    },
    loadChatHistory() {
      // 从localStorage加载该用户的聊天记录（永久保存）
      try {
        if (!this.currentUser || !this.currentUser.id) return
        
        const historyKey = `qa_chat_history_${this.currentUser.id}`
        const saved = localStorage.getItem(historyKey)
        if (saved) {
          const data = JSON.parse(saved)
          if (data.messages && Array.isArray(data.messages)) {
            this.messages = data.messages
            this.$nextTick(() => {
              this.scrollToBottom()
            })
          }
        }
      } catch (e) {
        console.error('加载聊天历史失败:', e)
      }
    },
    saveChatHistory() {
      // 保存该用户的聊天记录到localStorage（永久保存）
      try {
        if (!this.currentUser || !this.currentUser.id) return
        
        const historyKey = `qa_chat_history_${this.currentUser.id}`
        const data = {
          userId: this.currentUser.id,
          username: this.currentUser.username,
          lastUpdate: new Date().toISOString(),
          messages: this.messages,
          stats: this.todayStats
        }
        localStorage.setItem(historyKey, JSON.stringify(data))
      } catch (e) {
        console.error('保存聊天历史失败:', e)
      }
    },
    loadTodayStats() {
      // 从localStorage加载该用户的统计数据
      try {
        if (!this.currentUser || !this.currentUser.id) return
        
        const historyKey = `qa_chat_history_${this.currentUser.id}`
        const saved = localStorage.getItem(historyKey)
        if (saved) {
          const data = JSON.parse(saved)
          const today = new Date().toDateString()
          // 检查是否是今天的统计
          if (data.lastUpdate) {
            const lastUpdateDate = new Date(data.lastUpdate).toDateString()
            if (lastUpdateDate === today && data.stats) {
              this.todayStats = data.stats
            } else {
              // 如果不是今天，重置今日统计（但保留聊天记录）
              this.todayStats = { total: 0, answered: 0 }
            }
          }
        }
      } catch (e) {
        console.error('加载统计数据失败:', e)
      }
    },
    clearChat() {
      this.$confirm('确定要清空对话记录吗?', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(() => {
        this.messages = []
        this.todayStats.total = 0
        this.todayStats.answered = 0
        // 清除该用户的localStorage
        if (this.currentUser && this.currentUser.id) {
          const historyKey = `qa_chat_history_${this.currentUser.id}`
          localStorage.removeItem(historyKey)
        }
        this.$message.success('已清空对话记录')
      }).catch(() => {})
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },
    getCurrentTime() {
      const now = new Date()
      return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    },
    getSourceText(source) {
      const map = {
        'LOCAL_CORPUS': '本地库',
        'RASA': '本地库',
        'DEEPSEEK': '本地库'
      }
      return map[source] || '本地库'
    }
  }
}
</script>

<style scoped>
.student-qa {
  position: relative;
  height: calc(100vh - 200px);
  min-height: 600px;
}

/* 科技感背景 */
.tech-bg {
  position: absolute;
  top: -30px;
  left: -30px;
  right: -30px;
  bottom: -30px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8eaed 100%);
  z-index: 0;
}

.circuit-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(66, 133, 244, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(66, 133, 244, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
}

.qa-container {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  height: 100%;
}

/* 玻璃拟态面板 */
.glass-panel {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
}

/* 对话区域 */
.chat-section {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.ai-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #202124;
}

.pulse-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #34a853, #0f9d58);
  animation: pulse 2s infinite;
  box-shadow: 0 0 0 0 rgba(52, 168, 83, 0.7);
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(52, 168, 83, 0.7);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(52, 168, 83, 0);
  }
}

.action-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  color: #5f6368;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.action-btn:hover {
  background: rgba(66, 133, 244, 0.08);
  color: #1967d2;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  scroll-behavior: smooth;
}

.welcome-screen {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.ai-avatar {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 24px;
  background: linear-gradient(135deg, #4285f4, #1967d2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: white;
  margin-bottom: 28px;
}

.avatar-glow {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 26px;
  background: linear-gradient(135deg, #4285f4, #1967d2);
  opacity: 0.3;
  filter: blur(12px);
  animation: avatarGlow 3s infinite;
}

@keyframes avatarGlow {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.05); }
}

.welcome-screen h2 {
  font-size: 26px;
  font-weight: 700;
  color: #202124;
  margin: 0 0 12px;
}

.welcome-screen p {
  font-size: 15px;
  color: #5f6368;
  margin: 0 0 36px;
  text-align: center;
}

.quick-chips {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  width: 100%;
  max-width: 500px;
}

.chip {
  padding: 14px 20px;
  background: white;
  border: 2px solid #e8eaed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #202124;
}

.chip:hover {
  border-color: #4285f4;
  background: rgba(66, 133, 244, 0.04);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(66, 133, 244, 0.2);
}

.chip i {
  font-size: 16px;
  color: #1967d2;
}

/* 消息样式 */
.msg-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: msgSlide 0.3s;
}

@keyframes msgSlide {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.msg-user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.msg-bot .msg-avatar {
  background: linear-gradient(135deg, #e8f0fe, #d2e3fc);
  color: #1967d2;
}

.msg-user .msg-avatar {
  background: linear-gradient(135deg, #5f6368, #3c4043);
  color: white;
}

.msg-bubble {
  max-width: 70%;
  padding: 16px 20px;
  border-radius: 16px;
  position: relative;
}

.msg-bot .msg-bubble {
  background: white;
  border: 1px solid #e8eaed;
  border-bottom-left-radius: 4px;
}

.msg-user .msg-bubble {
  background: linear-gradient(135deg, #4285f4, #1967d2);
  color: white;
  border-bottom-right-radius: 4px;
}

.msg-text {
  white-space: pre-wrap;
  margin: 0;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
}

.msg-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.source-badge {
  padding: 4px 10px;
  background: rgba(66, 133, 244, 0.08);
  color: #1967d2;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
}

.msg-time {
  font-size: 12px;
  color: #80868b;
}

/* 加载动画 */
.loading-dots {
  display: flex;
  gap: 8px;
  padding: 8px 0;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4285f4;
  animation: dotBounce 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 输入区域 */
.chat-input-area {
  padding: 20px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chat-input {
  flex: 1;
  padding: 14px 20px;
  border: 2px solid #e8eaed;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  background: white;
}

.chat-input:focus {
  border-color: #4285f4;
  box-shadow: 0 0 0 4px rgba(66, 133, 244, 0.1);
}

.send-btn {
  width: 48px;
  height: 48px;
  border: none;
  background: linear-gradient(135deg, #4285f4, #1967d2);
  color: white;
  border-radius: 50%;
  cursor: pointer;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(66, 133, 244, 0.4);
}

.send-btn:disabled {
  background: #e8eaed;
  color: #80868b;
  cursor: not-allowed;
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-panel {
  padding: 24px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #202124;
  margin-bottom: 20px;
}

.panel-header i {
  color: #1967d2;
  font-size: 18px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.question-item {
  padding: 12px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #202124;
}

.question-item:hover {
  background: rgba(66, 133, 244, 0.08);
  transform: translateX(4px);
}

.question-item i {
  font-size: 12px;
  color: #1967d2;
}

/* 统计面板 */
.stats-panel {
  background: linear-gradient(135deg, rgba(66, 133, 244, 0.05), rgba(25, 103, 210, 0.05));
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  position: relative;
  padding: 16px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #202124;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 12px;
  color: #5f6368;
}

.stat-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  width: 100%;
  background: linear-gradient(to right, #4285f4, #1967d2);
  transform: scaleX(0);
  transform-origin: left;
  animation: barGrow 1s forwards;
}

.stat-bar-success {
  background: linear-gradient(to right, #34a853, #0f9d58);
}

@keyframes barGrow {
  to { transform: scaleX(1); }
}

/* 响应式 */
@media (max-width: 1200px) {
  .qa-container {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    display: none;
  }
}
</style>
