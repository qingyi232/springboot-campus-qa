<template>
  <div class="student-feedback">
    <!-- 科技感背景 -->
    <div class="tech-bg">
      <div class="circuit-pattern"></div>
    </div>
    
    <div class="feedback-container">
      <!-- 提交反馈区域 -->
      <div class="submit-section glass-panel">
        <div class="section-header">
          <i class="el-icon-edit-outline"></i>
          <span>提交问题反馈</span>
        </div>
        
        <el-form :model="feedbackForm" :rules="rules" ref="feedbackForm" label-width="100px">
          <el-form-item label="反馈类型" prop="feedbackType">
            <el-select v-model="feedbackForm.feedbackType" placeholder="请选择反馈类型" style="width: 100%">
              <el-option label="功能问题" value="功能问题"></el-option>
              <el-option label="内容建议" value="内容建议"></el-option>
              <el-option label="其他" value="其他"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="反馈标题" prop="title">
            <el-input v-model="feedbackForm.title" placeholder="请简要描述问题"></el-input>
          </el-form-item>
          
          <el-form-item label="反馈内容" prop="content">
            <el-input
              type="textarea"
              v-model="feedbackForm.content"
              placeholder="请详细描述问题或建议"
              :rows="6"
            ></el-input>
          </el-form-item>
          
          <el-form-item label="联系方式" prop="contact">
            <el-input v-model="feedbackForm.contact" placeholder="手机号或邮箱（选填）"></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitFeedback" :loading="submitting">
              <i class="el-icon-s-promotion"></i> 提交反馈
            </el-button>
            <el-button @click="resetForm">
              <i class="el-icon-refresh"></i> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 我的反馈列表 -->
      <div class="list-section glass-panel">
        <div class="section-header">
          <i class="el-icon-document"></i>
          <span>我的反馈记录</span>
        </div>
        
        <div class="feedback-list">
          <div
            v-for="item in feedbackList"
            :key="item.id"
            class="feedback-item"
            @click="viewDetail(item)"
          >
            <div class="item-header">
              <div class="item-title">
                <i :class="getTypeIcon(item.feedbackType)"></i>
                {{ item.title }}
              </div>
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
            <div class="item-content">
              {{ item.content }}
            </div>
            <div class="item-footer">
              <span class="item-time">
                <i class="el-icon-time"></i> {{ item.createTime }}
              </span>
              <span v-if="item.reply" class="has-reply">
                <i class="el-icon-chat-line-round"></i> 已回复
              </span>
            </div>
          </div>
          
          <el-empty v-if="feedbackList.length === 0" description="暂无反馈记录"></el-empty>
        </div>
        
        <el-pagination
          v-if="total > 0"
          @current-change="handlePageChange"
          :current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          style="margin-top: 20px; text-align: center"
        ></el-pagination>
      </div>
    </div>
    
    <!-- 反馈详情对话框 -->
    <el-dialog title="反馈详情" :visible.sync="detailDialogVisible" width="600px">
      <div v-if="currentFeedback" class="feedback-detail">
        <div class="detail-item">
          <div class="detail-label">反馈类型</div>
          <div class="detail-value">{{ currentFeedback.feedbackType }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">反馈标题</div>
          <div class="detail-value">{{ currentFeedback.title }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">反馈内容</div>
          <div class="detail-value">{{ currentFeedback.content }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">提交时间</div>
          <div class="detail-value">{{ currentFeedback.createTime }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">处理状态</div>
          <div class="detail-value">
            <el-tag :type="getStatusType(currentFeedback.status)" size="small">
              {{ getStatusText(currentFeedback.status) }}
            </el-tag>
          </div>
        </div>
        <div v-if="currentFeedback.reply" class="detail-item reply-section">
          <div class="detail-label">处理回复</div>
          <div class="detail-value reply-content">
            <div class="reply-info">
              <span>{{ currentFeedback.handlerName }}</span>
              <span class="reply-time">{{ currentFeedback.replyTime }}</span>
            </div>
            <div class="reply-text">{{ currentFeedback.reply }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { submitFeedback, getMyFeedback, getFeedbackDetail } from '@/api/feedback'

export default {
  name: 'StudentFeedback',
  data() {
    return {
      feedbackForm: {
        feedbackType: '',
        title: '',
        content: '',
        contact: ''
      },
      rules: {
        feedbackType: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
        title: [
          { required: true, message: '请输入反馈标题', trigger: 'blur' },
          { min: 5, max: 200, message: '标题长度在 5 到 200 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入反馈内容', trigger: 'blur' },
          { min: 10, message: '内容至少 10 个字符', trigger: 'blur' }
        ]
      },
      submitting: false,
      feedbackList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      detailDialogVisible: false,
      currentFeedback: null
    }
  },
  mounted() {
    this.loadFeedbackList()
  },
  methods: {
    async submitFeedback() {
      try {
        await this.$refs.feedbackForm.validate()
        
        this.submitting = true
        const res = await submitFeedback(this.feedbackForm)
        
        if (res.code === 200) {
          this.$message.success('提交成功！我们会尽快处理您的反馈')
          this.resetForm()
          this.loadFeedbackList()
        } else {
          this.$message.error(res.message || '提交失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        this.submitting = false
      }
    },
    resetForm() {
      this.$refs.feedbackForm.resetFields()
    },
    async loadFeedbackList() {
      try {
        const res = await getMyFeedback({
          current: this.currentPage,
          size: this.pageSize
        })
        
        if (res.code === 200 && res.data) {
          this.feedbackList = res.data.records || []
          this.total = res.data.total || 0
        }
      } catch (error) {
        console.error('加载反馈列表失败:', error)
      }
    },
    async viewDetail(item) {
      try {
        const res = await getFeedbackDetail(item.id)
        if (res.code === 200 && res.data) {
          this.currentFeedback = res.data
          this.detailDialogVisible = true
        }
      } catch (error) {
        console.error('加载详情失败:', error)
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadFeedbackList()
    },
    getTypeIcon(type) {
      const icons = {
        '功能问题': 'el-icon-warning',
        '内容建议': 'el-icon-star-off',
        '其他': 'el-icon-question'
      }
      return icons[type] || 'el-icon-document'
    },
    getStatusType(status) {
      const types = {
        'pending': 'warning',
        'processing': 'primary',
        'resolved': 'success',
        'rejected': 'info'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'pending': '待处理',
        'processing': '处理中',
        'resolved': '已解决',
        'rejected': '已驳回'
      }
      return texts[status] || status
    }
  }
}
</script>

<style scoped>
.student-feedback {
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

.feedback-container {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px;
  display: flex;
  gap: 30px;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 30px;
}

.submit-section {
  width: 500px;
  flex-shrink: 0;
}

.list-section {
  flex: 1;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid rgba(0, 0, 0, 0.05);
}

.section-header i {
  font-size: 22px;
  color: #667eea;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  min-height: 400px;
}

.feedback-item {
  padding: 20px;
  background: rgba(102, 126, 234, 0.03);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.feedback-item:hover {
  background: rgba(102, 126, 234, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-title i {
  color: #667eea;
}

.item-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.item-time,
.has-reply {
  display: flex;
  align-items: center;
  gap: 5px;
}

.has-reply {
  color: #67C23A;
}

.feedback-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-label {
  font-size: 13px;
  color: #909399;
  font-weight: 600;
}

.detail-value {
  font-size: 14px;
  color: #2c3e50;
  line-height: 1.6;
}

.reply-section {
  padding: 20px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.reply-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reply-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.reply-info span:first-child {
  font-weight: 600;
  color: #667eea;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}

.reply-text {
  white-space: pre-wrap;
  line-height: 1.8;
}
</style>


