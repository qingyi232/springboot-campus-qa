<template>
  <div class="my-registrations-container">
    <div class="page-header">
      <h2>我的报名</h2>
      <p>查看和管理您已报名的活动</p>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane label="待审核" name="pending"></el-tab-pane>
      <el-tab-pane label="已通过" name="approved"></el-tab-pane>
      <el-tab-pane label="已拒绝" name="rejected"></el-tab-pane>
    </el-tabs>

    <div v-loading="loading" class="registrations-list">
      <el-empty v-if="!loading && registrations.length === 0" description="暂无报名记录">
        <el-button type="primary" @click="goToActivities">去报名活动</el-button>
      </el-empty>

      <div v-else class="registration-cards">
        <el-card
          v-for="reg in registrations"
          :key="reg.id"
          class="registration-card"
          shadow="hover"
        >
          <div class="card-header">
            <div class="activity-title">
              <i class="el-icon-s-flag"></i>
              {{ reg.activityTitle || '活动标题' }}
            </div>
            <el-tag
              :type="getStatusType(reg.status)"
              size="small"
            >
              {{ getStatusText(reg.status) }}
            </el-tag>
          </div>

          <div class="card-content">
            <div class="info-item">
              <i class="el-icon-time"></i>
              <span class="label">报名时间：</span>
              <span>{{ formatDateTime(reg.createTime) }}</span>
            </div>
            <div class="info-item" v-if="reg.activityStartTime">
              <i class="el-icon-date"></i>
              <span class="label">活动时间：</span>
              <span>{{ formatDateTime(reg.activityStartTime) }}</span>
            </div>
            <div class="info-item" v-if="reg.activityLocation">
              <i class="el-icon-location"></i>
              <span class="label">活动地点：</span>
              <span>{{ reg.activityLocation }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-user"></i>
              <span class="label">报名人：</span>
              <span>{{ reg.userName }} ({{ reg.studentId }})</span>
            </div>
            <div class="info-item" v-if="reg.phone">
              <i class="el-icon-phone"></i>
              <span class="label">联系电话：</span>
              <span>{{ reg.phone }}</span>
            </div>
          </div>

          <div class="card-footer">
            <el-button
              type="primary"
              size="small"
              icon="el-icon-view"
              @click="viewActivity(reg.activityId)"
            >
              查看活动
            </el-button>
            <el-button
              v-if="reg.status === 0 || reg.status === 1"
              type="danger"
              size="small"
              icon="el-icon-delete"
              @click="cancelRegistration(reg)"
            >
              取消报名
            </el-button>
            <el-button
              v-if="reg.status === 1 && !reg.isCheckedIn"
              type="success"
              size="small"
              icon="el-icon-circle-check"
              @click="checkIn(reg)"
            >
              签到
            </el-button>
            <el-tag v-if="reg.isCheckedIn" type="success" size="small">
              <i class="el-icon-circle-check"></i> 已签到
            </el-tag>
          </div>
        </el-card>
      </div>

      <el-pagination
        v-if="total > 0"
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total"
        class="pagination"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'MyRegistrations',
  data() {
    return {
      loading: false,
      activeTab: 'all',
      registrations: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  computed: {
    ...mapState(['user']),
    statusFilter() {
      const map = {
        'all': null,
        'pending': 0,
        'approved': 1,
        'rejected': 2
      }
      return map[this.activeTab]
    }
  },
  mounted() {
    this.loadRegistrations()
  },
  methods: {
    async loadRegistrations() {
      if (!this.user || !this.user.id) {
        this.$message.error('请先登录')
        return
      }

      this.loading = true
      try {
        const res = await this.$axios.get('/activity-registration/page', {
          params: {
            current: this.currentPage,
            size: this.pageSize,
            userId: this.user.id,
            auditStatus: this.statusFilter
          }
        })

        if (res.code === 200 && res.data) {
          this.registrations = res.data.records || []
          this.total = res.data.total || 0
          
          // 获取活动详情
          await this.loadActivityDetails()
        } else {
          this.$message.error(res.message || '加载失败')
        }
      } catch (error) {
        console.error('加载报名记录失败:', error)
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    async loadActivityDetails() {
      // 批量获取活动详情
      const activityIds = [...new Set(this.registrations.map(r => r.activityId))]
      
      for (const id of activityIds) {
        try {
          const res = await this.$axios.get(`/activity/${id}`)
          if (res.code === 200 && res.data) {
            // 更新报名记录中的活动信息
            this.registrations.forEach(reg => {
              if (reg.activityId === id) {
                reg.activityTitle = res.data.title
                reg.activityStartTime = res.data.startTime
                reg.activityLocation = res.data.location
              }
            })
          }
        } catch (error) {
          console.error(`获取活动${id}详情失败:`, error)
        }
      }
    },
    handleTabClick() {
      this.currentPage = 1
      this.loadRegistrations()
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadRegistrations()
    },
    getStatusText(status) {
      const map = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝'
      }
      return map[status] || '未知'
    },
    getStatusType(status) {
      const map = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return map[status] || 'info'
    },
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      return new Date(dateTime).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    viewActivity(activityId) {
      this.$router.push(`/student/activity/${activityId}`)
    },
    cancelRegistration(reg) {
      this.$confirm('确定要取消报名吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          const res = await this.$axios.delete(`/activity-registration/${reg.id}`)
          if (res.code === 200) {
            this.$message.success('取消报名成功')
            this.loadRegistrations()
          } else {
            this.$message.error(res.message || '取消失败')
          }
        } catch (error) {
          console.error('取消报名失败:', error)
          this.$message.error('取消失败')
        }
      }).catch(() => {})
    },
    async checkIn(reg) {
      try {
        const res = await this.$axios.put(`/activity-registration/${reg.id}/checkin`)
        if (res.code === 200) {
          this.$message.success('签到成功')
          this.loadRegistrations()
        } else {
          this.$message.error(res.message || '签到失败')
        }
      } catch (error) {
        console.error('签到失败:', error)
        this.$message.error('签到失败')
      }
    },
    goToActivities() {
      this.$router.push('/student/activity')
    }
  }
}
</script>

<style scoped>
.my-registrations-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h2 {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-header p {
  font-size: 16px;
  color: #7f8c8d;
}

.el-tabs {
  margin-bottom: 20px;
}

.registrations-list {
  min-height: 400px;
}

.registration-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.registration-card {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.registration-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.activity-title {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.activity-title i {
  color: #409EFF;
  font-size: 20px;
}

.card-content {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.info-item i {
  color: #909399;
  font-size: 16px;
}

.info-item .label {
  font-weight: 500;
  color: #606266;
}

.card-footer {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .registration-cards {
    grid-template-columns: 1fr;
  }
}
</style>


