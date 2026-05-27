<template>
  <div class="qa-container">
    <el-card shadow="hover">
      <div slot="header" class="card-header">
        <span>智能问答</span>
        <div>
          <el-button type="text" @click="clearChat" style="color: #F56C6C;">清空记录</el-button>
        </div>
      </div>

      <!-- 聊天窗口 -->
      <div class="chat-window" ref="chatWindow">
        <div v-for="(msg, index) in messages" :key="index" class="message-item">
          <div :class="['message-bubble', msg.type === 'user' ? 'user-message' : 'bot-message']">
            <div class="message-avatar">
              <i :class="msg.type === 'user' ? 'el-icon-user' : 'el-icon-service'"></i>
            </div>
            <div class="message-content">
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
        </div>
        
        <div v-if="loading" class="message-item">
          <div class="message-bubble bot-message">
            <div class="message-avatar">
              <i class="el-icon-service"></i>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="input-area">
        <el-input
          v-model="userInput"
          placeholder="请输入您的问题..."
          @keyup.enter.native="sendMessage"
          :disabled="loading"
        >
          <el-button slot="append" icon="el-icon-s-promotion" @click="sendMessage" :loading="loading">
            发送
          </el-button>
        </el-input>
      </div>

      <!-- 快捷问题 -->
      <div class="quick-questions">
        <div class="quick-title">快捷问题：</div>
        <el-tag
          v-for="(q, index) in quickQuestions"
          :key="index"
          @click="selectQuickQuestion(q)"
          class="quick-tag"
        >
          {{ q }}
        </el-tag>
      </div>
    </el-card>

    <!-- 对话历史对话框 -->
    <el-dialog title="对话历史" :visible.sync="showHistory" width="60%">
      <el-table :data="historyList" style="width: 100%">
        <el-table-column prop="userQuestion" label="问题" min-width="200"></el-table-column>
        <el-table-column prop="botAnswer" label="回答" min-width="300"></el-table-column>
        <el-table-column prop="createTime" label="时间" width="180"></el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="repeatQuestion(scope.row)">
              再次提问
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="10"
        layout="total, prev, pager, next"
        :total="total"
        style="margin-top: 20px; text-align: center"
      ></el-pagination>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'QA',
  data() {
    return {
      userInput: '',
      messages: [],
      loading: false,
      sessionId: '',
      showHistory: false,
      historyList: [],
      currentPage: 1,
      total: 0,
      quickQuestions: [
        '新生报到需要带什么材料?',
        '校园卡如何充值?',
        '图书馆开放时间是什么时候?',
        '如何申请奖学金?',
        '宿舍可以使用哪些电器?'
      ]
    }
  },
  methods: {
    async sendMessage() {
      if (!this.userInput.trim()) {
        this.$message.warning('请输入问题')
        return
      }

      // 添加用户消息
      this.messages.push({
        type: 'user',
        content: this.userInput,
        time: this.formatTime(new Date())
      })

      const question = this.userInput
      this.userInput = ''
      this.loading = true

      // 滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom()
      })

      try {
        const res = await this.$axios.post('/qa/ask', {
          question: question,
          sessionId: this.sessionId,
          userId: this.$store.state.user.id
        })

        // 添加机器人回复
        this.messages.push({
          type: 'bot',
          content: res.data.answer,
          time: this.formatTime(new Date())
        })

        if (res.data.sessionId) {
          this.sessionId = res.data.sessionId
        }
        
        // 保存聊天记录到localStorage
        this.saveChatHistory()
      } catch (error) {
        console.error('提问失败:', error)
        this.messages.push({
          type: 'bot',
          content: '抱歉，我暂时无法回答您的问题，请稍后再试。',
          time: this.formatTime(new Date())
        })
      } finally {
        this.loading = false
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }
    },
    selectQuickQuestion(question) {
      this.userInput = question
      this.sendMessage()
    },
    formatTime(date) {
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}:${minutes}`
    },
    scrollToBottom() {
      const chatWindow = this.$refs.chatWindow
      if (chatWindow) {
        chatWindow.scrollTop = chatWindow.scrollHeight
      }
    },
    async loadHistory() {
      try {
        const res = await this.$axios.get('/qa/conversation/page', {
          params: {
            current: this.currentPage,
            size: 10,
            userId: this.$store.state.user.id
          }
        })
        this.historyList = res.data.records || []
        this.total = res.data.total || 0
      } catch (error) {
        console.error('加载历史记录失败:', error)
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadHistory()
    },
    repeatQuestion(row) {
      this.userInput = row.userQuestion
      this.showHistory = false
      this.sendMessage()
    },
    loadChatHistory() {
      // 从localStorage加载该用户的聊天记录（永久保存）
      try {
        const user = this.$store.state.user
        if (!user || !user.id) return
        
        const historyKey = `admin_qa_chat_history_${user.id}`
        const saved = localStorage.getItem(historyKey)
        if (saved) {
          const data = JSON.parse(saved)
          if (data.messages && Array.isArray(data.messages)) {
            this.messages = data.messages
            this.$nextTick(() => {
              this.scrollToBottom()
            })
          }
          if (data.sessionId) {
            this.sessionId = data.sessionId
          }
        }
      } catch (e) {
        console.error('加载聊天历史失败:', e)
      }
    },
    saveChatHistory() {
      // 保存该用户的聊天记录到localStorage（永久保存）
      try {
        const user = this.$store.state.user
        if (!user || !user.id) return
        
        const historyKey = `admin_qa_chat_history_${user.id}`
        const data = {
          userId: user.id,
          username: user.username,
          lastUpdate: new Date().toISOString(),
          messages: this.messages,
          sessionId: this.sessionId
        }
        localStorage.setItem(historyKey, JSON.stringify(data))
        console.log('✅ 管理员聊天记录已保存')
      } catch (e) {
        console.error('保存聊天历史失败:', e)
      }
    },
    clearChat() {
      this.$confirm('确定要清空对话记录吗?', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(() => {
        this.messages = []
        // 清除该用户的localStorage
        const user = this.$store.state.user
        if (user && user.id) {
          const historyKey = `admin_qa_chat_history_${user.id}`
          localStorage.removeItem(historyKey)
        }
        // 重新生成sessionId
        this.sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
        // 添加欢迎消息
        this.messages.push({
          type: 'bot',
          content: '您好！我是校园智能问答助手，有什么可以帮助您的吗？',
          time: this.formatTime(new Date())
        })
        this.saveChatHistory()
        this.$message.success('已清空对话记录')
      }).catch(() => {})
    }
  },
  mounted() {
    // 等待用户信息加载完成后再初始化
    this.$nextTick(() => {
      const user = this.$store.state.user
      if (user && user.id) {
        // 为当前用户生成或恢复sessionId
        const sessionKey = `admin_qa_session_id_${user.id}`
        const savedSessionId = localStorage.getItem(sessionKey)
        if (savedSessionId) {
          this.sessionId = savedSessionId
        } else {
          this.sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
          localStorage.setItem(sessionKey, this.sessionId)
        }
        
        // 加载该用户的历史聊天记录
        this.loadChatHistory()
        
        // 如果没有历史记录，添加欢迎消息
        if (this.messages.length === 0) {
          this.messages.push({
            type: 'bot',
            content: '您好！我是校园智能问答助手，有什么可以帮助您的吗？',
            time: this.formatTime(new Date())
          })
          this.saveChatHistory()
        }
      }
    })
  },
  beforeDestroy() {
    // 保存聊天记录到localStorage
    this.saveChatHistory()
  }
}
</script>

<style scoped>
.qa-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-window {
  height: 500px;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 5px;
  margin-bottom: 20px;
}

.message-item {
  margin-bottom: 20px;
}

.message-bubble {
  display: flex;
  align-items: flex-start;
  max-width: 80%;
}

.user-message {
  flex-direction: row-reverse;
  margin-left: auto;
}

.bot-message {
  flex-direction: row;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  flex-shrink: 0;
}

.user-message .message-avatar {
  background-color: #409EFF;
  margin-left: 10px;
}

.bot-message .message-avatar {
  background-color: #67C23A;
  margin-right: 10px;
}

.message-content {
  flex: 1;
}

.message-text {
  padding: 10px 15px;
  border-radius: 10px;
  background-color: #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  line-height: 1.6;
}

.user-message .message-text {
  background-color: #409EFF;
  color: #fff;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  text-align: right;
}

.user-message .message-time {
  text-align: left;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 10px 15px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #909399;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

.input-area {
  margin-bottom: 20px;
}

.quick-questions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-title {
  font-weight: bold;
  color: #606266;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.quick-tag:hover {
  transform: scale(1.05);
}
</style>

