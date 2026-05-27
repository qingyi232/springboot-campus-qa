<template>
  <div class="student-home">
    <!-- Hero区域 - 科技感设计 -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="grid-overlay"></div>
        <div class="gradient-overlay"></div>
      </div>
      <el-carousel height="420px" :interval="6000" arrow="never" indicator-position="none">
        <el-carousel-item v-for="(banner, index) in banners" :key="index">
          <div class="hero-content">
            <div class="hero-badge">{{ banner.tag }}</div>
            <h1 class="hero-title">{{ banner.title }}</h1>
            <p class="hero-subtitle">{{ banner.subtitle }}</p>
            <button class="hero-button" @click="banner.action">
              {{ banner.btnText }}
              <i class="el-icon-right"></i>
            </button>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 快速访问 - 玻璃拟态卡片 -->
    <section class="feature-section">
      <div class="feature-grid">
        <div class="feature-card" @click="goTo('/student/qa')">
          <div class="card-glow card-glow-blue"></div>
          <div class="card-icon icon-blue">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <h3>智能问答</h3>
          <p>AI驱动的智能助手</p>
          <div class="card-arrow">→</div>
        </div>
        <div class="feature-card" @click="goTo('/student/news')">
          <div class="card-glow card-glow-red"></div>
          <div class="card-icon icon-red">
            <i class="el-icon-news"></i>
          </div>
          <h3>校园资讯</h3>
          <p>最新校园动态</p>
          <div class="card-arrow">→</div>
        </div>
        <div class="feature-card" @click="goTo('/student/activity')">
          <div class="card-glow card-glow-yellow"></div>
          <div class="card-icon icon-yellow">
            <i class="el-icon-trophy"></i>
          </div>
          <h3>活动中心</h3>
          <p>精彩校园活动</p>
          <div class="card-arrow">→</div>
        </div>
        <div class="feature-card" @click="goTo('/student/content')">
          <div class="card-glow card-glow-green"></div>
          <div class="card-icon icon-green">
            <i class="el-icon-reading"></i>
          </div>
          <h3>内容中心</h3>
          <p>学习资料文档</p>
          <div class="card-arrow">→</div>
        </div>
      </div>
    </section>

    <!-- 最新资讯 -->
    <section class="content-section">
      <div class="section-header">
        <div class="header-line"></div>
        <h2>最新资讯</h2>
        <a @click="goTo('/student/news')" class="header-link">
          查看全部 <i class="el-icon-arrow-right"></i>
        </a>
      </div>
      <div class="news-grid" v-loading="loading">
        <div v-for="news in newsList" :key="news.id" class="news-card glass-card" @click="viewNews(news)">
          <div class="news-image">
            <img :src="news.coverImage || `https://picsum.photos/400/250?random=${news.id}`" :alt="news.title" loading="lazy">
            <div class="image-overlay"></div>
          </div>
          <div class="news-body">
            <span class="news-tag">{{ getCategoryText(news.category) }}</span>
            <h3>{{ news.title }}</h3>
            <p>{{ news.summary }}</p>
            <div class="news-footer">
              <span><i class="el-icon-user"></i> {{ news.author }}</span>
              <span><i class="el-icon-time"></i> {{ formatDate(news.publishTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门活动 -->
    <section class="content-section">
      <div class="section-header">
        <div class="header-line"></div>
        <h2>热门活动</h2>
        <a @click="goTo('/student/activity')" class="header-link">
          查看全部 <i class="el-icon-arrow-right"></i>
        </a>
      </div>
      <div class="activity-grid" v-loading="loading">
        <div v-for="activity in activityList" :key="activity.id" class="activity-card glass-card" @click="viewActivity(activity)">
          <div class="activity-image">
            <img :src="activity.coverImage || `https://picsum.photos/500/280?random=${activity.id}`" :alt="activity.title" loading="lazy">
            <div class="activity-badge">
              <div class="badge-date">{{ getDay(activity.startTime) }}</div>
              <div class="badge-month">{{ getMonth(activity.startTime) }}</div>
            </div>
          </div>
          <div class="activity-body">
            <span class="activity-tag">{{ getActivityType(activity.type) }}</span>
            <h3>{{ activity.title }}</h3>
            <div class="activity-meta">
              <span><i class="el-icon-location-outline"></i> {{ activity.location }}</span>
              <span><i class="el-icon-user"></i> {{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据统计 - 科技感设计 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-card" data-color="blue">
          <div class="stat-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-data">
            <div class="stat-number">{{ stats.userCount }}</div>
            <div class="stat-label">注册用户</div>
          </div>
          <div class="stat-line"></div>
        </div>
        <div class="stat-card" data-color="yellow">
          <div class="stat-icon">
            <i class="el-icon-trophy"></i>
          </div>
          <div class="stat-data">
            <div class="stat-number">{{ stats.activityCount }}</div>
            <div class="stat-label">精彩活动</div>
          </div>
          <div class="stat-line"></div>
        </div>
        <div class="stat-card" data-color="red">
          <div class="stat-icon">
            <i class="el-icon-news"></i>
          </div>
          <div class="stat-data">
            <div class="stat-number">{{ stats.newsCount }}</div>
            <div class="stat-label">校园资讯</div>
          </div>
          <div class="stat-line"></div>
        </div>
        <div class="stat-card" data-color="green">
          <div class="stat-icon">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <div class="stat-data">
            <div class="stat-number">{{ stats.qaCount }}</div>
            <div class="stat-label">问答次数</div>
          </div>
          <div class="stat-line"></div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'StudentHome',
  data() {
    return {
      banners: [
        {
          tag: 'AI POWERED',
          title: '校园智能问答系统',
          subtitle: '基于深度学习的智能问答引擎，提供24小时在线服务',
          btnText: '开始体验',
          action: () => this.goTo('/student/qa')
        },
        {
          tag: 'LATEST NEWS',
          title: '探索校园最新动态',
          subtitle: '实时更新的校园资讯，让您不错过任何重要信息',
          btnText: '查看资讯',
          action: () => this.goTo('/student/news')
        },
        {
          tag: 'ACTIVITIES',
          title: '精彩校园活动',
          subtitle: '丰富多彩的校园活动，充实您的大学生活',
          btnText: '立即参与',
          action: () => this.goTo('/student/activity')
        }
      ],
      newsList: [],
      activityList: [],
      stats: {
        userCount: 0,
        activityCount: 0,
        newsCount: 0,
        qaCount: 0
      },
      loading: false
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadNews(),
          this.loadActivities()
        ])
        this.loadStats()
      } finally {
        this.loading = false
      }
    },
    async loadNews() {
      try {
        const res = await this.$axios.get('/news/page', {
          params: { current: 1, size: 6, status: 1 }
        })
        if (res.data && res.data.records) {
          this.newsList = res.data.records
        }
      } catch (error) {
        console.error('加载资讯失败:', error)
      }
    },
    async loadActivities() {
      try {
        const res = await this.$axios.get('/activity/page', {
          params: { current: 1, size: 4, status: 1 }
        })
        if (res.data && res.data.records) {
          this.activityList = res.data.records
        }
      } catch (error) {
        console.error('加载活动失败:', error)
      }
    },
    async loadStats() {
      try {
        const res = await this.$axios.get('/statistics/home')
        if (res.data) {
          this.stats = res.data
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
        this.stats = {
          userCount: 0,
          activityCount: 0,
          newsCount: 0,
          qaCount: 0
        }
      }
    },
    goTo(path) {
      if (this.$route.path !== path) {
        this.$router.push(path).catch(() => {})
      }
    },
    viewNews(news) {
      this.$router.push(`/student/news/${news.id}`).catch(() => {})
    },
    viewActivity(activity) {
      this.$router.push(`/student/activity/${activity.id}`).catch(() => {})
    },
    getCategoryText(category) {
      const map = {
        'NOTICE': '通知',
        'ACADEMIC': '学术',
        'SCHOOL_NEWS': '新闻',
        'HOT_TOPIC': '热点'
      }
      return map[category] || category
    },
    getActivityType(type) {
      const map = {
        'LECTURE': '讲座',
        'COMPETITION': '竞赛',
        'CULTURAL': '文化',
        'SPORTS': '体育',
        'VOLUNTEER': '志愿',
        'OTHER': '其他'
      }
      return map[type] || type
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
    },
    getDay(date) {
      if (!date) return ''
      return new Date(date).getDate()
    },
    getMonth(date) {
      if (!date) return ''
      const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
      return months[new Date(date).getMonth()]
    }
  }
}
</script>

<style scoped>
.student-home {
  padding-bottom: 0;
}

/* Hero区域 - 科技感背景（亮色主题） */
.hero-section {
  position: relative;
  margin: -30px -30px 60px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #e3f2fd 0%, #f5f7fa 50%, #ffffff 100%);
}

.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(66, 153, 225, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(66, 153, 225, 0.08) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 50%, rgba(66, 153, 225, 0.12) 0%, transparent 50%),
              radial-gradient(circle at 80% 50%, rgba(99, 179, 237, 0.12) 0%, transparent 50%);
}

.hero-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  color: #2d3748;
  padding: 60px 40px;
}

