<template>
  <div class="home-container">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ statistics.userCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">活动总数</div>
              <div class="stat-value">{{ statistics.activityCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C">
              <i class="el-icon-news"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">资讯总数</div>
              <div class="stat-value">{{ statistics.newsCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">问答次数</div>
              <div class="stat-value">{{ statistics.qaCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 最新资讯 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>最新资讯</span>
            <el-button type="text" @click="$router.push('/admin/news')">更多 >></el-button>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="item in newsList"
              :key="item.id"
              :timestamp="item.publishTime"
              placement="top"
            >
              <el-link type="primary" @click="viewNews(item)">{{ item.title }}</el-link>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <!-- 最新活动 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>最新活动</span>
            <el-button type="text" @click="$router.push('/admin/activity')">更多 >></el-button>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="item in activityList"
              :key="item.id"
              :timestamp="item.startTime"
              placement="top"
            >
              <el-link type="primary" @click="viewActivity(item)">{{ item.title }}</el-link>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>快速问答</span>
          </div>
          <div class="quick-qa">
            <el-input
              v-model="question"
              placeholder="请输入您的问题..."
              @keyup.enter.native="askQuestion"
            >
              <el-button slot="append" icon="el-icon-search" @click="askQuestion">提问</el-button>
            </el-input>
            <div v-if="answer" class="answer-box">
              <div class="answer-label">回答：</div>
              <div class="answer-content">{{ answer }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      statistics: {
        userCount: 0,
        activityCount: 0,
        newsCount: 0,
        qaCount: 0
      },
      newsList: [],
      activityList: [],
      question: '',
      answer: ''
    }
  },
  methods: {
    async loadStatistics() {
      // 从后端API加载真实统计数据
      try {
        const res = await this.$axios.get('/statistics/home')
        if (res.code === 200 && res.data) {
          this.statistics = res.data
        } else {
          this.$message.error('加载统计数据失败')
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
        this.$message.error('加载统计数据失败')
      }
    },
    async loadNews() {
      try {
        const res = await this.$axios.get('/news/page', {
          params: { current: 1, size: 5, status: 1 }
        })
        this.newsList = res.data.records || []
      } catch (error) {
        console.error('加载资讯失败:', error)
      }
    },
    async loadActivities() {
      try {
        const res = await this.$axios.get('/activity/page', {
          params: { current: 1, size: 5, status: 1 }
        })
        this.activityList = res.data.records || []
      } catch (error) {
        console.error('加载活动失败:', error)
      }
    },
    async askQuestion() {
      if (!this.question.trim()) {
        this.$message.warning('请输入问题')
        return
      }
      try {
        const res = await this.$axios.post('/qa/ask', {
          question: this.question,
          userId: this.$store.state.user.id
        })
        this.answer = res.data.answer
      } catch (error) {
        console.error('提问失败:', error)
      }
    },
    viewNews(item) {
      this.$message.info('查看资讯: ' + item.title)
    },
    viewActivity(item) {
      this.$message.info('查看活动: ' + item.title)
    }
  },
  mounted() {
    this.loadStatistics()
    this.loadNews()
    this.loadActivities()
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: #fff;
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-qa {
  padding: 20px 0;
}

.answer-box {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 5px;
}

.answer-label {
  font-weight: bold;
  margin-bottom: 10px;
  color: #409EFF;
}

.answer-content {
  line-height: 1.8;
  color: #606266;
}
</style>

