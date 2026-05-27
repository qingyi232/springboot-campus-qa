<template>
  <div class="student-news">
    <div class="page-header">
      <h1>校园资讯</h1>
      <p>了解最新校园动态和通知公告</p>
    </div>

    <!-- 筛选器 -->
    <div class="filter-bar">
      <el-radio-group v-model="category" @change="loadData">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="NOTICE">通知公告</el-radio-button>
        <el-radio-button label="ACADEMIC">学术动态</el-radio-button>
        <el-radio-button label="SCHOOL_NEWS">校园新闻</el-radio-button>
        <el-radio-button label="HOT_TOPIC">热点话题</el-radio-button>
      </el-radio-group>
      
      <el-input
        v-model="keyword"
        placeholder="搜索资讯..."
        prefix-icon="el-icon-search"
        @input="handleSearch"
        style="width: 300px"
        clearable
      ></el-input>
    </div>

    <!-- 资讯列表 -->
    <div class="news-grid" v-loading="loading">
      <div v-for="news in newsList" :key="news.id" class="news-card" @click="viewDetail(news)">
        <div class="news-image">
          <img :src="news.coverImage || `https://picsum.photos/400/250?random=${news.id}`" :alt="news.title">
          <span class="news-badge" :class="'badge-' + news.category">{{ getCategoryText(news.category) }}</span>
          <span v-if="news.isTop" class="top-badge"><i class="el-icon-top"></i> 置顶</span>
        </div>
        <div class="news-content">
          <h3>{{ news.title }}</h3>
          <p class="news-summary">{{ news.summary }}</p>
          <div class="news-meta">
            <span><i class="el-icon-user"></i> {{ news.author }}</span>
            <span><i class="el-icon-view"></i> {{ news.viewCount }}</span>
            <span><i class="el-icon-time"></i> {{ formatDate(news.publishTime) }}</span>
          </div>
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
  name: 'StudentNews',
  data() {
    return {
      category: '',
      keyword: '',
      loading: false,
      newsList: [],
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
        const res = await this.$axios.get('/news/page', {
          params: {
            current: this.page.current,
            size: this.page.size,
            category: this.category,
            keyword: this.keyword,
            status: 1
          }
        })
        if (res.data) {
          this.newsList = res.data.records || []
          this.page.total = res.data.total || 0
        }
      } catch (error) {
        console.error('加载资讯失败:', error)
        this.$message.error('加载失败')
      } finally {
        this.loading = false
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
    viewDetail(news) {
      // 这里可以跳转到详情页或打开对话框
      this.$router.push(`/student/news/${news.id}`).catch(() => {})
    },
    getCategoryText(category) {
      const map = {
        'NOTICE': '通知公告',
        'ACADEMIC': '学术动态',
        'SCHOOL_NEWS': '校园新闻',
        'HOT_TOPIC': '热点话题'
      }
      return map[category] || category
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    }
  }
}
</script>

<style scoped>
.student-news {
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

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}

.news-card {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.news-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.15);
}

.news-image {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.news-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.news-card:hover .news-image img {
  transform: scale(1.1);
}

.news-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 6px 15px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.badge-NOTICE { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.badge-ACADEMIC { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.badge-SCHOOL_NEWS { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.badge-HOT_TOPIC { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }

.top-badge {
  position: absolute;
  top: 15px;
  left: 15px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  background: #ff4757;
  color: white;
}

.news-content {
  padding: 25px;
}

.news-content h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.news-summary {
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #95a5a6;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.news-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 30px 0;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    gap: 15px;
  }
  
  .news-grid {
    grid-template-columns: 1fr;
  }
}
</style>


