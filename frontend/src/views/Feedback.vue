<template>
  <div class="feedback-management">
    <div class="page-header">
      <h2>
        <i class="el-icon-chat-line-round"></i>
        问题反馈管理
      </h2>
      <div class="header-actions">
        <el-button size="small" icon="el-icon-refresh" @click="loadFeedbackList">刷新</el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-panel">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="处理状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="待处理" value="pending"></el-option>
            <el-option label="处理中" value="processing"></el-option>
            <el-option label="已解决" value="resolved"></el-option>
            <el-option label="已驳回" value="rejected"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="反馈类型">
          <el-select v-model="filterForm.feedbackType" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="功能问题" value="功能问题"></el-option>
            <el-option label="内容建议" value="内容建议"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 反馈列表 -->
    <div class="content-panel">
      <el-table :data="feedbackList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="feedbackType" label="反馈类型" width="120">
          <template slot-scope="scope">
            <el-tag :type="getTypeColor(scope.row.feedbackType)" size="small">
              {{ scope.row.feedbackType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="反馈标题" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="username" label="反馈人" width="120"></el-table-column>
        <el-table-column prop="contact" label="联系方式" width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewDetail(scope.row)">查看</el-button>
            <el-button
              v-if="scope.row.status === 'pending' || scope.row.status === 'processing'"
              type="text"
              size="small"
              @click="handleFeedback(scope.row)"
            >
              处理
            </el-button>
            <el-button type="text" size="small" @click="deleteFeedback(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; text-align: right"
      ></el-pagination>
    </div>

    <!-- 详情对话框 -->
    <el-dialog title="反馈详情" :visible.sync="detailDialogVisible" width="700px">
      <div v-if="currentFeedback" class="feedback-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="反馈ID">{{ currentFeedback.id }}</el-descriptions-item>
          <el-descriptions-item label="反馈类型">
            <el-tag :type="getTypeColor(currentFeedback.feedbackType)" size="small">
              {{ currentFeedback.feedbackType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="反馈人">{{ currentFeedback.username }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ currentFeedback.contact || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{ currentFeedback.createTime }}</el-descriptions-item>
          <el-descriptions-item label="反馈标题" :span="2">{{ currentFeedback.title }}</el-descriptions-item>
          <el-descriptions-item label="反馈内容" :span="2">
            <div style="white-space: pre-wrap; line-height: 1.6">{{ currentFeedback.content }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusType(currentFeedback.status)" size="small">
              {{ getStatusText(currentFeedback.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentFeedback.handlerName || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentFeedback.reply" label="处理回复" :span="2">
            <div style="white-space: pre-wrap; line-height: 1.6; background: #f5f7fa; padding: 10px; border-radius: 4px">
              {{ currentFeedback.reply }}
            </div>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentFeedback.replyTime" label="回复时间" :span="2">
            {{ currentFeedback.replyTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog title="处理反馈" :visible.sync="handleDialogVisible" width="600px">
      <el-form :model="handleForm" :rules="handleRules" ref="handleForm" label-width="100px">
        <el-form-item label="处理状态" prop="status">
          <el-radio-group v-model="handleForm.status">
            <el-radio label="processing">处理中</el-radio>
            <el-radio label="resolved">已解决</el-radio>
            <el-radio label="rejected">已驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="回复内容" prop="reply">
          <el-input
            type="textarea"
            v-model="handleForm.reply"
            placeholder="请输入回复内容"
            :rows="6"
          ></el-input>
        </el-form-item>
      </el-form>
      
      <div slot="footer">
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getFeedbackPage, getFeedbackDetail, handleFeedback as handleFeedbackApi, deleteFeedback as deleteFeedbackApi } from '@/api/feedback'

export default {
  name: 'FeedbackManagement',
  data() {
    return {
      filterForm: {
        status: '',
        feedbackType: ''
      },
      feedbackList: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      detailDialogVisible: false,
      handleDialogVisible: false,
      currentFeedback: null,
      handleForm: {
        status: 'processing',
        reply: ''
      },
      handleRules: {
        status: [{ required: true, message: '请选择处理状态', trigger: 'change' }],
        reply: [
          { required: true, message: '请输入回复内容', trigger: 'blur' },
          { min: 10, message: '回复内容至少10个字符', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadFeedbackList()
  },
  methods: {
    async loadFeedbackList() {
      this.loading = true
      try {
        const res = await getFeedbackPage({
          current: this.currentPage,
          size: this.pageSize,
          status: this.filterForm.status || undefined,
          feedbackType: this.filterForm.feedbackType || undefined
        })
        
        if (res.code === 200 && res.data) {
          this.feedbackList = res.data.records || []
          this.total = res.data.total || 0
        }
      } catch (error) {
        console.error('加载反馈列表失败:', error)
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.loadFeedbackList()
    },
    handleReset() {
      this.filterForm = {
        status: '',
        feedbackType: ''
      }
      this.handleSearch()
    },
    async viewDetail(row) {
      try {
        const res = await getFeedbackDetail(row.id)
        if (res.code === 200 && res.data) {
          this.currentFeedback = res.data
          this.detailDialogVisible = true
        }
      } catch (error) {
        console.error('加载详情失败:', error)
        this.$message.error('加载详情失败')
      }
    },
    handleFeedback(row) {
      this.currentFeedback = row
      this.handleForm = {
        status: 'processing',
        reply: ''
      }
      this.handleDialogVisible = true
    },
    async submitHandle() {
      try {
        await this.$refs.handleForm.validate()
        
        await handleFeedbackApi(this.currentFeedback.id, this.handleForm)
        this.$message.success('处理成功')
        this.handleDialogVisible = false
        this.loadFeedbackList()
      } catch (error) {
        if (error !== false) {
          console.error('处理失败:', error)
          this.$message.error('处理失败')
        }
      }
    },
    async deleteFeedback(id) {
      try {
        await this.$confirm('确定要删除此反馈吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await deleteFeedbackApi(id)
        this.$message.success('删除成功')
        this.loadFeedbackList()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadFeedbackList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadFeedbackList()
    },
    getTypeColor(type) {
      const colors = {
        '功能问题': 'warning',
        '内容建议': 'success',
        '其他': 'info'
      }
      return colors[type] || 'info'
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
.feedback-management {
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

.filter-panel {
  background: white;
  padding: 20px 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.content-panel {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.feedback-detail {
  padding: 10px;
}
</style>


