<template>
  <div class="news-container">
    <el-card shadow="hover">
      <div slot="header" class="card-header">
        <span>校园资讯</span>
        <el-button v-if="isAdmin" type="primary" icon="el-icon-plus" @click="handleAdd">
          发布资讯
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable></el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option label="学校新闻" value="SCHOOL_NEWS"></el-option>
            <el-option label="通知公告" value="NOTICE"></el-option>
            <el-option label="学术动态" value="ACADEMIC"></el-option>
            <el-option label="热点话题" value="HOT_TOPIC"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadData">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 资讯列表 -->
      <div class="news-list">
        <div v-for="item in tableData" :key="item.id" class="news-item" @click="handleView(item)">
          <div class="news-cover" v-if="item.coverImage">
            <img :src="item.coverImage" alt="">
          </div>
          <div class="news-content">
            <div class="news-header">
              <h3>
                <el-tag v-if="item.isTop" type="danger" size="mini">置顶</el-tag>
                {{ item.title }}
              </h3>
              <el-tag :type="getCategoryColor(item.category)" size="small">
                {{ getCategoryLabel(item.category) }}
              </el-tag>
            </div>
            <p class="news-summary">{{ item.summary }}</p>
            <div class="news-meta">
              <span class="meta-item">
                <i class="el-icon-view"></i>
                {{ item.viewCount }}
              </span>
              <span class="meta-item">
                <i class="el-icon-star-on"></i>
                {{ item.likeCount }}
              </span>
              <span class="meta-item">
                <i class="el-icon-time"></i>
                {{ item.publishTime }}
              </span>
            </div>
          </div>
          <div v-if="isAdmin" class="news-actions">
            <el-button type="text" size="small" @click.stop="handleEdit(item)">编辑</el-button>
            <el-button type="text" size="small" @click.stop="handleDelete(item)">删除</el-button>
          </div>
        </div>
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

    <!-- 新增/编辑资讯对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="60%" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="资讯标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入资讯标题"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="学校新闻" value="SCHOOL_NEWS"></el-option>
            <el-option label="通知公告" value="NOTICE"></el-option>
            <el-option label="学术动态" value="ACADEMIC"></el-option>
            <el-option label="热点话题" value="HOT_TOPIC"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input type="textarea" v-model="form.summary" :rows="3" placeholder="请输入摘要"></el-input>
        </el-form-item>
        <el-form-item label="正文内容" prop="content">
          <el-input type="textarea" v-model="form.content" :rows="8" placeholder="请输入正文内容，支持HTML格式"></el-input>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            💡 支持HTML格式，添加链接示例：&lt;a href="https://www.example.com" target="_blank"&gt;点击这里&lt;/a&gt;
          </div>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.author" placeholder="请输入作者"></el-input>
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="form.coverImage" placeholder="请输入图片URL"></el-input>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="form.publishTime"
            type="datetime"
            placeholder="选择发布时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">已发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'News',
  data() {
    return {
      loading: false,
      searchForm: {
        keyword: '',
        category: ''
      },
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      form: {
        title: '',
        category: '',
        summary: '',
        content: '',
        author: '',
        coverImage: '',
        publishTime: '',
        isTop: 0,
        status: 1
      },
      rules: {
        title: [{ required: true, message: '请输入资讯标题', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
        content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['isAdmin'])
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await this.$axios.get('/news/page', {
          params: {
            current: this.currentPage,
            size: this.pageSize,
            status: 1,
            ...this.searchForm
          }
        })
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
      this.searchForm = { keyword: '', category: '' }
      this.currentPage = 1
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '发布资讯'
      this.form = {
        title: '',
        category: '',
        summary: '',
        content: '',
        author: this.$store.state.user.realName || '',
        coverImage: '',
        publishTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        isTop: 0,
        viewCount: 0,
        likeCount: 0,
        status: 1
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑资讯'
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSave() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            if (this.form.id) {
              await this.$axios.put(`/news/${this.form.id}`, this.form)
              this.$message.success('更新成功')
            } else {
              await this.$axios.post('/news', this.form)
              this.$message.success('添加成功')
            }
            this.dialogVisible = false
            this.loadData()
          } catch (error) {
            console.error('保存失败:', error)
            this.$message.error('保存失败')
          }
        }
      })
    },
    handleView(row) {
      this.$message.info('查看资讯: ' + row.title)
      // 增加浏览次数
      this.$axios.get(`/news/${row.id}`)
    },
    handleDelete(row) {
      this.$confirm('确定要删除该资讯吗?', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await this.$axios.delete(`/news/${row.id}`)
          this.$message.success('删除成功')
          this.loadData()
        } catch (error) {
          console.error('删除失败:', error)
        }
      }).catch(() => {})
    },
    getCategoryLabel(category) {
      const map = {
        SCHOOL_NEWS: '学校新闻',
        NOTICE: '通知公告',
        ACADEMIC: '学术动态',
        HOT_TOPIC: '热点话题'
      }
      return map[category] || category
    },
    getCategoryColor(category) {
      const map = {
        SCHOOL_NEWS: 'primary',
        NOTICE: 'danger',
        ACADEMIC: 'success',
        HOT_TOPIC: 'warning'
      }
      return map[category] || 'info'
    }
  },
  mounted() {
    this.loadData()
  }
}
</script>

<style scoped>
.news-container {
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

.news-list {
  margin-bottom: 20px;
}

.news-item {
  display: flex;
  padding: 20px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: all 0.3s;
}

.news-item:hover {
  background-color: #f5f7fa;
}

.news-cover {
  width: 200px;
  height: 120px;
  flex-shrink: 0;
  margin-right: 20px;
  overflow: hidden;
  border-radius: 5px;
}

.news-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.news-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.news-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.news-header h3 {
  font-size: 18px;
  color: #303133;
  margin: 0;
  flex: 1;
}

.news-summary {
  color: #606266;
  font-size: 14px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.news-meta {
  display: flex;
  gap: 20px;
}

.meta-item {
  color: #909399;
  font-size: 13px;
  display: flex;
  align-items: center;
}

.meta-item i {
  margin-right: 3px;
}

.news-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-left: 20px;
}
</style>