.hero-badge {
  display: inline-block;
  padding: 8px 20px;
  background: rgba(66, 153, 225, 0.12);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(66, 153, 225, 0.25);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  margin-bottom: 24px;
  text-transform: uppercase;
  color: #2b6cb0;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 20px;
  line-height: 1.2;
  letter-spacing: -1px;
  background: linear-gradient(to right, #2b6cb0, #4299e1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 18px;
  margin: 0 0 36px;
  color: #4a5568;
  max-width: 600px;
  line-height: 1.6;
}

.hero-button {
  padding: 14px 36px;
  background: linear-gradient(135deg, #4299e1, #3182ce);
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 8px 24px rgba(66, 153, 225, 0.3);
}

.hero-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(66, 153, 225, 0.4);
}

/* 快速访问 - 玻璃拟态卡片 */
.feature-section {
  margin-bottom: 70px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
}

.feature-card {
  position: relative;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  padding: 36px 28px;
  text-align: center;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.feature-card:hover::before {
  left: 100%;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
  border-color: rgba(255, 255, 255, 0.5);
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  opacity: 0;
  transition: opacity 0.3s;
}

.feature-card:hover .card-glow {
  opacity: 0.15;
}

.card-glow-blue { background: radial-gradient(circle, #4285f4 0%, transparent 70%); }
.card-glow-red { background: radial-gradient(circle, #ea4335 0%, transparent 70%); }
.card-glow-yellow { background: radial-gradient(circle, #fbbc04 0%, transparent 70%); }
.card-glow-green { background: radial-gradient(circle, #34a853 0%, transparent 70%); }

.card-icon {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 32px;
  color: white;
  position: relative;
  z-index: 1;
}

.icon-blue { background: linear-gradient(135deg, #4285f4, #1967d2); }
.icon-red { background: linear-gradient(135deg, #ea4335, #c5221f); }
.icon-yellow { background: linear-gradient(135deg, #fbbc04, #f9ab00); }
.icon-green { background: linear-gradient(135deg, #34a853, #0f9d58); }

.feature-card h3 {
  font-size: 20px;
  font-weight: 600;
  color: #202124;
  margin: 0 0 8px;
}

.feature-card p {
  font-size: 14px;
  color: #5f6368;
  margin: 0 0 16px;
}

.card-arrow {
  font-size: 24px;
  color: #1967d2;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s;
}

.feature-card:hover .card-arrow {
  opacity: 1;
  transform: translateX(0);
}

/* 内容区域 */
.content-section {
  margin-bottom: 70px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}

.header-line {
  width: 4px;
  height: 32px;
  background: linear-gradient(to bottom, #4285f4, #1967d2);
  border-radius: 2px;
}

.section-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #202124;
  margin: 0;
  flex: 1;
  letter-spacing: -0.5px;
}

.header-link {
  font-size: 14px;
  color: #1967d2;
  text-decoration: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  transition: gap 0.2s;
}

.header-link:hover {
  gap: 10px;
}

/* 玻璃拟态卡片通用样式 */
.glass-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.12);
  border-color: rgba(255, 255, 255, 0.8);
}

/* 资讯卡片 */
.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 28px;
}

.news-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.news-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.news-card:hover .news-image img {
  transform: scale(1.08);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.4));
}

.news-body {
  padding: 24px;
}

.news-tag {
  display: inline-block;
  padding: 6px 14px;
  background: linear-gradient(135deg, #e8f0fe, #d2e3fc);
  color: #1967d2;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 14px;
}

.news-body h3 {
  font-size: 17px;
  font-weight: 600;
  color: #202124;
  margin: 0 0 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-body p {
  color: #5f6368;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-footer {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #80868b;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.news-footer span {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 活动卡片 */
.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 28px;
}

.activity-image {
  position: relative;
  height: 190px;
}

.activity-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.activity-card:hover .activity-image img {
  transform: scale(1.08);
}

.activity-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.badge-date {
  font-size: 24px;
  font-weight: 700;
  color: #1967d2;
  line-height: 1;
}

.badge-month {
  font-size: 11px;
  color: #5f6368;
  margin-top: 4px;
  font-weight: 500;
}

.activity-body {
  padding: 24px;
}

.activity-tag {
  display: inline-block;
  padding: 6px 14px;
  background: rgba(25, 103, 210, 0.08);
  color: #1967d2;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 14px;
}

.activity-body h3 {
  font-size: 17px;
  font-weight: 600;
  color: #202124;
  margin: 0 0 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13px;
  color: #5f6368;
}

.activity-meta span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.activity-meta i {
  color: #1967d2;
  font-size: 15px;
}

/* 数据统计 - 科技感设计 */
.stats-section {
  margin-bottom: 50px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
}

.stat-card {
  position: relative;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  padding: 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 0;
  transition: height 0.3s;
}

.stat-card[data-color="blue"]::before { background: #4285f4; }
.stat-card[data-color="yellow"]::before { background: #fbbc04; }
.stat-card[data-color="red"]::before { background: #ea4335; }
.stat-card[data-color="green"]::before { background: #34a853; }

.stat-card:hover::before {
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  transition: transform 0.3s;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
}

.stat-card[data-color="blue"] .stat-icon { background: rgba(66, 133, 244, 0.1); color: #4285f4; }
.stat-card[data-color="yellow"] .stat-icon { background: rgba(251, 188, 4, 0.1); color: #fbbc04; }
.stat-card[data-color="red"] .stat-icon { background: rgba(234, 67, 53, 0.1); color: #ea4335; }
.stat-card[data-color="green"] .stat-icon { background: rgba(52, 168, 83, 0.1); color: #34a853; }

.stat-data {
  flex: 1;
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: #202124;
  line-height: 1;
  margin-bottom: 8px;
  font-family: 'Segoe UI', Roboto, sans-serif;
}

.stat-label {
  font-size: 14px;
  color: #5f6368;
  font-weight: 500;
}

.stat-line {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  transition: width 0.5s;
}

.stat-card[data-color="blue"] .stat-line { background: linear-gradient(to right, #4285f4, #1967d2); }
.stat-card[data-color="yellow"] .stat-line { background: linear-gradient(to right, #fbbc04, #f9ab00); }
.stat-card[data-color="red"] .stat-line { background: linear-gradient(to right, #ea4335, #c5221f); }
.stat-card[data-color="green"] .stat-line { background: linear-gradient(to right, #34a853, #0f9d58); }

.stat-card:hover .stat-line {
  width: 100%;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }
  
  .news-grid,
  .activity-grid,
  .feature-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
