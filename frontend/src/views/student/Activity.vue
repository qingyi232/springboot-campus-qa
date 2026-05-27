<template>
  <div class="student-activity">
    <div class="page-header">
      <h1>活动中心</h1>
      <p>参与精彩校园活动，丰富你的大学生活</p>
    </div>

    <!-- 筛选器 -->
    <div class="filter-bar">
      <el-radio-group v-model="type" @change="loadData">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="LECTURE">讲座</el-radio-button>
        <el-radio-button label="COMPETITION">竞赛</el-radio-button>
        <el-radio-button label="CULTURAL">文化活动</el-radio-button>
        <el-radio-button label="SPORTS">体育活动</el-radio-button>
      </el-radio-group>
      
      <el-input
        v-model="keyword"
        placeholder="搜索活动..."
        prefix-icon="el-icon-search"
        @input="handleSearch"
        style="width: 300px"
        clearable
      ></el-input>
    </div>

    <!-- 活动列表 -->
    <div class="activity-grid" v-loading="loading">
      <div v-for="activity in activityList" :key="activity.id" class="activity-card" @click="viewDetail(activity)">
        <div class="activity-header">
          <div class="activity-date">
            <span class="date-day">{{ getDay(activity.startTime) }}</span>
            <span class="date-month">{{ getMonth(activity.startTime) }}</span>
          </div>
          <img :src="activity.coverImage || `https://picsum.photos/500/300?random=${activity.id}`" :alt="activity.title">
        </div>
        <div class="activity-body">
          <span class="activity-type">{{ getActivityType(activity.type) }}</span>
          <h3>{{ activity.title }}</h3>
          <p class="activity-desc">{{ activity.description }}</p>
          <div class="activity-info">
            <div class="info-item">
              <i class="el-icon-location"></i>
              <span>{{ activity.location }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-time"></i>
              <span>{{ formatDateTime(activity.startTime) }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-user"></i>
              <span>{{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants }}</span>
            </div>
          </div>
          <el-button 
            v-if="!activity.hasRegistered" 
            type="primary" 
            size="small" 
            round 
            @click.stop="register(activity)"
          >
            立即报名
          </el-button>
          <el-tag v-else type="success" size="medium">
            <i class="el-icon-circle-check"></i> 已报名
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        @current-change="handlePageChange"
        :current-page="page.current"
        :page-size="page.size"
        :total="page.total"
        layout="prev, pager, next, total"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudentActivity',
  data() {
    return {
      type: '',
      keyword: '',
      loading: false,
      activityList: [],
      page: {
        current: 1,
        size: 12,
        total: 0
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await this.$axios.get('/activity/page', {
          params: {
            current: this.page.current,
            size: this.page.size,
            type: this.type,
            keyword: this.keyword,
            status: 1
          }
        })
        if (res.data) {
          this.activityList = res.data.records || []
          this.page.total = res.data.total || 0
          
          // 检查用户是否已报名每个活动
          await this.checkRegistrationStatus()
        }
      } catch (error) {
        console.error('加载活动失败:', error)
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    async checkRegistrationStatus() {
      // 检查当前用户是否已报名各个活动
      if (!this.$store.state.user || !this.$store.state.user.id) {
        return
      }
      
      try {
        // 批量查询当前用户的所有报名记录
        const res = await this.$axios.get('/activity-registration/page', {
          params: {
            current: 1,
            size: 1000, // 获取所有报名记录
            userId: this.$store.state.user.id
          }
        })
        
        if (res.code === 200 && res.data && res.data.records) {
          const registeredActivityIds = new Set(
            res.data.records.map(reg => reg.activityId)
          )
          
          // 标记已报名的活动
          this.activityList.forEach(activity => {
            activity.hasRegistered = registeredActivityIds.has(activity.id)
          })
        }
      } catch (error) {
        console.error('检查报名状态失败:', error)
      }
    },
    handlePageChange(page) {
      this.page.current = page
      this.loadData()
    },
    handleSearch() {
      this.page.current = 1
      this.loadData()
    },
    viewDetail(activity) {
      this.$router.push(`/student/activity/${activity.id}`).catch(() => {})
    },
    async register(activity) {
      // 报名活动
      try {
        const res = await this.$axios.post(`/activity/${activity.id}/register`)
        if (res.code === 200) {
          this.$message.success('报名成功，等待审核！')
          
          // 重新加载数据，从数据库获取最新状态
          await this.loadData()
        } else {
          this.$message.error(res.message || '报名失败')
        }
      } catch (error) {
        console.error('报名失败:', error)
        if (error.response && error.response.data && error.response.data.message) {
          this.$message.error(error.response.data.message)
        } else {
          this.$message.error('报名失败，请稍后重试')
        }
      }
    },
    getActivityType(type) {
      const map = {
        'LECTURE': '讲座',
        'COMPETITION': '竞赛',
        'CULTURAL': '文化活动',
        'SPORTS': '体育活动',
        'VOLUNTEER': '志愿活动',
        'OTHER': '其他'
      }
      return map[type] || type
    },
    getDay(date) {
      if (!date) return ''
      return new Date(date).getDate()
    },
    getMonth(date) {
      if (!date) return ''
      const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC']
      return months[new Date(date).getMonth()]
    },
    formatDateTime(date) {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.student-activity {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 36px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: #7f8c8d;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}

.activity-card {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.activity-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.15);
}

.activity-header {
  position: relative;
  height: 200px;
}

.activity-date {
  position: absolute;
  top: 15px;
  left: 15px;
  z-index: 10;
  background: white;
  border-radius: 12px;
  padding: 12px 18px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.date-day {
  display: block;
  font-size: 30px;
  font-weight: 700;
  color: #667eea;
  line-height: 1;
}

.date-month {
  display: block;
  font-size: 12px;
  color: #7f8c8d;
  margin-top: 5px;
  font-weight: 600;
}

.activity-header img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.activity-card:hover .activity-header img {
  transform: scale(1.1);
}

.activity-body {
  padding: 25px;
}

.activity-type {
  display: inline-block;
  padding: 5px 15px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin-bottom: 15px;
}

.activity-body h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-desc {
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #7f8c8d;
  font-size: 13px;
}

.info-item i {
  color: #667eea;
  font-size: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 30px 0;
}

/* 已报名标签样式 */
.el-tag {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 20px;
}

.el-tag.el-tag--success {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
  border: none;
  color: white;
}

.el-tag i {
  margin-right: 5px;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    gap: 15px;
  }
  
  .activity-grid {
    grid-template-columns: 1fr;
  }
}
</style>

