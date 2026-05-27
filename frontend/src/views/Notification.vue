<template>
  <div class="notification-container">
    <el-card shadow="hover">
      <div slot="header" class="card-header">
        <span>消息通知</span>
        <el-button v-if="isAdmin" type="primary" icon="el-icon-plus" @click="handleAdd">
          发送通知
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="通知类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="系统通知" value="SYSTEM"></el-option>
            <el-option label="活动通知" value="ACTIVITY"></el-option>
            <el-option label="资讯通知" value="NEWS"></el-option>
            <el-option label="个人消息" value="PERSONAL"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="阅读状态" v-if="!isAdmin">
          <el-select v-model="searchForm.isRead" placeholder="请选择状态" clearable>
            <el-option label="未读" :value="0"></el-option>
            <el-option label="已读" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadData">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
          <el-button v-if="!isAdmin" type="success" icon="el-icon-check" @click="markAllAsRead">全部已读</el-button>
        </el-form-item>
      </el-form>

      <!-- 通知列表 -->
      <div class="notification-list">
        <div
          v-for="item in tableData"
          :key="item.id"
          :class="['notification-item', item.isRead === 0 ? 'unread' : '']"
          @click="handleRead(item)"
        >
          <div class="notification-header">
            <div class="notification-title">
              <el-badge :is-dot="item.isRead === 0" class="badge-dot">
                <i :class="getTypeIcon(item.type)" :style="{ color: getTypeColor(item.type) }"></i>
              </el-badge>
              <span class="title-text">{{ item.title }}</span>
              <el-tag :type="getTypeTagColor(item.type)" size="mini">
                {{ getTypeLabel(item.type) }}
              </el-tag>
              <el-tag v-if="item.priority === 1" type="danger" size="mini">重要</el-tag>
            </div>
            <div class="notification-time">{{ item.createTime }}</div>
          </div>
          <div class="notification-content">{{ item.content }}</div>
          <div v-if="isAdmin" class="notification-actions">
            <el-button type="text" size="small" @click.stop="handleDelete(item)">删除</el-button>
          </div>
        </div>
        <el-empty v-if="tableData.length === 0" description="暂无通知"></el-empty>
      </div>

      <!-- 分页 -->
      <el-pagination
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total"
        style="margin-top: 20px; text-align: center"
      ></el-pagination>
    </el-card>

    <!-- 发送通知对话框 -->
    <el-dialog title="发送通知" :visible.sync="dialogVisible" width="50%" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题"></el-input>
        </el-form-item>
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="系统通知" value="SYSTEM"></el-option>
            <el-option label="活动通知" value="ACTIVITY"></el-option>
            <el-option label="资讯通知" value="NEWS"></el-option>
            <el-option label="个人消息" value="PERSONAL"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input type="textarea" v-model="form.content" :rows="5" placeholder="请输入通知内容"></el-input>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio :label="0">普通</el-radio>
            <el-radio :label="1">重要</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="接收对象">
          <el-radio-group v-model="form.receiverType">
            <el-radio label="ALL">全部用户</el-radio>
            <el-radio label="STUDENT">仅学生</el-radio>
            <el-radio label="ADMIN">仅管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSend">发 送</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Notification',
  data() {
    return {
      loading: false,
      searchForm: {
        type: '',
        isRead: null
      },
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      form: {
        title: '',
        type: 'SYSTEM',
        content: '',
        priority: 0,
        receiverType: 'ALL'
      },
      rules: {
        title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
        type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
        content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['isAdmin', 'currentUser'])
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          current: this.currentPage,
          size: this.pageSize,
          ...this.searchForm
        }
        
        // 如果不是管理员，只查看自己的通知
        if (!this.isAdmin) {
          params.userId = this.currentUser.id
        }
        
        const res = await this.$axios.get('/notification/page', { params })
        this.tableData = res.data.records || []
        this.total = res.data.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadData()
    },
    handleReset() {
      this.searchForm = { type: '', isRead: null }
      this.currentPage = 1
      this.loadData()
    },
    handleAdd() {
      this.form = {
        title: '',
        type: 'SYSTEM',
        content: '',
        priority: 0,
        receiverType: 'ALL'
      }
      this.dialogVisible = true
    },
    async handleSend() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            await this.$axios.post('/notification', this.form)
            this.$message.success('发送成功')
            this.dialogVisible = false
            this.loadData()
          } catch (error) {
            console.error('发送失败:', error)
            this.$message.error('发送失败')
          }
        }
      })
    },
    async handleRead(item) {
      if (item.isRead === 0 && !this.isAdmin) {
        try {
          await this.$axios.put(`/notification/${item.id}/read`)
          item.isRead = 1
          this.$store.dispatch('loadUnreadCount')
        } catch (error) {
          console.error('标记已读失败:', error)
        }
      }
    },
    async markAllAsRead() {
      this.$confirm('确定要将所有通知标记为已读吗?', '提示', {
        type: 'info'
      }).then(async () => {
        try {
          const unreadList = this.tableData.filter(item => item.isRead === 0)
          for (const item of unreadList) {
            await this.$axios.put(`/notification/${item.id}/read`)
          }
          this.$message.success('操作成功')
          this.loadData()
          this.$store.dispatch('loadUnreadCount')
        } catch (error) {
          console.error('操作失败:', error)
        }
      }).catch(() => {})
    },
    handleDelete(item) {
      this.$confirm('确定要删除该通知吗?', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await this.$axios.delete(`/notification/${item.id}`)
          this.$message.success('删除成功')
          this.loadData()
        } catch (error) {
          console.error('删除失败:', error)
        }
      }).catch(() => {})
    },
    getTypeLabel(type) {
      const map = {
        SYSTEM: '系统通知',
        ACTIVITY: '活动通知',
        NEWS: '资讯通知',
        PERSONAL: '个人消息'
      }
      return map[type] || type
    },
    getTypeIcon(type) {
      const map = {
        SYSTEM: 'el-icon-warning',
        ACTIVITY: 'el-icon-trophy',
        NEWS: 'el-icon-news',
        PERSONAL: 'el-icon-message'
      }
      return map[type] || 'el-icon-bell'
    },
    getTypeColor(type) {
      const map = {
        SYSTEM: '#E6A23C',
        ACTIVITY: '#67C23A',
        NEWS: '#409EFF',
        PERSONAL: '#F56C6C'
      }
      return map[type] || '#909399'
    },
    getTypeTagColor(type) {
      const map = {
        SYSTEM: 'warning',
        ACTIVITY: 'success',
        NEWS: 'primary',
        PERSONAL: 'danger'
      }
      return map[type] || 'info'
    }
  },
  mounted() {
    this.loadData()
  }
}
</script>

<style scoped>
.notification-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.notification-list {
  margin-bottom: 20px;
}

.notification-item {
  padding: 15px;
  border: 1px solid #EBEEF5;
  border-radius: 5px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
}

.notification-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.notification-item.unread {
  background-color: #f0f9ff;
  border-left: 3px solid #409EFF;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.badge-dot {
  display: flex;
  align-items: center;
}

.badge-dot i {
  font-size: 20px;
}

.title-text {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.notification-time {
  color: #909399;
  font-size: 13px;
}

.notification-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 10px;
}

.notification-actions {
  text-align: right;
}
</style>


