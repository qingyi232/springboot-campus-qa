<template>
  <div class="activity-detail" v-loading="loading">
    <div v-if="activity" class="detail-container">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/student/activity' }">活动中心</el-breadcrumb-item>
        <el-breadcrumb-item>{{ activity.title }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 活动头部 -->
      <div class="activity-header">
        <span class="status-tag" :class="'status-' + activity.status">
          {{ getStatusText(activity.status) }}
        </span>
        <h1 class="activity-title">{{ activity.title }}</h1>
        <div class="activity-meta">
          <span><i class="el-icon-location"></i> {{ activity.location }}</span>
          <span><i class="el-icon-date"></i> {{ formatDate(activity.startTime) }}</span>
          <span><i class="el-icon-user"></i> {{ activity.currentParticipants || 0 }} / {{ activity.maxParticipants }} 人</span>
        </div>
      </div>

      <!-- 活动封面 -->
      <div v-if="activity.coverImage" class="activity-cover">
        <img :src="activity.coverImage || `https://picsum.photos/1200/400?random=${activity.id}`" :alt="activity.title">
      </div>

      <!-- 活动信息卡片 -->
      <div class="info-cards">
        <div class="info-card">
          <i class="el-icon-time"></i>
          <div>
            <span class="label">活动时间</span>
            <span class="value">{{ formatDateTime(activity.startTime) }}</span>
          </div>
        </div>
        <div class="info-card">
          <i class="el-icon-location-outline"></i>
          <div>
            <span class="label">活动地点</span>
            <span class="value">{{ activity.location }}</span>
          </div>
        </div>
        <div class="info-card">
          <i class="el-icon-user-solid"></i>
          <div>
            <span class="label">参与人数</span>
            <span class="value">{{ activity.currentParticipants || 0 }} / {{ activity.maxParticipants }}</span>
          </div>
        </div>
        <div class="info-card">
          <i class="el-icon-phone-outline"></i>
          <div>
            <span class="label">联系方式</span>
            <span class="value">{{ activity.contactInfo || '暂无' }}</span>
          </div>
        </div>
      </div>

      <!-- 活动详情 -->
      <div class="activity-content">
        <h2>活动详情</h2>
        <div v-html="activity.description"></div>
      </div>

      <!-- 底部操作 -->
      <div class="activity-footer">
        <el-button type="primary" size="large" @click="handleRegister" v-if="activity.status === 1" :disabled="activity.currentParticipants >= activity.maxParticipants">
          <i class="el-icon-circle-check"></i> {{ activity.currentParticipants >= activity.maxParticipants ? '名额已满' : '立即报名' }}
        </el-button>
        <el-button @click="$router.back()">
          <i class="el-icon-back"></i> 返回列表
        </el-button>
      </div>
    </div>

    <!-- 404提示 -->
    <div v-else-if="!loading" class="not-found">
      <i class="el-icon-warning"></i>
      <h2>未找到该活动</h2>
      <el-button type="primary" @click="$router.push('/student/activity')">返回列表</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ActivityDetail',
  data() {
    return {
      loading: false,
      activity: null
    }
  },
  mounted() {
    this.loadDetail()
  },
  watch: {
    '$route.params.id'() {
      this.loadDetail()
    }
  },
  methods: {
    async loadDetail() {
      const id = this.$route.params.id
      if (!id) {
        this.$message.error('缺少活动ID')
        this.$router.push('/student/activity')
        return
      }

      this.loading = true
      try {
        const res = await this.$axios.get(`/activity/${id}`)
        if (res.code === 200 && res.data) {
          this.activity = res.data
        } else {
          this.$message.error(res.message || '加载失败')
          this.activity = null
        }
      } catch (error) {
        console.error('加载活动详情失败:', error)
        this.$message.error('加载失败')
        this.activity = null
      } finally {
        this.loading = false
      }
    },
    async handleRegister() {
      try {
        const res = await this.$axios.post(`/activity/${this.activity.id}/register`)
        if (res.code === 200) {
          this.$message.success('报名成功！')
          // 重新加载活动信息
          this.loadDetail()
        } else {
          this.$message.error(res.message || '报名失败')
        }
      } catch (error) {
        console.error('报名失败:', error)
        this.$message.error('报名失败，请稍后重试')
      }
    },
    getStatusText(status) {
      const map = {
        0: '草稿',
        1: '进行中',
        2: '已结束'
      }
      return map[status] || '未知'
    },
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return d.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },
    formatDateTime(date) {
      if (!date) return ''
      const d = new Date(date)
      return d.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.activity-detail {
  min-height: calc(100vh - 200px);
  padding: 30px 0;
}

.detail-container {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 20px;
  padding: 50px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.breadcrumb {
  margin-bottom: 30px;
  font-size: 14px;
}

.activity-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 2px solid #f0f0f0;
}

.status-tag {
  display: inline-block;
  padding: 8px 20px;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  margin-bottom: 20px;
}

.status-0 { background: #95a5a6; }
.status-1 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.status-2 { background: #7f8c8d; }

.activity-title {
  font-size: 36px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1.4;
  margin-bottom: 20px;
}

.activity-meta {
  display: flex;
  justify-content: center;
  gap: 30px;
  font-size: 14px;
  color: #7f8c8d;
}

.activity-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.activity-meta i {
  font-size: 16px;
}

.activity-cover {
  margin-bottom: 40px;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.activity-cover img {
  width: 100%;
  height: auto;
  display: block;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 25px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border-radius: 12px;
  border: 2px solid #667eea30;
  transition: all 0.3s ease;
}

.info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.2);
}

.info-card i {
  font-size: 32px;
  color: #667eea;
}

.info-card > div {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-card .label {
  font-size: 13px;
  color: #7f8c8d;
}

.info-card .value {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.activity-content {
  margin-bottom: 50px;
}

.activity-content h2 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.activity-content >>> p {
  margin-bottom: 20px;
  font-size: 16px;
  line-height: 2;
  color: #34495e;
  text-align: justify;
}

.activity-content >>> img {
  max-width: 100%;
  height: auto;
  margin: 30px 0;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.activity-content >>> ul,
.activity-content >>> ol {
  margin: 20px 0;
  padding-left: 30px;
}

.activity-content >>> li {
  margin-bottom: 10px;
  font-size: 16px;
  line-height: 2;
  color: #34495e;
}

.activity-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding-top: 30px;
  border-top: 2px solid #f0f0f0;
}

.not-found {
  text-align: center;
  padding: 100px 20px;
  color: #95a5a6;
}

.not-found i {
  font-size: 80px;
  margin-bottom: 20px;
}

.not-found h2 {
  font-size: 28px;
  margin-bottom: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-container {
    padding: 30px 20px;
  }

  .activity-title {
    font-size: 26px;
  }

  .activity-meta {
    flex-direction: column;
    gap: 10px;
  }

  .info-cards {
    grid-template-columns: 1fr;
  }
}
</style>


