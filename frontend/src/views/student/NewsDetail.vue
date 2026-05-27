<template>
  <div class="news-detail" v-loading="loading">
    <div v-if="news" class="detail-container">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/student/news' }">校园资讯</el-breadcrumb-item>
        <el-breadcrumb-item>{{ news.title }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 文章头部 -->
      <div class="article-header">
        <span class="category-tag" :class="'tag-' + news.category">
          {{ getCategoryText(news.category) }}
        </span>
        <h1 class="article-title">{{ news.title }}</h1>
        <div class="article-meta">
          <span><i class="el-icon-user"></i> {{ news.author }}</span>
          <span><i class="el-icon-time"></i> {{ formatDate(news.publishTime) }}</span>
          <span><i class="el-icon-view"></i> {{ news.viewCount }} 次浏览</span>
        </div>
      </div>

      <!-- 封面图 -->
      <div v-if="news.coverImage" class="article-cover">
        <img :src="news.coverImage || `https://picsum.photos/1200/400?random=${news.id}`" :alt="news.title">
      </div>

      <!-- 摘要 -->
      <div v-if="news.summary" class="article-summary">
        <i class="el-icon-reading"></i>
        <span>{{ news.summary }}</span>
      </div>

      <!-- 正文内容 -->
      <div class="article-content" v-html="news.content"></div>

      <!-- 底部操作 -->
      <div class="article-footer">
        <el-button type="primary" @click="$router.back()">
          <i class="el-icon-back"></i> 返回列表
        </el-button>
      </div>
    </div>

    <!-- 404提示 -->
    <div v-else-if="!loading" class="not-found">
      <i class="el-icon-warning"></i>
      <h2>未找到该资讯</h2>
      <el-button type="primary" @click="$router.push('/student/news')">返回列表</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NewsDetail',
  data() {
    return {
      loading: false,
      news: null
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
        this.$message.error('缺少资讯ID')
        this.$router.push('/student/news')
        return
      }

      this.loading = true
      try {
        const res = await this.$axios.get(`/news/${id}`)
        if (res.code === 200 && res.data) {
          this.news = res.data
          // 后端已自动增加浏览量
        } else {
          this.$message.error(res.message || '加载失败')
          this.news = null
        }
      } catch (error) {
        console.error('加载资讯详情失败:', error)
        this.$message.error('加载失败')
        this.news = null
      } finally {
        this.loading = false
      }
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
.news-detail {
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

.article-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 2px solid #f0f0f0;
}

.category-tag {
  display: inline-block;
  padding: 8px 20px;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  margin-bottom: 20px;
}

.tag-NOTICE { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.tag-ACADEMIC { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.tag-SCHOOL_NEWS { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.tag-HOT_TOPIC { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }

.article-title {
  font-size: 36px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1.4;
  margin-bottom: 20px;
}

.article-meta {
  display: flex;
  justify-content: center;
  gap: 30px;
  font-size: 14px;
  color: #7f8c8d;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.article-meta i {
  font-size: 16px;
}

.article-cover {
  margin-bottom: 40px;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.article-cover img {
  width: 100%;
  height: auto;
  display: block;
}

.article-summary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 25px 30px;
  border-radius: 15px;
  margin-bottom: 40px;
  display: flex;
  align-items: flex-start;
  gap: 15px;
  font-size: 16px;
  line-height: 1.8;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.article-summary i {
  font-size: 24px;
  margin-top: 2px;
  flex-shrink: 0;
}

.article-content {
  font-size: 16px;
  line-height: 2;
  color: #34495e;
  margin-bottom: 50px;
}

/* 正文样式美化 */
.article-content >>> p {
  margin-bottom: 20px;
  text-align: justify;
}

.article-content >>> h1,
.article-content >>> h2,
.article-content >>> h3,
.article-content >>> h4 {
  margin: 30px 0 20px;
  font-weight: 600;
  color: #2c3e50;
}

.article-content >>> h1 { font-size: 28px; }
.article-content >>> h2 { font-size: 24px; }
.article-content >>> h3 { font-size: 20px; }
.article-content >>> h4 { font-size: 18px; }

.article-content >>> img {
  max-width: 100%;
  height: auto;
  margin: 30px 0;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.article-content >>> ul,
.article-content >>> ol {
  margin: 20px 0;
  padding-left: 30px;
}

.article-content >>> li {
  margin-bottom: 10px;
}

.article-content >>> blockquote {
  border-left: 4px solid #667eea;
  background: #f8f9fa;
  padding: 20px 25px;
  margin: 25px 0;
  border-radius: 8px;
  color: #555;
}

.article-content >>> a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.article-content >>> a:hover {
  text-decoration: underline;
}

.article-footer {
  text-align: center;
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

  .article-title {
    font-size: 26px;
  }

  .article-meta {
    flex-direction: column;
    gap: 10px;
  }

  .article-content {
    font-size: 15px;
  }
}
</style>

